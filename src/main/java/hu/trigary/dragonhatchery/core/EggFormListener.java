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
			plugin.getLogger().log(Level.FINE,
					() -> logPrefix + "Egg spawning was already cancelled, ignoring event");
			return;
		}
		
		try {
			plugin.getLogger().log(Level.FINE,
					() -> logPrefix + "Egg spawning was not cancelled, handling it");
			handleEggSpawn(event);
		} catch (Throwable t) {
			event.setCancelled(true);
			String players = event.getBlock().getLocation()
					.getNearbyPlayers(500)
					.stream()
					.map(HumanEntity::getName)
					.collect(Collectors.joining(", "));
			plugin.getLogger().log(Level.SEVERE, logPrefix + "Error handling egg spawning; "
					+ "cancelling event; nearby players when this happened: " + players, t);
		}
	}
	
	/**
	 * Handles the egg spawning, possibly cancelling or modifying it.
	 * This method is only called when the event should be handled:
	 * checks regarding whether to ignore the event are done prior, outside this method.
	 * This method is allowed to throw exceptions and expects them to be gracefully handled.
	 *
	 * @param event the event to modify
	 */
	private void handleEggSpawn(@NotNull DragonEggFormEvent event) {
		EggScenario scenario = EggScenario.getMatching(event.getDragonBattle());
		plugin.getLogger().log(Level.FINE, () -> logPrefix + "Detected scenario: " + scenario);
		
		ScenarioLogic logic = plugin.getScenarioLogicHolder().getLogicFor(scenario);
		if (logic == null) {
			throw new IllegalStateException("Logic is null; did the config fail to load?");
		}
		
		if (logic.shouldAllowEggSpawn()) {
			logic.handleEggSpawn(event.getDragonBattle(), event.getNewState());
			plugin.getLogger().log(Level.FINE,
					() -> logPrefix + "Allowed egg spawning, updated block");
		} else {
			event.setCancelled(true);
			plugin.getLogger().log(Level.FINE, () -> logPrefix + "Cancelled egg spawning");
		}
	}
}
