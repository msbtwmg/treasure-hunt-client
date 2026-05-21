package client.map.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import client.map.model.EClientTerrain;
public class DefaultFortPlacement implements FortPlacementStrategy {

    private final Random random = new Random();

    @Override
    public int[] chooseFortPosition(EClientTerrain[][] grid) {
        List<int[]> candidates = new ArrayList<>();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                if (grid[y][x] != EClientTerrain.Grass) continue;

                int score = scoreFortPosition(grid, x, y);
                if (score >= 0) {
                    candidates.add(new int[]{x, y, score});
                }
            }
        }

        if (candidates.isEmpty()) return null;

        candidates.sort((a, b) -> Integer.compare(b[2], a[2]));

        int cutoff = Math.max(1, candidates.size() * 3 / 10);
        return candidates.get(random.nextInt(cutoff));
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
        int width = grid[0].length;
        int height = grid.length;

        int score = 0;

        int centerX = width / 2;
        int centerY = height / 2;
        int distToCenter = Math.abs(x - centerX) + Math.abs(y - centerY);
        score -= distToCenter;

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
}