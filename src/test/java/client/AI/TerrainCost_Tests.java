package client.AI;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;

public class TerrainCost_Tests {

    private final TerrainCost terrainCost = new TerrainCost();

    @Test
    public void getCost_shouldReturnGrassCostBeforeTreasureFound() {
        ClientFullMapNode grass = node(EClientTerrain.Grass);

        //Before collecting the treasure grass is more expensive
        assertEquals(3, terrainCost.getCost(grass,false)); 
    }

    @Test
    public void getCost_shouldReturnGrassCostAfterTreasureFound() {
        ClientFullMapNode grass = node(EClientTerrain.Grass);
        // After collecting treasure  grass is cheaper
        assertEquals(1,terrainCost.getCost(grass, true));
    }

    @Test
    public void getCost_shouldReturnMountainCostBeforeTreasureFound() {
        ClientFullMapNode mountain = node(EClientTerrain.Mountain);

        // Mountains preferred before finding the treasure
        assertEquals(1, terrainCost.getCost(mountain, false)) ;
    }

    @Test
    public void getCost_shouldReturnMountainCostAfterTreasureFound() {
        ClientFullMapNode mountain = node(EClientTerrain.Mountain);

        // Mountains more expensive after getting the treasure
        assertEquals(4,terrainCost.getCost(mountain, true));
    }

    private ClientFullMapNode node(EClientTerrain terrain) {
        return new ClientFullMapNode(0, 0, terrain,
                EClientFortState.NoOrUnknownFortState,
                EClientTreasureState.NoOrUnknownTreasureState,
                EClientPositionState.NoPlayerPresent
        );
    }
}