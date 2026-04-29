package client.map.model;

import java.util.Set;

public class ClientFullMap {

	private  Set<ClientFullMapNode> nodes;
	 

	public ClientFullMap(Set<ClientFullMapNode> nodes) {
		this.nodes = nodes;
	}
	
	public Set<ClientFullMapNode> getMapNodes() {
		return nodes;
	}
	
}
