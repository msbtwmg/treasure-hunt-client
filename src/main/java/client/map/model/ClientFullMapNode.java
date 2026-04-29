package client.map.model;


public class ClientFullMapNode {

    private final int x;
    private final int y;
    private final EClientTerrain terrain;
    private final EClientFortState fortState;
    private final EClientTreasureState treasureState;
	private EClientPositionState positionState;

    public ClientFullMapNode(int x, int y, EClientTerrain terrain, EClientFortState fortState, EClientTreasureState treasureState, EClientPositionState positionState) {
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.fortState = fortState;
        this.treasureState = treasureState; 
        this.positionState = positionState; 
    }

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public EClientTerrain getTerrain() {
		return terrain;
	}

	public EClientFortState getFortState() {
		return fortState;
	}

	public EClientTreasureState getTreasureState() {
		return treasureState;
	}

	public EClientPositionState getPlayerPositionState() {
		return positionState;
	}
	

	
	


}