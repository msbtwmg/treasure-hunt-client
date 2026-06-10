package client.game.model;


import java.util.Collection;
import java.util.List;

import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;

 
public class ClientGameState {
	
	private ClientFullMap map; 
	private int x; 
	private int y; 
	private EClientGameState gameState;
	private boolean treasureVisible; 
	private String gameStateId;
	private boolean collectedTreasure;
	
	
	  public ClientGameState(ClientFullMap map, int x, int y, EClientGameState gameState,
			 String gameStateId, boolean collectedTreasure) {
	        this.map = map;
	        this.x =x; 
	        this.y = y; 
	        this.gameState = gameState;
	        this.gameStateId = gameStateId;
	        this.collectedTreasure = collectedTreasure;
	    }

    public int getX() {
		return x;
	}

	
	public int getY() {
		return y;
	}


	public ClientFullMap getMap() {
        return map;
    }
    
	public EClientGameState getMyGameState() {
	   return gameState;
	}

  	public boolean treasureVisible() {
	   return treasureVisible;
  	}

  	
    public String getGameStateId() {
        return gameStateId;
    }
    
    public boolean collectedTreasure() {
    	return collectedTreasure; 
    }
    
    public boolean gameFinished() {
        return gameState == EClientGameState.Won || gameState == EClientGameState.Lost;

    }
    
    public boolean mustAct() {
        return gameState == EClientGameState.MustAct;
    }
    
    public boolean hasFullMap() {
        return map != null && map.getMapNodes() != null && !map.getMapNodes().isEmpty();
    }
    
    public boolean isSecondClient() {
        return hasFullMap();
    }
    
    public Collection<ClientFullMapNode> getMapNodes() {
        if (map == null) return List.of();
        return map.getMapNodes();
    }
}


