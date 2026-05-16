package client.map;

import client.game.GameModel;
import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;

public class MapView {
   private final GameModel model;
   
	String PLAYER_SYMBOL = "👸";
	String ENEMY_SYMBOL = "🫅";
	String BOTH_PLAYERS_SYMBOL = "💥";
	String TREASURE_STANDARD_SYMBOL = "🎁";
	String TREASURE_COLLECTED_SYMBOL = "💰";
	String ENEMY_FORT_SYMBOL = "🏰";
	String MY_FORT_SYMBOL= "💒";
	String WATER_SYMBOL = "🔵";
	String MOUNTAIN_SYMBOL = "🟤";
	String GRASS_SYMBOL = "🟢";
	

    public MapView(GameModel model) {
        this.model = model;

        model.gameStateChanged.addObserver(event -> renderMap());
    }

    
    private void renderMap() {
        ClientFullMap map = model.getFullMap();
        if (map == null) return;

        int maxX = 0;
        int maxY = 0;

        for (ClientFullMapNode node : map.getMapNodes()) {
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
        }

        // TODO: replace with  ClientFullMap logic  MAPHELPER
        //Unknown full map width height , row by row visualising
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                System.out.print(renderField(map, x, y));
            }
            System.out.println();
        }
      
    }
		
    private String renderField(ClientFullMap map, int x, int y) {
        for (ClientFullMapNode node : map.getMapNodes()) {
            if (node.getX() == x && node.getY() == y) {
                return renderNode(node);
            }
        }
        
        return " "; 
    }
    
    private String renderNode(ClientFullMapNode node) {

    	// players
        if (node.hasMyPlayer()) return PLAYER_SYMBOL;
        if (node.hasEnemyPlayer()) return ENEMY_SYMBOL;
        if (node.hasBothPlayer()) return BOTH_PLAYERS_SYMBOL;


        // Treasure 
        if (node.isTreasureVisible()) { 
        	
        	if (model.hasCollectedTreasure()) {
        		return TREASURE_COLLECTED_SYMBOL;
        	} 

        		return TREASURE_STANDARD_SYMBOL;
        }
        
        // Forts 
        
        if (node.hasEnemyFort()) return ENEMY_FORT_SYMBOL;
        if (node.hasMyFort()) return MY_FORT_SYMBOL;
        
        // Other terrains
        
        switch (node.getTerrain()) {
        case Water:
        	return WATER_SYMBOL;
        	
        case Mountain:
        	return MOUNTAIN_SYMBOL;
        case Grass: 
        	return GRASS_SYMBOL; 
        	
        default: 
        	return "?";
        }
    }
}
