package client.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import client.AI.Position;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;

public class MapHelper_Tests {

    @Test
    public void findMyPosition_withNullMap_shouldReturnNull() {

        ClientFullMapNode result = MapHelper.findMyPosition(null);

        assertNull(result);
    }

    @Test
    public void findMyPosition_withEmptyMap_shouldReturnNull() {
        Map<Position, ClientFullMapNode> nodes = Map.of();

        ClientFullMapNode result = MapHelper.findMyPosition(nodes);

        assertNull(result);
    }

    @Test
    public void findMyPosition_withMyPlayerPosition_shouldReturnNode() {
    	Position myPosition = new Position(1, 2);
    	Position otherPosition = new Position(2, 2);

    	ClientFullMapNode myNode = node(1, 2, EClientPositionState.MyPlayerPosition);
    	ClientFullMapNode otherNode = node(2, 2, EClientPositionState.NoPlayerPresent);

    	Map<Position, ClientFullMapNode> nodes = Map.of(
    			myPosition, myNode,
    			otherPosition, otherNode);

    	ClientFullMapNode result = MapHelper.findMyPosition(nodes);

    	assertEquals(myNode, result);
    }

    @Test
    public void findMyPosition_withBothPlayerPosition_shouldReturnNode() {
    	Position position = new Position(3, 4);

    	ClientFullMapNode bothPlayerNode = node(3, 4, EClientPositionState.BothPlayerPosition);

    	Map<Position, ClientFullMapNode> nodes = Map.of(
    			position, bothPlayerNode);

    	ClientFullMapNode result = MapHelper.findMyPosition(nodes);

    	assertEquals(bothPlayerNode, result);
    }

    @Test
    public void findMyPosition_withoutPlayerPosition_shouldReturnNull() {
    	Position firstPosition = new Position(0, 0);
    	Position secondPosition = new Position(1, 0) ;

    	ClientFullMapNode firstNode = node(0, 0, EClientPositionState.NoPlayerPresent);
    	ClientFullMapNode secondNode = node(1, 0, EClientPositionState.EnemyPlayerPosition);

    	Map<Position, ClientFullMapNode> nodes = Map.of(
    			firstPosition, firstNode,
    			secondPosition, secondNode);

    	ClientFullMapNode result = MapHelper.findMyPosition(nodes);

    	assertNull(result);
    }
    
    
    @Test
    public void isFortPositionValid_withGrassTile_shouldReturnTrue() {
        EClientTerrain[][] grid = {
        		{ EClientTerrain.Grass, EClientTerrain.Water },
        		
                { EClientTerrain.Mountain, EClientTerrain.Grass }};

        boolean result = MapHelper.isFortPositionValid(grid, 0, 0);

        assertTrue(result);
    }

    @Test
    public void isFortPositionValid_withWater_shouldReturnFalse() {
        EClientTerrain[][] grid = { { EClientTerrain.Grass, EClientTerrain.Water }};

        boolean result = MapHelper.isFortPositionValid(grid, 1, 0);

        assertFalse(result);
    }

    @Test
    public void isFortPositionValid_withMountain_shouldReturnFalse() {
        EClientTerrain[][] grid = {{ EClientTerrain.Mountain}};

        boolean result = MapHelper.isFortPositionValid(grid, 0, 0);
        assertFalse(result);
    }

    @Test
    public void isFortPositionValid_withNegativePosition_shouldReturnFalse() {
        EClientTerrain[][] grid = {{ EClientTerrain.Grass }};

        boolean result = MapHelper.isFortPositionValid(grid, -1, 0);

        assertFalse(result);
    }

    @Test
    public void isFortPositionValid_withPositionOutsideGrid_shouldReturnFalse() {
        EClientTerrain[][] grid = {{ EClientTerrain.Grass }};
        
        boolean result = MapHelper.isFortPositionValid(grid, 2, 0);

        assertFalse(result);
    }

    private ClientFullMapNode node(int x, int y, EClientPositionState positionState) {
        return new ClientFullMapNode(x, y,
                EClientTerrain.Grass,
                EClientFortState.NoOrUnknownFortState,
                EClientTreasureState.NoOrUnknownTreasureState,
                positionState
        );
    }
}