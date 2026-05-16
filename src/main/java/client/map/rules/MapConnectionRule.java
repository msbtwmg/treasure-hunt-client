package client.map.rules;

import client.map.MapHelper;
import client.map.model.ClientHalfMap;
import client.map.model.EClientTerrain;

public class MapConnectionRule implements MapRule{

	@Override
	public void validate(ClientHalfMap map, RuleNotifications notifications) {
		EClientTerrain[][] grid = map.toTerrainGrid();

   
        if (!MapHelper.isConnected(grid)) {
            notifications.addError("Can not reach all accessible Fields!");
        }
	}
}
