package exceptions;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import client.map.HalfMapCreator;
import client.map.model.EClientTerrain;
import client.map.placement.FortPlacementStrategy;

public class HalfMapException_Tests {
	
@Test
public void createHalfMap_withoutValidFort_shouldThrowException() {
	FortPlacementStrategy strategy = new FortPlacementStrategy() {
		
		@Override 
		public int[] chooseFortPosition(EClientTerrain[][] grid) { 
			return null; 
		}
	
	};
	
	HalfMapCreator creator = new HalfMapCreator(strategy); 
	HalfMapGenerationException exception = assertThrows(HalfMapGenerationException.class, () -> { 
		creator.createHalfMap(); }); 
		assertTrue(exception.getMessage().contains("Could not create valid half map"));
	}
	
}
