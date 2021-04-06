package hu.trigary.dragonhatchery.core;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import hu.trigary.dragonhatchery.util.ConfigHelper;
import hu.trigary.dragonhatchery.util.InvalidConfigException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Container of {@link ScenarioLogic} instances.
 * This class is also responsible for creating them (from the configuration).
 */
public class ScenarioLogicHolder {
	private final String logPrefix = getClass().getSimpleName() + ": ";
	private final Map<EggScenario, ScenarioLogic> logics = new EnumMap<>(EggScenario.class);
	private final DragonHatcheryPlugin plugin;
	
	/**
	 * Constructs a new instance.
	 * Internally calls {@link Plugin#getConfig()}, but doesn't fail if it's invalid.
	 *
	 * @param plugin the plugin instance
	 */
	@Contract(pure = true)
	public ScenarioLogicHolder(@NotNull DragonHatcheryPlugin plugin) {
		this.plugin = plugin;
		
		ConfigurationSection config;
		try {
			config = ConfigHelper.getSection(plugin.getConfig(), "scenario");
		} catch (InvalidConfigException e) {
			plugin.getLogger().log(Level.SEVERE,
					logPrefix + "Invalid config, unable to load scenarios", e);
			return;
		}
		
		config.getKeys(false).stream()
				.filter(key -> parseScenario(key) == null)
				.forEach(key -> plugin.getLogger().log(Level.WARNING,
						logPrefix + "Ignoring unknown scenario: " + key));
		
		for (EggScenario scenario : EggScenario.values()) {
			String key = scenario.getConfigKey();
			
			ScenarioLogic logic;
			try {
				logic = new ScenarioLogic(plugin, ConfigHelper.getSection(config, key));
			} catch (Throwable t) {
				plugin.getLogger().log(Level.SEVERE,
						logPrefix + "Error parsing scenario: " + key, t);
				continue;
			}
			
			logics.put(scenario, logic);
			plugin.getLogger().log(Level.FINE,
					() -> logPrefix + "Registered logic for scenario: " + scenario);
		}
	}
	
	/**
	 * Gets the stored logic for the specified scenario.
	 * Null is returned in case there is no stored scenario,
	 * because the loading of the scenario has failed.
	 *
	 * @param scenario the scenario whose associated logic to get
	 * @return the logic associated with the scenario
	 * or null, if there's no valid one stored in this instance
	 */
	@Contract(pure = true)
	public @Nullable ScenarioLogic getLogicFor(@NotNull EggScenario scenario) {
		return logics.get(scenario);
	}
	
	/**
	 * Gets the {@link EggScenario} constant, if any,
	 * associated with the specified value.
	 * Returns null in case no matching scenario could be found.
	 *
	 * @param raw the value to parse as a scenario
	 * @return the parsed scenario or null, if parsing failed
	 */
	@Contract(pure = true)
	private @Nullable EggScenario parseScenario(@NotNull String raw) {
		return Arrays.stream(EggScenario.values())
				.filter(scenario -> scenario.getConfigKey().equals(raw))
				.findAny().orElse(null);
	}
}
