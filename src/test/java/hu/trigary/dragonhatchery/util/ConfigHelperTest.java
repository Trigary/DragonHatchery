package hu.trigary.dragonhatchery.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Tests the {@link ConfigHelper} class.
 */
public class ConfigHelperTest {
	
	/**
	 * Tests that {@link ConfigHelper#getSection(ConfigurationSection, String)}
	 * works when a valid key is specified.
	 */
	@Test
	void testGetSectionValid() {
		ConfigurationSection config = new YamlConfiguration();
		config.createSection("key");
		Assertions.assertNotNull(ConfigHelper.getSection(config, "key"));
	}
	
	/**
	 * Tests that {@link ConfigHelper#getSection(ConfigurationSection, String)}
	 * fails when a missing key is specified.
	 */
	@Test
	void testGetSectionMissing() {
		ConfigurationSection config = new YamlConfiguration();
		config.createSection("key");
		Assertions.assertThrows(InvalidConfigException.class,
				() -> Objects.requireNonNull(ConfigHelper.getSection(config, "invalid")));
	}
	
	/**
	 * Tests that {@link ConfigHelper#parseValue(ConfigurationSection, String, Function)}
	 * works when a valid key and a valid parser is specified.
	 */
	@Test
	void testParseValueValid() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "42");
		Assertions.assertNotNull(ConfigHelper.parseValue(config, "key", Integer::parseInt));
	}
	
	/**
	 * Tests that {@link ConfigHelper#parseValue(ConfigurationSection, String, Function)}
	 * fails when a missing key is specified.
	 */
	@Test
	void testParseValueMissing() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "42");
		Assertions.assertThrows(InvalidConfigException.class,
				() -> Objects.requireNonNull(ConfigHelper
						.parseValue(config, "invalid", Integer::parseInt)));
	}
	
	/**
	 * Tests that {@link ConfigHelper#parseValue(ConfigurationSection, String, Function)}
	 * fails when the parser returns a null value.
	 */
	@Test
	void testParseValueNull() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "42");
		Assertions.assertThrows(InvalidConfigException.class,
				() -> Objects.requireNonNull(ConfigHelper
						.parseValue(config, "key", raw -> null)));
	}
	
	/**
	 * Tests that {@link ConfigHelper#parseValue(ConfigurationSection, String, Function)}
	 * fails when the parser throws an exception.
	 */
	@Test
	void testParseValueThrows() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "not a number");
		Assertions.assertThrows(InvalidConfigException.class,
				() -> Objects.requireNonNull(ConfigHelper
						.parseValue(config, "key", Integer::parseInt)));
	}
	
	/**
	 * Tests that {@link ConfigHelper#computeValue(ConfigurationSection, String, BiFunction)}
	 * works when a valid key and a valid computer is specified.
	 */
	@Test
	void testComputeValueValid() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "42");
		Assertions.assertNotNull(ConfigHelper.computeValue(config, "key", (conf, k) -> {
			String raw = Objects.requireNonNull(conf.getString(k));
			return Integer.parseInt(raw);
		}));
	}
	
	/**
	 * Tests that {@link ConfigHelper#computeValue(ConfigurationSection, String, BiFunction)}
	 * fails when the computer returns a null value.
	 */
	@Test
	void testComputeValueNull() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "42");
		Assertions.assertThrows(InvalidConfigException.class,
				() -> Objects.requireNonNull(ConfigHelper
						.computeValue(config, "key", (conf, k) -> null)));
	}
	
	/**
	 * Tests that {@link ConfigHelper#computeValue(ConfigurationSection, String, BiFunction)}
	 * fails when the parser throws an exception.
	 */
	@Test
	void testComputeValueThrows() {
		ConfigurationSection config = new YamlConfiguration();
		config.set("key", "not a number");
		Assertions.assertThrows(InvalidConfigException.class,
				() -> Objects.requireNonNull(ConfigHelper
						.computeValue(config, "key", (conf, k) -> {
							String raw = Objects.requireNonNull(conf.getString(k));
							return Integer.parseInt(raw);
						})));
	}
}
