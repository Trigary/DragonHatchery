package hu.trigary.dragonhatchery.core;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import io.papermc.paper.event.block.DragonEggFormEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Listener responsible for the main logic of this plugin:
 * listening to and modifying dragon egg spawning.
 */
public class EggFormListener implements Listener {
	private final String logPrefix = getClass().getSimpleName() + ": ";
	private final DragonHatcheryPlugin plugin;
	
	/**
	 * Constructs a new instance. It needs to be manually registered
	 * via {@link PluginManager#registerEvents(Listener, Plugin)}.
	 *
	 * @param plugin the plugin instance
	 */
	public EggFormListener(@NotNull DragonHatcheryPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Handles the event that fires when the dragon egg is about to appear.
	 * We listen on the {@link EventPriority#NORMAL}:
	 * <ul>
	 *     <li>We want to allow other plugins to cancel the event before us.</li>
	 *     <li>We modify the event's cancelled state and the result of event,
	 *     other plugins should be able to see that.</li>
	 * </ul>
	 *
	 * @param event the event being fired
	 */
	@EventHandler
	private void onEggSpawn(@NotNull DragonEggFormEvent event) {
		if (event.isCancelled()) {
			plugin.getLogger().log(Level.OFF,
					() -> logPrefix + "Egg spawning was already cancelled, ignoring event");
			return;
		}
		
		EggScenario scenario = EggScenario.getMatching(event.getDragonBattle());
		plugin.getLogger().log(Level.OFF, () -> logPrefix + "Detected scenario: " + scenario);
		
		ScenarioLogic logic = plugin.getScenarioLogicHolder().getLogicFor(scenario);
		if (logic == null) {
			event.setCancelled(true);
			String players = event.getBlock().getLocation()
					.getNearbyPlayers(500)
					.stream()
					.map(HumanEntity::getName)
					.collect(Collectors.joining(", "));
			plugin.getLogger().severe(logPrefix + "Logic is null"
					+ " (Did it fail to load on startup?), cancelling event,"
					+ " nearby players when this happened: " + players);
			return;
		}
		
		if (logic.shouldCancelEggSpawn()) {
			event.setCancelled(true);
			plugin.getLogger().log(Level.OFF, () -> logPrefix + "Cancelled egg spawning");
		} else {
			logic.handleEggSpawn(event.getDragonBattle(), event.getNewState());
			plugin.getLogger().log(Level.OFF,
					() -> logPrefix + "Allowed egg spawning, updated block");
		}
	}
}
