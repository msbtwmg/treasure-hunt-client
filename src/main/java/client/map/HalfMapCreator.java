package client.map;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;
import client.map.placement.FortPlacementStrategy;
import exceptions.HalfMapGenerationException;

public class HalfMapCreator {

    private static final Logger logger = LoggerFactory.getLogger(HalfMapCreator.class);

    private final int width = 10;
    private final int height = 5;
    private final int total = width * height;

    private int fortPositionX = -1;
    private int fortPositionY = -1;

    private static final double MIN_MOUNTAIN = 0.12;
    private static final double MIN_GRASS = 0.48;
    private static final double MIN_WATER = 0.14;

    private final int minMountain = (int) Math.ceil(total * MIN_MOUNTAIN);
    private final int minWater    = (int) Math.ceil(total * MIN_WATER);
    private final int minGrass    = (int) Math.ceil(total * MIN_GRASS);

    private final Random random = new Random();
    private final int maxAttempts = 50;
    
    private final FortPlacementStrategy fortPlacementStrategy;
    
    public HalfMapCreator(FortPlacementStrategy fortPlacementStrategy) {
        this.fortPlacementStrategy = fortPlacementStrategy;
    }

   
    public List<ClientHalfMapNode> createHalfMap() throws HalfMapGenerationException {

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            resetFort();

            EClientTerrain[][] grid = createTerrainGrid();
            
            smoothTerrain(grid);
           
            
            if (!placeFortSmart(grid)) continue;

            if (!MapHelper.isConnected(grid)) continue;
            if (!MapHelper.isBorderAccessible(grid)) continue;
            if (!MapHelper.isTerrainValid(grid, minGrass, minWater, minMountain)) continue;
            
            if (evaluateMap(grid) > 25) continue;


            Map<Position, ClientHalfMapNode> nodeMap = createNodeMap(grid);

            logger.debug("Valid map created on attempt {}", attempt);

         
            return List.copyOf(nodeMap.values());
        }

        throw new HalfMapGenerationException("Could not create valid half map after " + maxAttempts + " attempts");
    }
    

    /**
     * Generating the randomized terrain grid + minimum distribution of grass water and mountain 
     * @return grid that represents the terrains
     */

    private EClientTerrain[][] createTerrainGrid() {

        List<EClientTerrain> fields = new ArrayList<>(total);

        int totalGrass = total - minMountain - minWater;

        for (int i = 0; i < minMountain; i++) fields.add(EClientTerrain.Mountain);
        for (int i = 0; i < minWater; i++)    fields.add(EClientTerrain.Water);
        for (int i = 0; i < totalGrass; i++)  fields.add(EClientTerrain.Grass);

        Collections.shuffle(fields, random);

        EClientTerrain[][] grid = new EClientTerrain[height][width];
        Iterator<EClientTerrain> it = fields.iterator();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = it.next();
            }
        }

        return grid;
    }
    
    //Soft clustering logic
    private void smoothTerrain(EClientTerrain[][] grid) {
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            int nx = Math.max(0, Math.min(width - 1, x + random.nextInt(3) - 1));
            int ny = Math.max(0, Math.min(height - 1, y + random.nextInt(3) - 1));

            EClientTerrain temp = grid[y][x];
            grid[y][x] = grid[ny][nx];
            grid[ny][nx] = temp;
        }
    }

    // checks how efficient the map is for movement
    private int evaluateMap(EClientTerrain[][] grid) {
        int penalty = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (grid[y][x] == EClientTerrain.Water) continue;

                int walkableNeighbors = 0;

                if (x > 0 && grid[y][x-1] != EClientTerrain.Water) walkableNeighbors++;
                if (x < width-1 && grid[y][x+1] != EClientTerrain.Water) walkableNeighbors++;
                if (y > 0 && grid[y-1][x] != EClientTerrain.Water) walkableNeighbors++;
                if (y < height-1 && grid[y+1][x] != EClientTerrain.Water) walkableNeighbors++;

                // penalty  dead ends
                if (walkableNeighbors <= 1) penalty += 5;

            }
        }

        return penalty;
    }

    
   
    private void resetFort() {
        fortPositionX = -1;
        fortPositionY = -1;
    }

    private boolean placeFortSmart(EClientTerrain[][] grid) {
        int[] chosen = fortPlacementStrategy.chooseFortPosition(grid);

        if (chosen == null) {
            logger.debug("No valid fort positions found");
            return false;
        }

        fortPositionX = chosen[0];
        fortPositionY = chosen[1];

        logger.debug("Placed fort at ({}, {})", fortPositionX, fortPositionY);
        return true;
    }


 
    private Map<Position, ClientHalfMapNode> createNodeMap(EClientTerrain[][] grid) {

        Map<Position, ClientHalfMapNode> map = new HashMap<>(total);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                boolean myFort = (x == fortPositionX && y == fortPositionY);
                Position pos = new Position(x, y);
                map.put(pos,
                        new ClientHalfMapNode(x, y, myFort, false, grid[y][x])
                );
            }
        }

        return map;
    }



    public record Position(int x, int y) {}
}