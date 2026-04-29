package client.AI;

import java.util.*;

import client.game.model.ClientGameState;
import client.game.model.EClientMove;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTreasureState;

public class PathFinder {

    private Map<Position, ClientFullMapNode> nodes = new HashMap<>();

    private boolean preferGrass = true;
    private ClientFullMapNode currentTarget = null;

    private final Set<Position> visited = new HashSet<>();


    public record Position(int x, int y) {}

    /**
     * Set the map
     * @param mapNodes
     */

    public void setNodes(Collection<ClientFullMapNode> mapNodes) {
        nodes.clear();
        visited.clear();

        for (ClientFullMapNode n : mapNodes) {
            nodes.put(new Position(n.getX(), n.getY()), n);
        }
    }
    

    /**
     * Move decisions
     * @param gameState
     * @return
     */public EClientMove decideNextMove(ClientGameState gameState) {

    	    if (nodes == null || nodes.isEmpty()) {
    	        return EClientMove.Right;
    	    }

    	    ClientFullMapNode me = findMyPosition(); // ← now internal
    	    if (me == null) return EClientMove.Right;

    	    Position myPos = new Position(me.getX(), me.getY());

    	    // -------------------------------
    	    // TARGET REACHED
    	    // -------------------------------
    	    if (currentTarget != null &&
    	        currentTarget.getX() == me.getX() &&
    	        currentTarget.getY() == me.getY()) {

    	        visited.add(myPos);
    	        currentTarget = null;
    	    }

    	    // -------------------------------
    	    // PRIORITY: TREASURE
    	    // -------------------------------
    	    if (currentTarget == null && !gameState.collectedTreasure()) {
    	        currentTarget = findTreasure();
    	    }

    	    // -------------------------------
    	    // THEN: ENEMY FORT
    	    // -------------------------------
    	    if (currentTarget == null && gameState.collectedTreasure()) {
    	        currentTarget = findEnemyFort();
    	    }

    	    // -------------------------------
    	    // EXPLORE UNKNOWN
    	    // -------------------------------
    	    if (currentTarget == null) {
    	        currentTarget = findUnvisited(me);
    	    }

    	    // -------------------------------
    	    // MOVE TO TARGET
    	    // -------------------------------
    	    if (currentTarget != null) {
    	        EClientMove step = nextStep(me, currentTarget);

    	        if (step != null) {
    	            return step;
    	        }

    	        // fallback if path fails
    	        currentTarget = null;
    	    }

    	    return fallbackMove(me);
    	}

    // -------------------------------
    // DIJKSTRA (single version)
    // -------------------------------

    private EClientMove nextStep(ClientFullMapNode start, ClientFullMapNode target) {

        Position s = new Position(start.getX(), start.getY());
        Position t = new Position(target.getX(), target.getY());

        Map<Position, Integer> dist = new HashMap<>();
        Map<Position, Position> prev = new HashMap<>();

        PriorityQueue<Position> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        dist.put(s, 0);
        pq.add(s);

        while (!pq.isEmpty()) {
            Position cur = pq.poll();

            if (cur.equals(t)) break;

            for (Position nb : neighbors(cur)) {

                ClientFullMapNode node = nodes.get(nb);
                if (node == null || !isPassable(node)) continue;

                int cost = terrainCost(node);

                if (visited.contains(nb)) {
                    cost += 3; // penalty
                }

                int nd = dist.get(cur) + cost;

                if (nd < dist.getOrDefault(nb, Integer.MAX_VALUE)) {
                    dist.put(nb, nd);
                    prev.put(nb, cur);
                    pq.add(nb);
                }
            }
        }

        if (!prev.containsKey(t)) return null;

        // reconstruct path
        Position step = t;
        while (!prev.get(step).equals(s)) {
            step = prev.get(step);
        }

        return direction(s, step);
    }

    // -------------------------------
    // FIND UNVISITED
    // -------------------------------

    private ClientFullMapNode findUnvisited(ClientFullMapNode me) {

        Position start = new Position(me.getX(), me.getY());

        Map<Position, Integer> dist = new HashMap<>();
        PriorityQueue<Position> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        dist.put(start, 0);
        pq.add(start);

        ClientFullMapNode best = null;
        int bestDist = Integer.MAX_VALUE;

        while (!pq.isEmpty()) {
            Position cur = pq.poll();

            for (Position nb : neighbors(cur)) {

                ClientFullMapNode node = nodes.get(nb);
                if (node == null || !isPassable(node)) continue;

                int nd = dist.get(cur) + terrainCost(node);

                if (nd < dist.getOrDefault(nb, Integer.MAX_VALUE)) {
                    dist.put(nb, nd);
                    pq.add(nb);

                    if (!visited.contains(nb)) {

                        boolean isGrass = node.getTerrain().name().equalsIgnoreCase("Grass");

                        if ((preferGrass && isGrass) || (!preferGrass && !isGrass)) {
                            if (nd < bestDist) {
                                bestDist = nd;
                                best = node;
                            }
                        }
                    }
                }
            }
        }

        if (best != null) {
            preferGrass = !preferGrass;
        }

        return best;
    }

    // -------------------------------
    // HELPERS
    // -------------------------------

    private List<Position> neighbors(Position p) {
        return List.of(
            new Position(p.x()+1, p.y()),
            new Position(p.x()-1, p.y()),
            new Position(p.x(), p.y()+1),
            new Position(p.x(), p.y()-1)
        );
    }

    private boolean isPassable(ClientFullMapNode n) {
        return n.getTerrain() != client.map.model.EClientTerrain.Water;
    }

    private int terrainCost(ClientFullMapNode n) {
        return switch (n.getTerrain()) {
            case Grass -> 1;
            case Mountain -> 2;
            case Water -> Integer.MAX_VALUE / 4;
        };
    }

    private EClientMove direction(Position from, Position to) {
        if (to.x() == from.x()+1) return EClientMove.Right;
        if (to.x() == from.x()-1) return EClientMove.Left;
        if (to.y() == from.y()+1) return EClientMove.Down;
        if (to.y() == from.y()-1) return EClientMove.Up;
        return EClientMove.Right;
    }

    public EClientMove fallbackMove(ClientFullMapNode me) {

        Position p = new Position(me.getX(), me.getY());

        for (Position nb : neighbors(p)) {
            ClientFullMapNode n = nodes.get(nb);
            if (n != null && isPassable(n)) {
                return direction(p, nb);
            }
        }

        return EClientMove.Right;
    }
    
    private ClientFullMapNode findMyPosition() {
        for (ClientFullMapNode n : nodes.values()) {
            var pos = n.getPlayerPositionState();

            if (pos == EClientPositionState.MyPlayerPosition ||
                pos == EClientPositionState.BothPlayerPosition) {
                return n;
            }
        }
        return null;
    }
    
    private ClientFullMapNode findTreasure() {
        for (ClientFullMapNode n : nodes.values()) {
            if (n.getTreasureState() == EClientTreasureState.MyTreasureIsPresent) {
                return n;
            }
        }
        return null;
    }
    
    
    private ClientFullMapNode findEnemyFort() {
        for (ClientFullMapNode n : nodes.values()) {
            if (n.getFortState() == EClientFortState.EnemyFortPresent) {
                return n;
            }
        }
        return null;
    }
}