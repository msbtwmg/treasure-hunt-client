package client.map;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;

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

    

    public List<ClientHalfMapNode> createHalfMap() {

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

        throw new RuntimeException("Could not create valid half map after " + maxAttempts + " attempts");
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

        List<int[]> candidates = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (grid[y][x] != EClientTerrain.Grass) continue;

                int score = scoreFortPosition(grid, x, y);
                if (score >= 0) {
                    candidates.add(new int[]{x, y, score});
                }
            }
        }

        if (candidates.isEmpty()) {
            logger.debug("No valid fort positions found");
            return false;
        }

        
        candidates.sort((a, b) -> Integer.compare(b[2], a[2]));

        // Select randomly from top 30 % best positions
        int cutoff = Math.max(1, candidates.size() * 3 / 10);
        int[] chosen = candidates.get(random.nextInt(cutoff));

        fortPositionX = chosen[0];
        fortPositionY = chosen[1];

        logger.debug("Placed fort at ({}, {})", fortPositionX, fortPositionY);
        return true;
    }

    /**
     * Rates how good a fort position is based on the terrains near it
     * Avoiding borders and water, prefer grass around
     * @param grid terrain grid
     * @param x x-coordinate
     * @param y y-coordinate
     * @return score , when it's higher its a better position
     * 
     */
    private int scoreFortPosition(EClientTerrain[][] grid, int x, int y) {

        int score = 0;

        // Prefer central positions
        int centerX = width / 2;
        int centerY = height / 2;
        int distToCenter = Math.abs(x - centerX) + Math.abs(y - centerY);
        score -= distToCenter;

        // Avoid placing fort on border 
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            score -= 5;
        }

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int grassNeighbors = 0;

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;

            if (grid[ny][nx] == EClientTerrain.Grass) grassNeighbors++;
            if (grid[ny][nx] == EClientTerrain.Water) score -= 4;
        }

        score += grassNeighbors * 2;

        return score;
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