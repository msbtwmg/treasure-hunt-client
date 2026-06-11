package client.AI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;

public class DijkstraSearch_Tests {
	
	@Test
	public void findPath_withSameStartAndTarget_shouldBeEmpty() {

	    ClientFullMap map = new ClientFullMap(Set.of(node(0, 0, EClientTerrain.Grass)));

	    DijkstraSearch search = new DijkstraSearch(map, new PathMemory());
	    Position start = new Position(0, 0);

	    List<Position> result = search.findPath(start, start, false); // Already at target position

	    assertTrue(result.isEmpty());
	}
	
	@Test
	public void findPath_withWaterTarget_shouldBeEmpty() {
	    ClientFullMap map = new ClientFullMap(Set.of(node(0, 0, EClientTerrain.Grass),
	            node(1, 0, EClientTerrain.Water)));

	    DijkstraSearch search = new DijkstraSearch(map, new PathMemory());
	    
		Position start = new Position(0, 0);
		Position target = new Position(1, 0);

		List<Position> result = search.findPath(start, target, false);

	    // Water cant be crossed
	    assertTrue(result.isEmpty());
	}
	
	@Test
	public void findPath_withValidNeighbor_shouldReturnOneSTep() {
	    ClientFullMap map = new ClientFullMap(Set.of(node(0, 0, EClientTerrain.Grass),
	            node(1, 0, EClientTerrain.Grass)));

	    DijkstraSearch search = new DijkstraSearch(map, new PathMemory());
	    Position start = new Position(0, 0);
	    Position target =  new Position(1, 0);

	    List<Position> result = search.findPath(start, target , false);

	    assertEquals(1, result.size());
	    assertEquals(target, result.get(0));
	}
	
	private ClientFullMapNode node(int x, int y, EClientTerrain terrain) {
	    return new ClientFullMapNode(
	            x,
	            y,
	            terrain,
	            EClientFortState.NoOrUnknownFortState,
	            EClientTreasureState.NoOrUnknownTreasureState,
	            EClientPositionState.NoPlayerPresent
	    );
	}
}
