package hu.trigary.dragonhatchery.util;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception that's thrown when the config is invalid.
 */
public class InvalidConfigException extends RuntimeException {
	
	/**
	 * Generates {@link Exception#getMessage()} from the provided context.
	 *
	 * @param config the config in which the error is
	 * @param key the location of the error in the config
	 * @param message the description of the error
	 * @return the constructed error message
	 */
	@Contract(pure = true)
	private static @NotNull String getFullMessage(@NotNull ConfigurationSection config,
			@NotNull String key, @NotNull String message) {
		String current = config.getCurrentPath();
		String root = (current == null || current.isEmpty()) ? "" : current + ".";
		return "Invalid configuration! " + message
				+ "; location: " + root + key;
	}
	
	private final String shortError;
	private final String location;
	
	/**
	 * Constructs a new instance with the specified context
	 * and without any inner exceptions.
	 *
	 * @param config the config in which the error is
	 * @param key the location of the error in the config
	 * @param message the description of the error
	 */
	public InvalidConfigException(@NotNull ConfigurationSection config,
			@NotNull String key, @NotNull String message) {
		this(config, key, message, null);
	}
	
	/**
	 * Constructs a new instance with the specified context.
	 *
	 * @param config the config in which the error is
	 * @param key the location of the error in the config
	 * @param message the description of the error
	 * @param cause the reason this exception has to be constructed
	 */
	public InvalidConfigException(@NotNull ConfigurationSection config,
			@NotNull String key, @NotNull String message, @Nullable Throwable cause) {
		super(getFullMessage(config, key, message), cause);
		shortError = message;
		String current = config.getCurrentPath();
		location = (current == null || current.isEmpty()) ? key : current + "." + key;
	}
	
	/**
	 * Gets the short description of this error, without the error's location, etc.
	 * For example, this could return {@code "Missing value"}
	 * when the issue is that there was no value at the specified key.
	 *
	 * @return the short description of this error
	 */
	public String getShortError() {
		return shortError;
	}
	
	/**
	 * Gets the full path at which something was incorrect, which caused this error.
	 *
	 * @return the key path at which something was invalid
	 */
	public String getLocation() {
		return location;
	}
}
