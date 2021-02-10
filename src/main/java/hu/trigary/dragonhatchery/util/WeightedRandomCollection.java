package hu.trigary.dragonhatchery.util;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * An immutable collection which can retrieve random entries
 * from its pool of entry-weight pairs.
 * <br><br>
 * The source must not be an empty collection.
 * The entries can be null and the same entry can be present multiple times in the source.
 * The weights must be non-null positive (non-zero and non-negative) values.
 *
 * @param <E> the type of the entry
 */
public class WeightedRandomCollection<E> {
	private final NavigableMap<Double, E> entries = new TreeMap<>();
	private final double weightSum;
	
	/**
	 * Constructs a new collection from the specified source.
	 * All weights are guaranteed to be acquired (via the {@code weightExtractor}),
	 * but not all entries:
	 * {@code entryExtractor} might not be called for each element of {@code source}.
	 *
	 * @param source the source of the entries and weights
	 * @param entryExtractor the function which gets an entry from a source element
	 * @param weightExtractor the function which gets a weight from a source element
	 * @param <T> the type of the source elements
	 */
	public <T> WeightedRandomCollection(@NotNull Collection<T> source,
			@NotNull Function<T, E> entryExtractor,
			@NotNull Function<T, Double> weightExtractor) {
		Validate.isTrue(!source.isEmpty(), "Source must not be empty");
		double sum = 0;
		for (T value : source) {
			entries.put(sum, entryExtractor.apply(value));
			Double weight = weightExtractor.apply(value);
			Validate.isTrue(weight != null && weight > 0,
					"Weights must be non-null positive values");
			sum += weight;
		}
		weightSum = sum;
	}
	
	/**
	 * Gets a random entry from this collection, while taking the weights into consideration.
	 *
	 * @return the randomly selected entry
	 */
	@Contract(pure = true)
	public E getRandom() {
		return entries.floorEntry(ThreadLocalRandom.current().nextDouble(weightSum)).getValue();
	}
	
	/**
	 * Gets an unmodifiable collection containing all stored entries.
	 *
	 * @return the immutable collection of entries
	 */
	@Unmodifiable
	@Contract(pure = true)
	public @NotNull Collection<E> getEntries() {
		return Collections.unmodifiableCollection(entries.values());
	}
}
