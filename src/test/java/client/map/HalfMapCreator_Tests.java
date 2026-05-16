package client.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;

public class HalfMapCreator_Tests {

    private HalfMapCreator creator;

    @BeforeEach
    public void setUp() {
        creator = new HalfMapCreator();
    }

    @Test
    public void createHalfMap_shouldCreate50Nodes() {
        // Arrange

        // Act
        List<ClientHalfMapNode> result = creator.createHalfMap();

        // Assert
        assertEquals(50, result.size());
    }

    @Test
    public void createHalfMap_shouldCreateOneFort() {
        // Arrange

        // Act
        List<ClientHalfMapNode> result = creator.createHalfMap();

        // Assert
        long fortCount = result.stream()
                .filter(node -> node.myFortPresent())
                .count();

        assertEquals(1, fortCount);
    }

    @Test
    public void createHalfMap_shouldCreateEnoughGrass() {
        // Arrange

        // Act
        List<ClientHalfMapNode> result = creator.createHalfMap();

        // Assert
        long grassCount = result.stream()
                .filter(node -> node.getTerrain() == EClientTerrain.Grass)
                .count();

        assertTrue(grassCount >= 24);
    }

    @Test
    public void createHalfMap_shouldCreateEnoughWater() {
        // Arrange

        // Act
        List<ClientHalfMapNode> result = creator.createHalfMap();

        // Assert
        long waterCount = result.stream()
                .filter(node -> node.getTerrain() == EClientTerrain.Water)
                .count();

        assertTrue(waterCount >= 7);
    }

    @Test
    public void createHalfMap_shouldCreateEnoughMountains() {
        // Arrange

        // Act
        List<ClientHalfMapNode> result = creator.createHalfMap();

        // Assert
        long mountainCount = result.stream()
                .filter(node -> node.getTerrain() == EClientTerrain.Mountain)
                .count();

        assertTrue(mountainCount >= 5);
    }

    @RepeatedTest(10)
    public void createHalfMap_repeated_shouldAlwaysCreateValidSize() {
      
        List<ClientHalfMapNode> result = creator.createHalfMap();

       
        assertEquals(50, result.size());
    }
}