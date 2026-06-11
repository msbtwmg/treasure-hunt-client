package client.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;
import client.map.placement.DefaultFortPlacement;
import exceptions.HalfMapGenerationException;
public class HalfMapCreator_Tests {

    private HalfMapCreator creator;
    List<ClientHalfMapNode> result;
    @BeforeEach
    public void setUp() throws HalfMapGenerationException {
        creator = new HalfMapCreator(new DefaultFortPlacement());
		result = creator.createHalfMap();

    }

    @Test
    public void createHalfMap_shouldCreate50Nodes() {
    
        assertEquals(50, result.size());
    }

    @Test
    public void createHalfMap_shouldCreateOneFort() {
      
    
        long fortCount = result.stream()
                .filter(node -> node.myFortPresent())
                .count();

        assertEquals(1, fortCount);
    }

    @Test
    public void createHalfMap_shouldCreateEnoughGrass() {
      
       
        long grassCount = result.stream()
                .filter(node -> node.getTerrain() == EClientTerrain.Grass)
                .count();

        assertTrue(grassCount >= 24);
    }

    @Test
    public void createHalfMap_shouldCreateEnoughWater() {
    
     
        long waterCount = result.stream()
                .filter(node -> node.getTerrain() == EClientTerrain.Water)
                .count();

        assertTrue(waterCount >= 7);
    }

    @Test
    public void createHalfMap_shouldCreateEnoughMountains() {
      
        long mountainCount = result.stream()
                .filter(node -> node.getTerrain() == EClientTerrain.Mountain)
                .count();

        assertTrue(mountainCount >= 5);
    }

    @RepeatedTest(10)
    public void createHalfMap_repeated_shouldAlwaysCreateValidSize() {
      

       
        assertEquals(50, result.size());
    }
}