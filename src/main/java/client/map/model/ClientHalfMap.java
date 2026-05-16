package client.map.model;

import java.util.Set;

public class ClientHalfMap {

	private  Set<ClientHalfMapNode> nodes;
	 

	public ClientHalfMap(Set<ClientHalfMapNode> nodes) {
		this.nodes = nodes;
	}
	
	public Set<ClientHalfMapNode> getMapNodes() {
		return nodes;
	}
	
	// Zwiebelschale
	public EClientTerrain[][] toTerrainGrid() {
	    EClientTerrain[][] grid = new EClientTerrain[5][10];

	    for (ClientHalfMapNode node : nodes) {
	        grid[node.getY()][node.getX()] = node.getTerrain();
	    }

	    return grid;
	}
}
