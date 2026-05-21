package client.map.model;

import java.util.Set;

import client.AI.PathFinder.Position;

public class ClientFullMap {

	private  Set<ClientFullMapNode> nodes;
	 

	public ClientFullMap(Set<ClientFullMapNode> nodes) {
		this.nodes = nodes;
	}
	
	public Set<ClientFullMapNode> getMapNodes() {
		return nodes;
	}
	
	public int getWidth() {
	    return nodes.stream().mapToInt(ClientFullMapNode::getX).max().orElse(0)
	         - nodes.stream().mapToInt(ClientFullMapNode::getX).min().orElse(0)
	         + 1;
	}

	public int getHeight() {
	    return nodes.stream().mapToInt(ClientFullMapNode::getY).max().orElse(0)
	         - nodes.stream().mapToInt(ClientFullMapNode::getY).min().orElse(0)
	         + 1;
	}
	
	// TODO: from Pathfinder -> here. check Aufrufe and change them
	public boolean isEnemyHalf(int x, int y) {
        ClientFullMapNode fort = findMyFort();

        if (fort == null) {
            return false;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (ClientFullMapNode node : nodes) {
            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());
            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }

        if (isHorizontalMap(minX, maxX, minY, maxY)) {
            double middleX = (minX + maxX) / 2.0;
            boolean myFortIsLeft = fort.getX() < middleX;

            return myFortIsLeft ? x > middleX : x < middleX;
        } else {
            double middleY = (minY + maxY) / 2.0;
            boolean myFortIsTop = fort.getY() < middleY;

            return myFortIsTop ? y > middleY : y < middleY;
        }
    }

    private boolean isHorizontalMap(int minX, int maxX, int minY, int maxY) {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return width > height;
    }

    private ClientFullMapNode findMyFort() {
        for (ClientFullMapNode node : nodes) {
            if (node.getFortState() == EClientFortState.MyFortPresent) {
                return node;
            }
        }

        return null;
    }

	
}
