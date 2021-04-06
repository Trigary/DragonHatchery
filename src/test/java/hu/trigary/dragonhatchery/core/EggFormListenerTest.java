package hu.trigary.dragonhatchery.core;

import hu.trigary.dragonhatchery.BukkitTestBase;
import io.papermc.paper.event.block.DragonEggFormEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Tests the {@link EggFormListener} class.
 */
public class EggFormListenerTest extends BukkitTestBase {
	
	/**
	 * Tests that cancelled {@link DragonEggFormEvent} instances are ignored.
	 */
	@Test
	void testCancelledIsIgnored() {
		EggFormListener listener = createListener(Map.of());
		DragonEggFormEvent event = Mockito.mock(DragonEggFormEvent.class);
		Mockito.when(event.isCancelled()).thenReturn(true);
		Mockito.when(event.getDragonBattle()).thenThrow(AssertionError.class);
		Mockito.when(event.getBlock()).thenThrow(AssertionError.class);
		Mockito.when(event.getNewState()).thenThrow(AssertionError.class);
		callEvent(listener, event);
	}
	
	/**
	 * Tests that an invalid {@link ScenarioLogic} causes
	 * the events that fit into its {@link EggScenario} to get cancelled.
	 */
	@Test
	void testIncorrectLogicCausesCancellation() {
		ConfigurationSection config = loadConfig("simple.yml");
		config.set("spawn-chance", "invalid");
		EggFormListener listener = createListener(Map.of(
				EggScenario.FIRST, () -> config,
				EggScenario.SUBSEQUENT, () -> config
		));
		
		Location location = Mockito.mock(Location.class);
		Mockito.when(location.getNearbyPlayers(Mockito.anyDouble()))
				.thenReturn(Collections.emptyList());
		Block block = Mockito.mock(Block.class);
		Mockito.when(block.getLocation()).thenReturn(location);
		
		BlockState blockState = Mockito.mock(BlockState.class);
		DragonBattle battle = Mockito.mock(DragonBattle.class);
		DragonEggFormEvent event = new DragonEggFormEvent(block, blockState, battle);
		
		callEvent(listener, event);
		Assertions.assertTrue(event.isCancelled());
	}
	
	/**
	 * Tests that when {@link ScenarioLogic#shouldAllowEggSpawn()}
	 * returns true the {@link DragonEggFormEvent#getNewState()} gets modified.
	 */
	@Test
	void testAllowedEggCausesChange() {
		ConfigurationSection config = loadConfig("always-spawn.yml");
		EggFormListener listener = createListener(Map.of(
				EggScenario.FIRST, () -> config,
				EggScenario.SUBSEQUENT, () -> config
		));
		
		BlockState blockState = Mockito.mock(BlockState.class);
		ArgumentCaptor<BlockData> captor = ArgumentCaptor.forClass(BlockData.class);
		Mockito.doNothing().when(blockState).setBlockData(captor.capture());
		
		Block block = Mockito.mock(Block.class);
		DragonBattle battle = Mockito.mock(DragonBattle.class);
		DragonEggFormEvent event = new DragonEggFormEvent(block, blockState, battle);
		
		callEvent(listener, event);
		Assertions.assertFalse(captor.getAllValues().isEmpty());
		Assertions.assertFalse(event.isCancelled());
	}
	
	/**
	 * Tests that when {@link ScenarioLogic#shouldAllowEggSpawn()}
	 * returns false the {@link DragonEggFormEvent#getNewState()} stays unmodified.
	 */
	@Test
	void testDisallowedEggCausesCancellation() {
		ConfigurationSection config = loadConfig("never-spawn.yml");
		EggFormListener listener = createListener(Map.of(
				EggScenario.FIRST, () -> config,
				EggScenario.SUBSEQUENT, () -> config
		));
		
		BlockState blockState = Mockito.mock(BlockState.class);
		Block block = Mockito.mock(Block.class);
		DragonBattle battle = Mockito.mock(DragonBattle.class);
		DragonEggFormEvent event = new DragonEggFormEvent(block, blockState, battle);
		
		callEvent(listener, event);
		Assertions.assertTrue(event.isCancelled());
	}
	
	/**
	 * Creates a {@link EggFormListener} instance whose {@link ScenarioLogic}
	 * instances are initialized with the specified configurations.
	 *
	 * @param listenerConfig the configuration of each scenario
	 * @return the newly created {@link EggFormListener} with the specified config
	 */
	@Contract("_ -> new")
	private @NotNull EggFormListener createListener(@NotNull Map<EggScenario,
			Supplier<ConfigurationSection>> listenerConfig) {
		FileConfiguration pluginConfig = new YamlConfiguration();
		listenerConfig.forEach((scenario, supplier) ->
				pluginConfig.set("scenario." + scenario.getConfigKey(), supplier.get()));
		Mockito.when(getPlugin().getConfig()).thenReturn(pluginConfig);
		ScenarioLogicHolder holder = new ScenarioLogicHolder(getPlugin());
		Mockito.when(getPlugin().getScenarioLogicHolder()).thenReturn(holder);
		return new EggFormListener(getPlugin());
	}
	
	/**
	 * Loads a {@link ScenarioLogic} configuration and returns it.
	 *
	 * @param filename the path of the configuration to load
	 * @return the loaded configuration
	 */
	@NotNull
	private ConfigurationSection loadConfig(@NotNull String filename) {
		InputStream stream = ScenarioLogic.class.getResourceAsStream("/logic/" + filename);
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			return YamlConfiguration.loadConfiguration(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Calls the (private) event listener method in the specified instance
	 * with the specified event as the parameter.
	 *
	 * @param listener the listener whose method should be invoked
	 * @param event the event that the listener should receive
	 */
	private void callEvent(@NotNull EggFormListener listener,
			@NotNull DragonEggFormEvent event) {
		try {
			Method method = ReflectionUtils.findMethod(listener.getClass(),
					"onEggSpawn", DragonEggFormEvent.class).orElseThrow();
			//use DragonEggFormEvent.class instead of event.getClass():
			//  the class instance might be a mocked instance or something
			method.setAccessible(true);
			method.invoke(listener, event);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
