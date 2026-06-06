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
	
	@Test
    public void decideNextMove_withEmptyMap_shouldReturnRight() {
        ClientFullMap map = new ClientFullMap(Set.of());

        pathFinder.setMap(map);

        EClientMove result = pathFinder.decideNextMove(false);

        assertEquals(EClientMove.Right, result);
    }
	


}
