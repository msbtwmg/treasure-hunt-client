package client.map.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

        map = new ClientFullMap(Set.of( start,
                node(1, 0, EClientTerrain.Grass),
                node(0, 1, EClientTerrain.Water)));
    }

    @Test
    public void isMoveSafe_withWaterDown_shouldReturnFalse() {
        boolean result = map.isMoveSafe(start, EClientMove.Down); 

        assertFalse(result);
    }
    
    @Test
    public void isMoveSafe_withLeftOutsideMap_shouldReturnFalse() {
        boolean result = map.isMoveSafe(start, EClientMove.Left); 

        assertFalse(result);
    }
    
    
    @Test
    public void isMoveSafe_withRightGrass_shouldReturnTrue() {
    	
        boolean result = map.isMoveSafe(start, EClientMove.Right);

        assertTrue(result);
    }


    @Test
    public void getNode_shouldReturnCorrectNode() {
        ClientFullMapNode result = map.getNode(1, 0);

        assertEquals(1, result.getX());
        assertEquals(0, result.getY());
        assertEquals(EClientTerrain.Grass, result.getTerrain());
    }
 
    @Test
    public void getNode_withUnknownPosition_shouldReturnNull() {
        ClientFullMapNode result = map.getNode(9, 9);

        assertNull(result);
    }
    
    @Test
    public void hasNodes_shouldReturnFalseForEmptyMap() {
        ClientFullMap emptyMap = new ClientFullMap(Set.of());

        assertFalse(emptyMap.hasNodes());
        
    }
    @Test
    public void isEnemyHalf_withoutMyFort_shouldReturnFalse() {
        ClientFullMap map = new ClientFullMap(Set.of(
                node(0, 0, EClientTerrain.Grass),
                node(1, 0, EClientTerrain.Grass)));

        boolean result = map.isEnemyHalf(1, 0);

        assertFalse(result);
    }
    
    @Test
    public void distanceToMapMiddle_horizontalMap_shouldReturnDistance() {
        ClientFullMap map = new ClientFullMap(Set.of(
                node(0, 0, EClientTerrain.Grass),
                node(4, 0, EClientTerrain.Grass)));

        int result = map.distanceToMapMiddle(4, 0);

        assertEquals(2, result);
    }

	@Test
	public void isPassable_shouldReturnFalseForwWater() {
	    ClientFullMapNode water = map.getNode(0, 1);
	
	    assertFalse(map.isPassable(water));
	}
	    
	@Test
	public void mapValues_shouldReturnCorrectValues() {
	    ClientFullMap map = new ClientFullMap(Set.of(
	            node(0, 0, EClientTerrain.Grass),
	            node(4, 0, EClientTerrain.Grass),
	            node(0, 2, EClientTerrain.Grass) ));

	    assertEquals(0, map.getMinX());
	    assertEquals(4, map.getMaxX());
	    assertEquals(0, map.getMinY());
	    assertEquals(2, map.getMaxY());

	    assertEquals(5, map.getWidth());
	    assertEquals(3, map.getHeight());
	    assertTrue(map.isHorizontalMap());
	}
	
    
    private ClientFullMapNode node(int x, int y, EClientTerrain terrain) {
        return new ClientFullMapNode(x, y, terrain,
                EClientFortState.NoOrUnknownFortState,
                EClientTreasureState.NoOrUnknownTreasureState,
                EClientPositionState.NoPlayerPresent
        );
    }
}
