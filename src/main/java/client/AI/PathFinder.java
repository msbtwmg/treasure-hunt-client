package client.AI;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.game.model.ClientGameState;
import client.game.model.EClientMove;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;

public class PathFinder {
	private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);
	
    private Map<Position, ClientFullMapNode> nodes = new HashMap<>();

    private ClientFullMapNode currentTarget = null;

    private final Set<Position> visited = new HashSet<>();
    private Position lastPosition = null;

    public record Position(int x, int y) {}

    public void setNodes(Collection<ClientFullMapNode> mapNodes) {
        nodes.clear();

        for (ClientFullMapNode n : mapNodes) {
            nodes.put(new Position(n.getX(), n.getY()), n);
        }
    }

    /**
     * Decides the next move based on current game state
     * 
     * @param gameState current game state
     * @return next move for player
     */
 
    public EClientMove decideNextMove(ClientGameState gameState) {
    	logger.debug("~ Deciding next move");

        if (nodes.isEmpty()) return EClientMove.Right;

        ClientFullMapNode me = findMyPosition();
        if (me == null) return EClientMove.Right;

        Position myPos = new Position(me.getX(), me.getY());

        // mark visited
        visited.add(myPos);
        lastPosition = myPos;

        boolean hasTreasure = gameState.collectedTreasure();

        // Reset the target when is already reached
        if (currentTarget != null &&
            currentTarget.getX() == me.getX() &&
            currentTarget.getY() == me.getY()) {

            currentTarget = null;
        }

        // Trying to find the treasure
        if (!hasTreasure) {
         logger.debug("Searching for treasure..");
         
            ClientFullMapNode treasure = findTreasure();

            if (treasure != null) {
                currentTarget = treasure;
                logger.info("Target set > Treasure at ({}, {})", treasure.getX(), treasure.getY());

            } 
            else if (currentTarget == null) {
                currentTarget = findClosestUnvisited(me, hasTreasure);
            }
        }

        // Try to cross the enemy map half
        else if (!isEnemyHalf(myPos)) {
        	  logger.debug("Crossing to enemy half..");

            if (currentTarget == null ||
                !isEnemyHalf(new Position(currentTarget.getX(), currentTarget.getY()))) {

                currentTarget = findClosestBorder(me, hasTreasure);
            }
        }

        // When on enemy half, try and find the fort
        else {
            logger.debug("Searching for enemy fort...");
            ClientFullMapNode fort = findEnemyFort();

            if (fort != null) {
                currentTarget = fort;
            } 
            else if (currentTarget == null) {
                currentTarget = findClosestUnvisited(me, hasTreasure);
            }
        }

        // Move to a target
        if (currentTarget != null) {

            EClientMove step = nextStep(me, currentTarget, hasTreasure);

            if (step != null) {
                return step;
            }

            
            currentTarget = null;
        }

        // Fallback in case the others fail
        return fallbackMove(me);
    }
    /**
     * Computes the next move using Dijkstra 
     * @param start starting node
     * @param target destination nod e
     * @param collectedTreasure to check if the treasure has been collected
     * @return the next move to target
     */
    private EClientMove nextStep(ClientFullMapNode start, ClientFullMapNode target, boolean collectedTreasure) {

        Position s = new Position(start.getX(), start.getY());
        Position t = new Position(target.getX(), target.getY());

        Map<Position, Integer> dist = new HashMap<>();
        Map<Position, Position> prev = new HashMap<>();

        class Node {
            Position pos;
            int dist;
            Node(Position p, int d) { pos = p; dist = d; }
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));

        dist.put(s, 0);
        pq.add(new Node(s, 0));

        while (!pq.isEmpty()) {
            Node curNode = pq.poll();
            Position cur = curNode.pos;

            if (curNode.dist != dist.get(cur)) continue;
            if (cur.equals(t)) break;

            for (Position nb : neighbors(cur)) {

                ClientFullMapNode node = nodes.get(nb);
                if (node == null || !isPassable(node)) continue;

                int cost = terrainCost(node, collectedTreasure);
                // Avoiding already visited tiles
                if (visited.contains(nb)) {
                    cost += 3; 
                }

                int nd = dist.get(cur) + cost;

                if (nd < dist.getOrDefault(nb, Integer.MAX_VALUE)) {
                    dist.put(nb, nd);
                    prev.put(nb, cur);
                    pq.add(new Node(nb, nd));
                }
            }
        }

        if (!prev.containsKey(t))  {
            logger.warn("No path found to target ({}, {})", target.getX(), target.getY());
        	return null;
        }

        Position step = t;
        while (!prev.get(step).equals(s)) {
            step = prev.get(step);
        }

        return direction(s, step);
    }

    private ClientFullMapNode findClosestUnvisited(ClientFullMapNode me, boolean hasTreasure) {

        ClientFullMapNode best = null;
        int bestDist = Integer.MAX_VALUE;

        for (ClientFullMapNode n : nodes.values()) {

            Position p = new Position(n.getX(), n.getY());

            if (visited.contains(p)) continue;
            if (!isPassable(n)) continue;

            int dist = Math.abs(me.getX() - n.getX()) +
                       Math.abs(me.getY() - n.getY());

            if (dist < bestDist) {
                bestDist = dist;
                best = n;
            }
        }

        return best;
    }

    private ClientFullMapNode findClosestBorder(ClientFullMapNode me, boolean hasTreasure) {

        ClientFullMapNode best = null;
        int bestDist = Integer.MAX_VALUE;

        for (ClientFullMapNode n : nodes.values()) {

            if (!isPassable(n)) continue;

            boolean isBorder =
                n.getX() == 0 || n.getY() == 0 ||
                n.getX() == 19 || n.getY() == 9;

            if (!isBorder) continue;

            if (nextStep(me, n, hasTreasure) == null) continue;

            int dist = Math.abs(me.getX() - n.getX()) +
                       Math.abs(me.getY() - n.getY());

            if (dist < bestDist) {
                bestDist = dist;
                best = n;
            }
        }

        return best;
    }
    
 

    public EClientMove fallbackMove(ClientFullMapNode me) {
    	logger.warn("Using fallback move");

        Position p = new Position(me.getX(), me.getY());

        List<Position> options = new ArrayList<>();

        for (Position nb : neighbors(p)) {
            ClientFullMapNode n = nodes.get(nb);
            if (n != null && isPassable(n)) {
                options.add(nb);
            }
        }

        Collections.shuffle(options);

        for (Position nb : options) {
            if (lastPosition != null && nb.equals(lastPosition)) continue;
            if (!visited.contains(nb)) {
                return direction(p, nb);
            }
        }

        if (!options.isEmpty()) {
            return direction(p, options.get(0));
        }

        return EClientMove.Right;
    }


    private List<Position> neighbors(Position p) {
        return List.of(
            new Position(p.x()+1, p.y()),
            new Position(p.x()-1, p.y()),
            new Position(p.x(), p.y()+1),
            new Position(p.x(), p.y()-1)
        );
    }

    private boolean isPassable(ClientFullMapNode n) {
        return n.getTerrain() != EClientTerrain.Water;
    }

    private int terrainCost(ClientFullMapNode n, boolean collectedTreasure) {
    	
        return switch (n.getTerrain()) {
            case Grass -> collectedTreasure ? 1 : 3;
            case Mountain -> collectedTreasure ? 4 : 1; // Mountain priority before finind treasure
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

    public ClientFullMapNode findMyPosition() {
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

    private ClientFullMapNode findMyFort() {
        for (ClientFullMapNode n : nodes.values()) {
            if (n.getFortState() == EClientFortState.MyFortPresent) {
                return n;
            }
        }
        return null;
    }

    private boolean isHorizontalSplit() {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Position p : nodes.keySet()) {
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return width > height;
    }

    private boolean isEnemyHalf(Position p) {
        ClientFullMapNode fort = findMyFort();
        if (fort == null) return false;

        if (isHorizontalSplit()) {
            return p.x() > fort.getX();
        } else {
            return p.y() > fort.getY();
        }
    }
    
    public ClientFullMapNode getNode(int x, int y) {
        return nodes.get(new Position(x, y));
    }
    
}