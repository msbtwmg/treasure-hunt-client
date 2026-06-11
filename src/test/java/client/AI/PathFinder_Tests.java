package client.AI;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.game.model.EClientMove;
import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;

public class PathFinder_Tests {
    private PathFinder pathFinder;

	@BeforeEach
    public void setUp() {
        pathFinder = new PathFinder();
    }
	
	// decide 
	
	@Test
    public void decideNextMove_withEmptyMap_shouldReturnRight() {
        ClientFullMap map = new ClientFullMap(Set.of());

        pathFinder.setMap(map);

        EClientMove result = pathFinder.decideNextMove(false);

        assertEquals(EClientMove.Right, result);
    }
	
	@Test
	public void decideNextMove_withoutMap_shouldReturnRight() {
	    EClientMove result = pathFinder.decideNextMove(false);

	    assertEquals(EClientMove.Right, result);
	}
	
	
	@Test
	public void fallbackMove_withGrassRight_shouldReturnRight() {

	    ClientFullMapNode me = node(0, 0, EClientTerrain.Grass,
	            EClientPositionState.MyPlayerPosition);

	    ClientFullMap map = new ClientFullMap(Set.of(
	    		me,
	            node(1, 0, EClientTerrain.Grass, EClientPositionState.NoPlayerPresent)));
	    pathFinder.setMap(map);
	    
	    EClientMove result = pathFinder.fallbackMove(me, false);

	    assertEquals(EClientMove.Right, result);
	}
	
	@Test
	public void fallbackMove_withWaterRightGrassDown_shouldGoDown() {
		
	    ClientFullMapNode me = node(0, 0, EClientTerrain.Grass,
	            EClientPositionState.MyPlayerPosition);

	    ClientFullMap map = new ClientFullMap(Set.of(me,
	            node(1, 0, EClientTerrain.Water, EClientPositionState.NoPlayerPresent),
	            node(0, 1, EClientTerrain.Grass, EClientPositionState.NoPlayerPresent)));

	    pathFinder.setMap(map);

	    EClientMove result = pathFinder.fallbackMove(me, false);

	    assertEquals(EClientMove.Down, result);
	}
	

	@Test
	public void fallbackMove_withoutNeighbors_shouldReturnRight() {
		ClientFullMapNode me = node(0, 0, EClientTerrain.Grass,
				EClientPositionState.MyPlayerPosition);

		ClientFullMap map = new ClientFullMap(Set.of(me));
		
		pathFinder.setMap(map);

		EClientMove result = pathFinder.fallbackMove(me, false);

		assertEquals(EClientMove.Right, result);
	}
	
	
	@Test
	public void decideNextMove_withPlayerOnMap_shouldReturnMove() {

	    ClientFullMap map = new ClientFullMap(Set.of(
	            node(0, 0, EClientTerrain.Grass,EClientPositionState.MyPlayerPosition),
	            node(1, 0, EClientTerrain.Grass, EClientPositionState.NoPlayerPresent)));

	    pathFinder.setMap(map);

	    EClientMove result = pathFinder.decideNextMove(false);

	    assertEquals(EClientMove.Right, result);
	}
	
	
	// HELPER
	private ClientFullMapNode node(int x, int y, EClientTerrain terrain,
	        EClientPositionState positionState) {
		    return new ClientFullMapNode(x, y, terrain,
		            EClientFortState.NoOrUnknownFortState,
		            EClientTreasureState.NoOrUnknownTreasureState,
		            positionState
		    );
	}
	


}
