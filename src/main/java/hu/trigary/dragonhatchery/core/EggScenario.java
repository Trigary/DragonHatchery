package hu.trigary.dragonhatchery.core;

import io.papermc.paper.event.block.DragonEggFormEvent;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the different situations in which a dragon egg appears.
 * Enum values are basically discrete context values.
 *
 * @see DragonEggFormEvent
 */
public enum EggScenario {
	
	/**
	 * The ender dragon is being killed for the first time.
	 * This is the first ender dragon death.
	 *
	 * @see DragonBattle#hasBeenPreviouslyKilled()
	 */
	FIRST,
	
	/**
	 * The ender dragon has been previously killed.
	 * This is a subsequent ender dragon death.
	 *
	 * @see DragonBattle#hasBeenPreviouslyKilled()
	 */
	SUBSEQUENT;
	
	/**
	 * Gets the appropriate enum constant for the specified situation.
	 *
	 * @param battle the battle in which the dragon was just killed
	 * @return the enum constant for the situation
	 */
	@Contract(pure = true)
	public static @NotNull EggScenario getMatching(@NotNull DragonBattle battle) {
		if (!battle.hasBeenPreviouslyKilled()) {
			return FIRST;
		} else {
			return SUBSEQUENT;
		}
	}
	
	/**
	 * Gets the identifier of this enum constant
	 * used inside {@link ConfigurationSection}.
	 *
	 * @return this constant's identifier to use in configs
	 */
	@Contract(pure = true)
	public @NotNull String getConfigKey() {
		return name().toLowerCase();
	}
}
