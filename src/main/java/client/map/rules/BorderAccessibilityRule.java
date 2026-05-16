package client.map.rules;

import client.map.MapHelper;
import client.map.model.ClientHalfMap;
import client.map.model.EClientTerrain;

public class BorderAccessibilityRule implements MapRule {

    @Override
    public void validate(ClientHalfMap map, RuleNotifications notifications) {
        EClientTerrain[][] grid = map.toTerrainGrid();

        if (!MapHelper.isBorderAccessible(grid)) {
            notifications.addError("Border accessibility rules violated!");
        }
    }
}