package hu.trigary.dragonhatchery;

import hu.trigary.dragonhatchery.command.BaseCommandHandler;
import hu.trigary.dragonhatchery.core.EggFormListener;
import hu.trigary.dragonhatchery.core.ScenarioLogicHolder;
import hu.trigary.dragonhatchery.util.ConfigHelper;
import hu.trigary.dragonhatchery.util.DebugLogHandler;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main class of the Bukkit plugin.
 */
public class DragonHatcheryPlugin extends JavaPlugin {
	
	public static void main(String[] args) {
		//Fake entry point for code analysis
		DragonHatcheryPlugin hatchery = new DragonHatcheryPlugin();
		hatchery.onEnable();
		hatchery.onDisable();
	}
	
	private final String logPrefix = getClass().getSimpleName() + ": ";
	private ScenarioLogicHolder scenarioLogicHolder;
	private EggFormListener eggFormListener;
	
	@Override
	public void onEnable() {
		DebugLogHandler.attachDebugLogger(this);
		validateServer();
		
		reload();
		
		PluginCommand baseCommand = getCommand("dragonhatchery");
		Validate.notNull(baseCommand, "Command must be found");
		//This also sets the tab completer
		baseCommand.setExecutor(new BaseCommandHandler(this));
	}
	
	/**
	 * Gets the current {@link ScenarioLogicHolder} instance.
	 * The returned value mustn't be cached: it might chance during runtime.
	 *
	 * @return the current {@link ScenarioLogicHolder} instance
	 */
	public ScenarioLogicHolder getScenarioLogicHolder() {
		return scenarioLogicHolder;
	}
	
	/**
	 * Initializes or re-initializes this plugin
	 * (by eg. also reloading its configuration).
	 */
	public void reload() {
		saveDefaultConfig();
		reloadConfig();
		
		boolean enableDebugLogging;
		try {
			enableDebugLogging = ConfigHelper.parseValue(getConfig(),
					"debug-logging", Boolean::parseBoolean);
		} catch (ConfigHelper.InvalidConfigException e) {
			getLogger().log(Level.SEVERE,
					logPrefix + "Invalid config, defaulting to debug logging", e);
			enableDebugLogging = true;
		}
		getLogger().setLevel(enableDebugLogging ? Level.OFF : Level.INFO);
		
		scenarioLogicHolder = new ScenarioLogicHolder(this);
		
		if (eggFormListener != null) {
			HandlerList.unregisterAll(eggFormListener);
		}
		eggFormListener = new EggFormListener(this);
		Bukkit.getPluginManager().registerEvents(eggFormListener, this);
	}
	
	/**
	 * Asserts that the current server is capable of running this plugin.
	 * Throws an exception if it's not.
	 */
	private void validateServer() {
		String loadError;
		try {
			Class.forName("io.papermc.paper.event.block.DragonEggFormEvent");
			loadError = null;
		} catch (ClassNotFoundException notSupported) {
			try {
				Class.forName("com.destroystokyo.paper.PaperConfig");
				loadError = "Your server is outdated, please update.";
			} catch (ClassNotFoundException notPaper) {
				loadError = "The server isn't running https://github.com/PaperMC/Paper"
						+ " (and not one of its forks either).";
			}
		}
		
		if (loadError != null) {
			//We throw exceptions so that all server owners notice the error...
			//Positive side effect: the plugin gets disabled
			throw new RuntimeException(loadError);
		}
	}
}
