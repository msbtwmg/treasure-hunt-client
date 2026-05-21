package client.map.placement;

import java.util.Collection;

import client.map.model.ClientFullMapNode;

public class EnemyHalfFinder {
	// TODO: Test , check if I have a Denkfehler. 
	 public EnemyHalfPosition find(Collection<ClientFullMapNode> nodes) {
	        int minX = nodes.stream().mapToInt(ClientFullMapNode::getX).min().orElse(0);
	        int maxX = nodes.stream().mapToInt(ClientFullMapNode::getX).max().orElse(0);
	        int minY = nodes.stream().mapToInt(ClientFullMapNode::getY).min().orElse(0);
	        int maxY = nodes.stream().mapToInt(ClientFullMapNode::getY).max().orElse(0);

	        if (minX == 0 && minY == 0) return EnemyHalfPosition.ORIGIN;
	        
	        if (minY >= 5)  return EnemyHalfPosition.BOTTOM;
	        if (maxY < 5)   return EnemyHalfPosition.TOP;

	        
	        if (minX >= 10) return EnemyHalfPosition.RIGHT;
	        if (maxX < 10)  return EnemyHalfPosition.LEFT;
	       

	        return EnemyHalfPosition.NOTFOUND;
	    }
	 
}
