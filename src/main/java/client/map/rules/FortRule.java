package client.map.rules;

import client.map.MapHelper;
import client.map.model.ClientHalfMap;
import client.map.model.EClientTerrain;

public class FortRule {
    private final int fortX;
    private final int fortY;
    
	 public FortRule(int fortX, int fortY) {
	        this.fortX = fortX;
	        this.fortY = fortY;
	    }
	 
	 
	public void validate(ClientHalfMap map, RuleNotifications notifications) {
	    EClientTerrain[][] grid = map.toTerrainGrid();

	    if (!MapHelper.isFortPositionValid(grid, fortX, fortY)) {
	        notifications.addError("Fort is placed on invalid terrain!");
	    }
	}
}
