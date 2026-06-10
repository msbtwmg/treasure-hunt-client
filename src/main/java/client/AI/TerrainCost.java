package client.AI;

import client.map.model.ClientFullMapNode;

public class TerrainCost {

	public int getCost(ClientFullMapNode node, boolean hasTreasure) {
		return switch (node.getTerrain()) {
			case Grass -> hasTreasure ? 1 : 3;
			case Mountain -> hasTreasure ? 4 : 1;
			case Water -> Integer.MAX_VALUE / 4;
		};
	}
}