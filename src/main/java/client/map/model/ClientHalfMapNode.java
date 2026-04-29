package client.map.model;

public class ClientHalfMapNode {

    private int x;
    private int y;
    private boolean myFort;
    private boolean enemyFort;
    private EClientTerrain terrain;

    public ClientHalfMapNode(int x, int y, boolean myFort, boolean enemyFort, EClientTerrain terrain) {
        this.x = x;
        this.y = y;
        this.myFort = myFort;
        this.enemyFort = enemyFort;
        this.terrain = terrain;
    }


	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean myFortPresent() {
		return myFort;
	}
	
	public boolean enemyFortPresent() {
		return enemyFort;
	}


	public EClientTerrain getTerrain() {
		return terrain;
	}
	
}
