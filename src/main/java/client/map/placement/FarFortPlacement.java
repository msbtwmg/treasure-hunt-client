package client.map.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import client.map.model.EClientTerrain;
public class FarFortPlacement implements FortPlacementStrategy {

    private final Random random = new Random();
    private final FortQuarter quarter;

    public FarFortPlacement(FortQuarter quarter) {
        this.quarter = quarter;
    }

    @Override
    public int[] chooseFortPosition(EClientTerrain[][] grid) {
        List<int[]> candidates = new ArrayList<>();

        int width = grid[0].length;
        int height = grid.length;

        int startX = switch (quarter) {
            case TOP_LEFT, BOTTOM_LEFT -> 0;
            case TOP_RIGHT, BOTTOM_RIGHT -> width / 2;
        };

        int endX = switch (quarter) {
            case TOP_LEFT, BOTTOM_LEFT -> width / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> width;
        };

        int startY = switch (quarter) {
            case TOP_LEFT, TOP_RIGHT -> 0;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> height / 2;
        };

        int endY = switch (quarter) {
            case TOP_LEFT, TOP_RIGHT -> (height / 2) + 1;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> height;
        };

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if (grid[y][x] == EClientTerrain.Grass) {
                    candidates.add(new int[]{x, y});
                }
            }
        }

        if (candidates.isEmpty()) return null;

        return candidates.get(random.nextInt(candidates.size()));
    }
}
