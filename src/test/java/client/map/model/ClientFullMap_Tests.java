package client.map.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.game.model.EClientMove;

public class ClientFullMap_Tests {
	

    private ClientFullMapNode start;
    private ClientFullMap map;

    @BeforeEach
    public void setUp() {
        start = node(0, 0, EClientTerrain.Grass);

        map = new ClientFullMap(Set.of(
                start,
                node(1, 0, EClientTerrain.Grass),
                node(0, 1, EClientTerrain.Water)
        ));
    }

    @Test
    public void isMoveSafe_shouldReturnFalseWater() {
        boolean result = map.isMoveSafe(start, EClientMove.Down); // Down from start is water 

        assertFalse(result);
    }
    
    @Test
    public void isMoveSafe_shouldReturnFalseOutMap() {
        boolean result = map.isMoveSafe(start, EClientMove.Left); // Left from the start is outside map

        assertFalse(result);
    }

    @Test
    public void getNode_shouldReturnCorrectNode() {
        ClientFullMapNode result = map.getNode(1, 0);

        assertEquals(1, result.getX());
        assertEquals(0, result.getY());
        
        assertEquals(EClientTerrain.Grass, result.getTerrain());
    }
    
    private ClientFullMapNode node(int x, int y, EClientTerrain terrain) {
        return new ClientFullMapNode(x, y, terrain,
                EClientFortState.NoOrUnknownFortState,
                EClientTreasureState.NoOrUnknownTreasureState,
                EClientPositionState.NoPlayerPresent
        );
    }
}
