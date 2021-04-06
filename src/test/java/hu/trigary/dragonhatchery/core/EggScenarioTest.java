package hu.trigary.dragonhatchery.core;

import org.bukkit.boss.DragonBattle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests the {@link EggScenario} class.
 */
public class EggScenarioTest {
	
	/**
	 * Tests that {@link EggScenario#getMatching(DragonBattle)}
	 * correctly returns {@link EggScenario#FIRST} when it should.
	 */
	@Test
	void testMatchingFirst() {
		DragonBattle battle = Mockito.mock(DragonBattle.class);
		Mockito.when(battle.hasBeenPreviouslyKilled()).thenReturn(false);
		Assertions.assertEquals(EggScenario.FIRST, EggScenario.getMatching(battle));
	}
	
	/**
	 * Tests that {@link EggScenario#getMatching(DragonBattle)}
	 * correctly returns {@link EggScenario#SUBSEQUENT} when it should.
	 */
	@Test
	void testMatchingSubsequent() {
		DragonBattle battle = Mockito.mock(DragonBattle.class);
		Mockito.when(battle.hasBeenPreviouslyKilled()).thenReturn(true);
		Assertions.assertEquals(EggScenario.SUBSEQUENT, EggScenario.getMatching(battle));
	}
}
