package hu.trigary.dragonhatchery.core;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import hu.trigary.dragonhatchery.util.ConfigHelper;
import hu.trigary.dragonhatchery.util.WeightedRandomCollection;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Defines what should happen in case of a specific {@link EggScenario}.
 */
public class ScenarioLogic {
	private final String logPrefix;
	private final DragonHatcheryPlugin plugin;
	private final double spawnChance;
	private final WeightedRandomCollection<BlockData> blocks;
	
	/**
	 * Constructs a new instance.
	 * Should usually be called by {@link ScenarioLogicHolder}.
	 * Fails in case of an invalid configuration.
	 *
	 * @param plugin the plugin instance
	 * @param config the configuration that contains the values to use
	 */
	@Contract(pure = true)
	public ScenarioLogic(@NotNull DragonHatcheryPlugin plugin,
			@NotNull ConfigurationSection config) {
		logPrefix = getClass().getSimpleName() + "#" + config.getName() + ": ";
		this.plugin = plugin;
		
		spawnChance = ConfigHelper.parseValue(config, "spawn-chance", raw -> {
			double v = Double.parseDouble(raw);
			Validate.isTrue(v >= 0 && v <= 1, "Chance must be between 0 and 1 (both inclusive)");
			return v;
		});
		plugin.getLogger().log(Level.FINE, () -> logPrefix + "Spawn chance = " + spawnChance);
		
		//If any entry in the list is invalid: invalidate the entire instance.
		//Why? Because we have proper fallback logic; no need to use improper weights.
		
		ConfigurationSection spawnedBlocksSection = ConfigHelper
				.computeValue(config, "spawned-block", (c, k) -> {
					ConfigurationSection section = c.getConfigurationSection(k);
					Validate.notNull(section, "Missing section");
					Validate.notEmpty(section.getKeys(false),
							"There must be at least 1 entry");
					return section;
				});
		
		List<Map.Entry<BlockData, Double>> rawBlocks = new ArrayList<>();
		
		for (String spawnedBlockKey : spawnedBlocksSection.getKeys(false)) {
			ConfigurationSection section = spawnedBlocksSection
					.getConfigurationSection(spawnedBlockKey);
			
			Material blockType = ConfigHelper.parseValue(section, "block-type", raw -> {
				Material v = Material.matchMaterial(raw.toUpperCase());
				Validate.notNull(v, "Material not found");
				return v;
			});
			plugin.getLogger().log(Level.FINE, () -> logPrefix + "Block type = " + blockType);
			
			BlockData blockData = ConfigHelper.parseValue(section,
					"block-data", blockType::createBlockData);
			plugin.getLogger().log(Level.FINE,
					() -> logPrefix + "Block data = " + blockData.getAsString(true));
			
			double weight = ConfigHelper.parseValue(section,
					"weight", Double::parseDouble);
			plugin.getLogger().log(Level.FINE, () -> logPrefix + "Weight = " + weight);
			
			rawBlocks.add(Map.entry(blockData, weight));
		}
		
		blocks = new WeightedRandomCollection<>(rawBlocks,
				Map.Entry::getKey, Map.Entry::getValue);
	}
	
	/**
	 * Returns whether or not the egg spawning should be cancelled.
	 * Cancelling means that no blocks will appear.
	 *
	 * @return true if the egg spawning should get cancelled, false otherwise
	 */
	@Contract(pure = true)
	public boolean shouldAllowEggSpawn() {
		double random = ThreadLocalRandom.current().nextDouble();
		plugin.getLogger().log(Level.FINE,
				() -> logPrefix + "Rolled should-spawn value: " + random);
		return random < spawnChance;
	}
	
	/**
	 * Handles the egg spawning, potentially modifying the resulting block
	 * and potentially other values as well.
	 *
	 * @param battle the battle that caused the egg spawning
	 * @param newBlock the block that will get spawned, mutable
	 */
	public void handleEggSpawn(@NotNull DragonBattle battle, @NotNull BlockState newBlock) {
		BlockData random = blocks.getRandom();
		plugin.getLogger().log(Level.FINE,
				() -> logPrefix + "Rolled block: " + random.getAsString(true));
		newBlock.setBlockData(random.clone());
	}
}
