package client.game.model;


import client.map.model.ClientFullMap;

 
public class ClientGameState {
	
	private ClientFullMap map; 
	private int x; 
	private int y; 
	private EClientGameState gameState;
	private boolean hasTreasure; 
	private String gameStateId;
	private boolean collectedTreasure;
	
	
	  public ClientGameState(ClientFullMap map, int x, int y, EClientGameState gameState,
			  boolean hasTreasure, String gameStateId, boolean collectedTreasure) {
	        this.map = map;
	        this.x =x; 
	        this.y = y; 
	        this.gameState = gameState;
	        this.hasTreasure = hasTreasure;
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

  	public boolean hasTreasure() {
	   return hasTreasure;
  	}

  	
    public String getGameStateId() {
        return gameStateId;
    }
    
    public boolean collectedTreasure() {
    	return collectedTreasure; 
    }
}
