package client.map.rules;

import client.map.MapHelper;
import client.map.model.ClientHalfMap;
import client.map.model.EClientTerrain;

public class TerrainRule implements MapRule {

    private final int minGrass;
    private final int minWater;
    private final int minMountain;

    public TerrainRule(int minGrass, int minWater, int minMountain) {
        this.minGrass = minGrass;
        this.minWater = minWater;
        this.minMountain = minMountain;
    }

    @Override
    public void validate(ClientHalfMap map, RuleNotifications notifications) {
        EClientTerrain[][] grid = map.toTerrainGrid();

        if (!MapHelper.isTerrainValid(grid, minGrass, minWater, minMountain)) {
            notifications.addError("Terrain distribution is invalid!");
        }
    }
}