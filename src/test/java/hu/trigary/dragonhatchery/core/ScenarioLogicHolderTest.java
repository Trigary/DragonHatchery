package hu.trigary.dragonhatchery.core;

import hu.trigary.dragonhatchery.BukkitTestBase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Tests the {@link ScenarioLogicHolder} class.
 */
public class ScenarioLogicHolderTest extends BukkitTestBase {
	
	/**
	 * Tests that the loading of each {@link ScenarioLogic} succeeds
	 * if all of them are valid.
	 * Also tests that having keys that are not valid {@link EggScenario} values
	 * does not make the construction of {@link ScenarioLogicHolder} fail.
	 */
	@Test
	void testAllScenariosValid() {
		ConfigurationSection validConfig = loadConfig("simple.yml");
		FileConfiguration pluginConfig = new YamlConfiguration();
		pluginConfig.set("scenario.not-a-scenario", validConfig);
		for (EggScenario scenario : EggScenario.values()) {
			pluginConfig.set("scenario." + scenario.getConfigKey(), validConfig);
		}
		Mockito.when(getPlugin().getConfig()).thenReturn(pluginConfig);
		
		ScenarioLogicHolder holder = new ScenarioLogicHolder(getPlugin());
		for (EggScenario scenario : EggScenario.values()) {
			Assertions.assertNotNull(holder.getLogicFor(scenario));
		}
	}
	
	/**
	 * Tests that the loading of valid {@link ScenarioLogic} succeeds
	 * even if invalid {@link ScenarioLogic} configurations are included.
	 */
	@Test
	void testValidAndInvalidScenario() {
		EggScenario invalidScenario = EggScenario.FIRST;
		ConfigurationSection invalidConfig = loadConfig("simple.yml");
		invalidConfig.set("spawn-chance", "invalid");
		ConfigurationSection validConfig = loadConfig("simple.yml");
		
		FileConfiguration pluginConfig = new YamlConfiguration();
		for (EggScenario scenario : EggScenario.values()) {
			pluginConfig.set("scenario." + scenario.getConfigKey(),
					scenario == invalidScenario ? invalidConfig : validConfig);
		}
		Mockito.when(getPlugin().getConfig()).thenReturn(pluginConfig);
		
		ScenarioLogicHolder holder = new ScenarioLogicHolder(getPlugin());
		for (EggScenario scenario : EggScenario.values()) {
			if (scenario == invalidScenario) {
				Assertions.assertNull(holder.getLogicFor(scenario));
			} else {
				Assertions.assertNotNull(holder.getLogicFor(scenario));
			}
		}
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
}
