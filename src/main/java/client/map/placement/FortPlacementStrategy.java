package client.map.placement;

import client.map.model.EClientTerrain;

public interface FortPlacementStrategy {
    int[] chooseFortPosition(EClientTerrain[][] grid);

}
