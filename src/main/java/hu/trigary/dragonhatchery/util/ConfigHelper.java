package hu.trigary.dragonhatchery.util;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Helper around {@link ConfigurationSection} based configurations.
 * Handles missing/invalid values by throwing exceptions
 * that contain many relevant details, eg. the location of the error.
 */
public final class ConfigHelper {
	private ConfigHelper() {}
	
	/**
	 * Gets the {@link ConfigurationSection} at the specified location.
	 * Fails if the section doesn't exist or if the value isn't a section.
	 *
	 * @param config the config in which to search
	 * @param key the identifier of the section
	 * @return the requested section
	 * @throws InvalidConfigException if the section doesn't exist
	 */
	@Contract(pure = true)
	public static @NotNull ConfigurationSection getSection(@NotNull ConfigurationSection config,
			@NotNull String key) throws InvalidConfigException {
		ConfigurationSection result = config.getConfigurationSection(key);
		if (result == null) {
			throw new InvalidConfigException(config, key, "Missing section");
		}
		return result;
	}
	
	/**
	 * Parses the value at the specified location: gets the value from the config
	 * in raw {@link String} form and applies the specified parsing function.
	 * Fails if the value doesn't exist.
	 * It is valid to throw exceptions during parsing.
	 * The parsed values mustn't be null.
	 *
	 * @param config the config in which to search
	 * @param key the identifier of the value
	 * @param parser the function that parses the raw {@link String} value
	 * @param <T> the type of the parsed value
	 * @return the parsed value
	 * @throws InvalidConfigException if the value doesn't exist or if parsing failed
	 */
	@Contract(pure = true)
	public static <T> @NotNull T parseValue(@NotNull ConfigurationSection config,
			@NotNull String key, @NotNull Function<String, T> parser)
			throws InvalidConfigException {
		String raw = config.getString(key);
		if (raw == null) {
			throw new InvalidConfigException(config, key, "Missing value");
		}
		try {
			T result = parser.apply(raw);
			Validate.notNull(result, "Parsed value mustn't be null");
			return result;
		} catch (Throwable t) {
			throw new InvalidConfigException(config, key,
					"Parse error: invalid value: '" + raw + "'", t);
		}
	}
	
	/**
	 * Gets the value at the specified location: the specified parsing function
	 * is responsible for getting and parsing the value from the config.
	 * Fails if the value doesn't exist.
	 * It is valid to throw exceptions during parsing.
	 * The parsed values mustn't be null.
	 *
	 * @param config the config in which to search
	 * @param key the identifier of the value
	 * @param getter the function that gets and parses the value
	 * @param <T> the type of the parsed value
	 * @return the parsed value
	 * @throws InvalidConfigException if the value doesn't exist or if parsing failed
	 */
	@Contract(pure = true)
	public static <T> @NotNull T computeValue(@NotNull ConfigurationSection config,
			@NotNull String key, @NotNull BiFunction<ConfigurationSection, String, T> getter)
			throws InvalidConfigException {
		try {
			T result = getter.apply(config, key);
			Validate.notNull(result, "Computed value mustn't be null");
			return result;
		} catch (Throwable t) {
			throw new InvalidConfigException(config, key,
					"Compute error: invalid value", t);
		}
	}
}
