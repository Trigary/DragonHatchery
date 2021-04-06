package hu.trigary.dragonhatchery.core;

import hu.trigary.dragonhatchery.BukkitTestBase;
import hu.trigary.dragonhatchery.util.InvalidConfigException;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Tests the {@link ScenarioLogic} class.
 */
public class ScenarioLogicTest extends BukkitTestBase {
	
	/**
	 * Tests that valid {@link ScenarioLogic} configurations successfully get parsed.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"always-spawn.yml", "block-data.yml",
			"stone-and-dirt-blocks.yml", "never-spawn.yml",
			"simple.yml", "sometimes-spawn.yml"})
	void testConstructionValid(String filename) throws IOException {
		InputStream stream = getClass().getResourceAsStream("/logic/" + filename);
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			ConfigurationSection config = YamlConfiguration.loadConfiguration(reader);
			//noinspection ResultOfObjectAllocationIgnored
			new ScenarioLogic(getPlugin(), config);
		}
	}
	
	/**
	 * Tests that {@link ScenarioLogic#shouldAllowEggSpawn()}
	 * correctly returns always false or true or returns both values,
	 * depending on the configuration.
	 */
	@Test
	@Timeout(value = 10)
	void testShouldEggSpawn() {
		int iterations = 100;
		
		ScenarioLogic always = createLogic("always-spawn.yml");
		for (int i = 0; i < iterations; i++) {
			Assertions.assertTrue(always.shouldAllowEggSpawn());
		}
		
		ScenarioLogic never = createLogic("never-spawn.yml");
		for (int i = 0; i < iterations; i++) {
			Assertions.assertFalse(never.shouldAllowEggSpawn());
		}
		
		ScenarioLogic sometimes = createLogic("sometimes-spawn.yml");
		boolean wasTrue = false;
		boolean wasFalse = false;
		while (!wasTrue || !wasFalse) {
			if (sometimes.shouldAllowEggSpawn()) {
				wasTrue = true;
			} else {
				wasFalse = true;
			}
		}
	}
	
	/**
	 * Tests that {@link ScenarioLogic#handleEggSpawn(DragonBattle, BlockState)}
	 * correctly updates the {@link BlockState} it received as a parameter.
	 * Also tests that all blocks specified in the configuration are used.
	 */
	@Test
	@Timeout(value = 10)
	void testHandlEggSpawn() {
		Set<Material> yetToSee = EnumSet.of(Material.STONE, Material.DIRT);
		ScenarioLogic logic = createLogic("stone-and-dirt-blocks.yml");
		
		while (!yetToSee.isEmpty()) {
			BlockState block = Mockito.mock(BlockState.class);
			ArgumentCaptor<BlockData> captor = ArgumentCaptor.forClass(BlockData.class);
			Mockito.doNothing().when(block).setBlockData(captor.capture());
			
			DragonBattle battle = Mockito.mock(DragonBattle.class);
			logic.handleEggSpawn(battle, block);
			yetToSee.remove(captor.getValue().getMaterial());
		}
	}
	
	/**
	 * Tests that the parsing of invalid {@link ScenarioLogic}
	 * configurations fail (with the correct error).
	 */
	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("invalidParameterSource")
	void testConstructionInvalid(String ignored, ConfigurationSection config,
			Predicate<InvalidConfigException> exceptionValidator) {
		try {
			//noinspection ResultOfObjectAllocationIgnored
			new ScenarioLogic(getPlugin(), config);
			Assertions.fail("Expression should have thrown exception");
		} catch (InvalidConfigException e) {
			if (!exceptionValidator.test(e)) {
				throw new AssertionError("Invalid exception", e);
			}
		}
	}
	
	
	/**
	 * The {@link MethodSource} for
	 * {@link #testConstructionInvalid(String, ConfigurationSection, Predicate)}.
	 *
	 * @return the parameters of the mentioned test
	 */
	@Contract(pure = true)
	private @NotNull Stream<Arguments> invalidParameterSource() {
		return Stream.of(
				Arguments.of("spawn chance missing",
						loadConfig("simple.yml",
								c -> c.set("spawn-chance", null)),
						createMissingError("spawn-chance")),
				
				Arguments.of("spawn chance not a number",
						loadConfig("simple.yml",
								c -> c.set("spawn-chance", "not-a-number")),
						createParseError("spawn-chance")),
				
				Arguments.of("spawn chance above max",
						loadConfig("simple.yml",
								c -> c.set("spawn-chance", 1.01)),
						createParseError("spawn-chance")),
				
				Arguments.of("spawn chance below min",
						loadConfig("simple.yml",
								c -> c.set("spawn-chance", -0.01)),
						createParseError("spawn-chance")),
				
				Arguments.of("spawned block section missing",
						loadConfig("simple.yml",
								c -> c.set("spawned-block", null)),
						createComputeError("spawned-block")),
				
				Arguments.of("spawned block section empty",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone", null)),
						createComputeError("spawned-block")),
				
				Arguments.of("spawned block section contains both valid and invalid",
						loadConfig("simple.yml", c -> {
							c.createSection("spawned-block.invalid",
									Objects.requireNonNull(c.getConfigurationSection(
											"spawned-block.stone")).getValues(true));
							c.set("spawned-block.invalid.weight", "invalid");
						}),
						createParseError("spawned-block.invalid.weight")),
				
				Arguments.of("block data missing",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.block-data", null)),
						createMissingError("spawned-block.stone.block-data")),
				
				Arguments.of("block data invalid",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.block-data", "invalid")),
						createParseError("spawned-block.stone.block-data")),
				
				Arguments.of("block type missing",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.block-type", null)),
						createMissingError("spawned-block.stone.block-type")),
				
				Arguments.of("block type invalid",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.block-type", "does-not-exist")),
						createParseError("spawned-block.stone.block-type")),
				
				Arguments.of("weight missing",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.weight", null)),
						createMissingError("spawned-block.stone.weight")),
				
				Arguments.of("weight not a number",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.weight", "not-a-number")),
						createParseError("spawned-block.stone.weight")),
				
				Arguments.of("weight below min",
						loadConfig("simple.yml",
								c -> c.set("spawned-block.stone.weight", 0)),
						createParseError("spawned-block.stone.weight"))
		);
	}
	
	@Contract(pure = true)
	private @NotNull ScenarioLogic createLogic(@NotNull String filename) {
		return new ScenarioLogic(getPlugin(), loadConfig(filename, config -> {}));
	}
	
	/**
	 * Loads a configuration, modifies it, then returns it.
	 *
	 * @param filename the path of the configuration to start with
	 * @param modifier the modifications to apply to the configuration
	 * @return the modified configuration
	 */
	@NotNull
	private ConfigurationSection loadConfig(@NotNull String filename,
			@NotNull Consumer<ConfigurationSection> modifier) {
		InputStream stream = ScenarioLogic.class.getResourceAsStream("/logic/" + filename);
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			ConfigurationSection config = YamlConfiguration.loadConfiguration(reader);
			modifier.accept(config);
			return config;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates a {@link Predicate} which determines whether
	 * a {@link InvalidConfigException} was thrown because
	 * the value at the specified path was missing.
	 *
	 * @param path the path at which the value might be missing
	 * @return a {@link Predicate} that returns whether
	 * the error was caused by a missing value at the specified path
	 */
	@Contract(pure = true)
	private @NotNull Predicate<InvalidConfigException> createMissingError(@NotNull String path) {
		return e -> e.getShortError().equalsIgnoreCase("missing value")
				&& e.getLocation().equals(path);
	}
	
	/**
	 * Creates a {@link Predicate} which determines whether
	 * a {@link InvalidConfigException} was thrown because
	 * the value at the specified path failed to be parsed.
	 *
	 * @param path the path at which the value might be incorrect
	 * @return a {@link Predicate} that returns whether
	 * the error was caused by a failed parsing at the specified path
	 * @see hu.trigary.dragonhatchery.util.ConfigHelper#parseValue(ConfigurationSection, String, Function)
	 */
	@Contract(pure = true)
	private @NotNull Predicate<InvalidConfigException> createParseError(@NotNull String path) {
		return e -> e.getShortError().toLowerCase().startsWith("parse error")
				&& e.getLocation().equals(path);
	}
	
	/**
	 * Creates a {@link Predicate} which determines whether
	 * a {@link InvalidConfigException} was thrown because
	 * the value at the specified path failed to be computed.
	 *
	 * @param path the path at which the value might be incorrect
	 * @return a {@link Predicate} that returns whether
	 * the error was caused by a failed computing at the specified path
	 * @see hu.trigary.dragonhatchery.util.ConfigHelper#computeValue(ConfigurationSection, String, BiFunction)
	 */
	@Contract(pure = true)
	private @NotNull Predicate<InvalidConfigException> createComputeError(@NotNull String path) {
		return e -> e.getShortError().toLowerCase().startsWith("compute error")
				&& e.getLocation().equals(path);
	}
}
