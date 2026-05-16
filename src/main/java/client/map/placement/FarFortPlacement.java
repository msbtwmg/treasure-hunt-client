package client.map.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import client.map.model.EClientTerrain;

public class FarFortPlacement implements FortPlacementStrategy {

    private final Random random = new Random();
    private final int preferredStartX;
    private final int preferredStartY;

    public FarFortPlacement(int preferredStartX, int preferredStartY) {
        this.preferredStartX = preferredStartX;
        this.preferredStartY = preferredStartY;
    }

    public int[] chooseFortPosition(EClientTerrain[][] grid) {
        List<int[]> candidates = new ArrayList<>();

        for (int y = preferredStartY; y < preferredStartY + 3; y++) {
            for (int x = preferredStartX; x < preferredStartX + 5; x++) {
                if (grid[y][x] == EClientTerrain.Grass) {
                    candidates.add(new int[]{x, y});
                }
            }
        }

        if (candidates.isEmpty()) return null;

        return candidates.get(random.nextInt(candidates.size()));
    }
}
