package hu.trigary.dragonhatchery.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Tests the {@link WeightedRandomCollection} class.
 */
public class WeightedRandomCollectionTest {
	
	/**
	 * Tests whether the construction of an empty
	 * weighted random collection is indeed disallowed.
	 */
	@Test
	void testConstructEmpty() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new WeightedRandomCollection<>(List.of(),
						Function.identity(), Function.identity()));
	}
	
	/**
	 * Tests whether the {@link WeightedRandomCollection#getEntries()}
	 * returns the correct values.
	 */
	@Test
	void testGetEntries() {
		Set<Double> source = generateRandomDoubles();
		var collection = new WeightedRandomCollection<>(source,
				Function.identity(), Function.identity());
		Assertions.assertEquals(source, new HashSet<>(collection.getEntries()));
	}
	
	/**
	 * Tests whether {@link WeightedRandomCollection} has the correct distribution.
	 */
	@Test
	void testGetRandom() {
		int sampleCount = 1_000_000;
		double leniency = 0.05;
		
		Set<Double> doubles = generateRandomDoubles();
		double doubleSum = doubles.stream().mapToDouble(d -> d).sum();
		
		var collection = new WeightedRandomCollection<>(doubles,
				Function.identity(), Function.identity());
		
		Map<Double, Integer> histogram = new HashMap<>();
		for (int i = 0; i < sampleCount; i++) {
			histogram.merge(collection.getRandom(), 1, Integer::sum);
		}
		
		histogram.forEach((key, count) -> {
			double expected = key / doubleSum;
			double actual = (double) count / sampleCount;
			if (Math.abs(expected - actual) > leniency) {
				Assertions.fail("Expected chance " + expected + " but got " + actual);
			}
		});
	}
	
	/**
	 * Generates multiple (more than 1) different {@link Double} values.
	 *
	 * @return 2 or more unique {@link Double} values
	 */
	@Contract(pure = true)
	private @NotNull Set<Double> generateRandomDoubles() {
		Set<Double> source = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			double value;
			do {
				value = ThreadLocalRandom.current().nextDouble();
			} while (source.contains(value));
			source.add(value);
		}
		return source;
	}
}
