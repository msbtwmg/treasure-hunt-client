package client.map.model;

import java.util.Set;

import client.game.model.EClientMove;

public class ClientFullMap {

	private final Set<ClientFullMapNode> nodes;
	 
	
	public ClientFullMap(Set<ClientFullMapNode> nodes) {
		this.nodes = nodes;
		
	}
	
	public Set<ClientFullMapNode> getMapNodes() {
		return nodes;
	}
	public ClientFullMapNode getNode(int x, int y) {
        for (ClientFullMapNode node : nodes) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }

        return null;
    }
	

    public int getMinX() {
        return nodes.stream().mapToInt(ClientFullMapNode::getX).min().orElse(0);
    }
    
    public int getMaxX() {
        return nodes.stream().mapToInt(ClientFullMapNode::getX).max().orElse(0);
    }

    public int getMinY() {
        return nodes.stream().mapToInt(ClientFullMapNode::getY).min().orElse(0);
    }

    public int getMaxY() {
        return nodes.stream().mapToInt(ClientFullMapNode::getY).max().orElse(0);
    }

    public int getWidth() {
        return getMaxX() - getMinX() + 1;
    }

    public int getHeight() {
        return getMaxY() - getMinY() + 1;
    }
    
    public boolean isHorizontalMap() {
        return getWidth() > getHeight();
}
    
	
    public boolean isEnemyHalf(int x, int y) {
        ClientFullMapNode fort = findMyFort();

        if (fort == null) {
            return false;
        }

        if (isHorizontalMap()) {
            int middleX = getMinX() + getWidth() / 2;
            boolean myFortIsLeft = fort.getX() < middleX;

            return myFortIsLeft ? x >= middleX : x < middleX;
        }

        int middleY = getMinY() + getHeight() / 2;
        boolean myFortIsTop = fort.getY() < middleY;

        return myFortIsTop ? y >= middleY : y < middleY;
    }
	
    private ClientFullMapNode findMyFort() {
        for (ClientFullMapNode node : nodes) {
            if (node.getFortState() == EClientFortState.MyFortPresent) {
                return node;
            }
        }

        return null;
    }
    
    public int distanceToMapMiddle(int x, int y) {
        if (isHorizontalMap()) {
            double middleX = (getMinX() + getMaxX()) / 2.0;
            return (int) Math.abs(x - middleX);
        }

        double middleY = (getMinY() + getMaxY()) / 2.0;
        return (int) Math.abs(y - middleY);
    }

    public boolean isMoveSafe(ClientFullMapNode node, EClientMove move) {
        int nx = node.getX();
        int ny = node.getY();

        switch (move) {
            case Right -> nx++;
            case Left  -> nx--;
            case Down  -> ny++;
            case Up    -> ny--;
        }

        ClientFullMapNode target = getNode(nx, ny);

        return target != null && target.getTerrain() != EClientTerrain.Water;
    }
    
	public ClientFullMapNode findMyPosition() {
		for (ClientFullMapNode node : getMapNodes()) {
			var position = node.getPlayerPositionState();

			if (position == EClientPositionState.MyPlayerPosition ||
				position == EClientPositionState.BothPlayerPosition) {
				return node;
			}
		}

		return null;
	}
	
	public ClientFullMapNode findEnemyPosition() {
		for (ClientFullMapNode node : nodes) {
			EClientPositionState position = node.getPlayerPositionState();

			if (position == EClientPositionState.EnemyPlayerPosition ||
				position == EClientPositionState.BothPlayerPosition) {
				return node;
			}
		}

		return null;
	}
	public ClientFullMapNode findTreasure() {
		for (ClientFullMapNode node : getMapNodes()) {
			if (node.getTreasureState() == EClientTreasureState.MyTreasureIsPresent) {
				return node;
			}
		}

		return null;
	}
	
	public ClientFullMapNode findEnemyFort() {
		for (ClientFullMapNode node :getMapNodes()) {
			if (node.getFortState() == EClientFortState.EnemyFortPresent) {
				return node;
			}
		}

		return null;
	}
	
	public boolean isPassable(ClientFullMapNode node) {
		return node.getTerrain() != EClientTerrain.Water;
	}
	
	public boolean hasNodes() {
	    return !nodes.isEmpty();
	}

}
