package client.AI;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.game.model.EClientMove;
import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;


public class PathFinder {
	private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);

	private ClientFullMapNode currentTarget;
	private ClientFullMap map;
	private boolean enemyPositionReached = false;
	
	private final PathMemory memory = new PathMemory();
	private final TerrainCost terrainCost = new TerrainCost();

	private DijkstraSearch dijkstraSearch;
	
	// remember enemy position after these amoutn of moves 
	private static final int MOVES = 10;

	private int moveCounter = 0;
	private Position enemyPosition;

	public void setMap(ClientFullMap fullMap) {
	    this.map = fullMap;
	    this.dijkstraSearch = new DijkstraSearch(fullMap, memory);
	}
	


	/**
	 * Decides the next move based on current game state
	 * 
	 * @param gameState current game state
	 * @return next move for player
	 */

	public EClientMove decideNextMove(boolean hasTreasure) {
	    if (!hasUsableMap()) return EClientMove.Right;

	    ClientFullMapNode me = map.findMyPosition();
	    if (me == null) return EClientMove.Right;

	    Position myPos = new Position(me.getX(), me.getY());
	    
	    moveCounter++;
	    rememberEnemyPosition();

	    memory.updatePosition(myPos);
	    resetTarget(me);
	    resetTargetWhenStuck();

	    chooseTarget(me, myPos, hasTreasure);

	    EClientMove move = moveToCurrentTarget(me, hasTreasure);

	    if (move != null) {
	        return move;
	    }

	    return fallbackMove(me, hasTreasure);
	}
	
	private void rememberEnemyPosition() {

		if (enemyPositionReached) return; 
		
		if (moveCounter < MOVES) return;
		

		if (enemyPosition != null) return;
		

		ClientFullMapNode enemy = map.findEnemyPosition();

		if (enemy == null) return;
		

		enemyPosition = new Position(enemy.getX(), enemy.getY());

		logger.debug("Remembered enemy position after {} moves: ({}, {})",
				moveCounter,
				enemyPosition.x(),
				enemyPosition.y());
	}
	
	
	private boolean hasUsableMap() {
		return map != null && map.hasNodes();
	}
	
	private void resetTarget(ClientFullMapNode me) {
		if (currentTarget == null) {
			return; 
		}
		
		boolean targetReached  = currentTarget.getX() == me.getX() 
									&& currentTarget.getY() == me.getY();

		if (!targetReached) {
			return;
		}
	
		logger.debug("Target reached");
		
		
		if (enemyPosition != null &&
			    enemyPosition.x() == currentTarget.getX() &&
			    enemyPosition.y() == currentTarget.getY()) {
			
			logger.debug("Reached remembered enemy position");
			enemyPosition = null;
			enemyPositionReached = true;
			}
		
		currentTarget = null;

	}
	
	private void resetTargetWhenStuck()   {
		if (!memory.isStuck()) {
			return; 
		}
		
		logger.warn("Probably stuck ..resetting target");
	
		currentTarget = null;
		memory.resetStuckCounter();
	}
	
	
	private void chooseTarget(ClientFullMapNode me, Position myPos, boolean hasTreasure) {
		if (currentTarget != null) {
			return;
		}

		if (!hasTreasure) {
			currentTarget = chooseTreasureTarget(me);
			return;
		}

	
		if (enemyPosition != null) {
			ClientFullMapNode enemyNode = map.getNode(enemyPosition.x(), enemyPosition.y());

			if (enemyNode != null && map.isPassable(enemyNode)) {
				logger.debug("Going to remembered enemy position ({}, {})",
						enemyPosition.x(),
						enemyPosition.y());

				currentTarget = enemyNode;
				return;
			}

			enemyPosition = null;
		}

		currentTarget = chooseEnemyFortTarget(me, hasTreasure);
	}
	

	
	private ClientFullMapNode chooseTreasureTarget(ClientFullMapNode me) {
	    logger.debug("Searching for treasure...");

	    ClientFullMapNode treasure = map.findTreasure();

	    if (treasure != null) {
	        return treasure;
	    }

	    return findClosestUnvisited(me, false);
	}
	private ClientFullMapNode chooseEnemyFortTarget(ClientFullMapNode me, boolean hasTreasure) {
	    logger.debug("Searching for enemy fort...");

	    ClientFullMapNode fort = map.findEnemyFort();

	    if (fort != null) {
	        return fort;
	    }

	    return findClosestUnvisited(me, hasTreasure);
	}
	
	private EClientMove moveToCurrentTarget(ClientFullMapNode me, boolean hasTreasure) {
	    if (currentTarget == null) {
	        return null;
	    }

	    EClientMove step = nextStep(me, currentTarget, hasTreasure);

	    if (step != null) {
	        logger.debug("Moving to target ({}, {})",
	                currentTarget.getX(),
	                currentTarget.getY());

	        return step;
	    }

	    logger.warn("Target not reachable , resetting");

	    if (!isImportantTarget(currentTarget)) {
	       memory.addUnreachableTarget(new Position(
	                currentTarget.getX(),
	                currentTarget.getY()));
	    }

	    currentTarget = null;
	    return null;
	}
	
	/**
	 * Computes the next move using Dijkstra
	 * 
	 * @param start starting node
	 * @param target destination nod e
	 * @param collectedTreasure to check if the treasure has been collected
	 * @return the next move to target
	 */
	private EClientMove nextStep(ClientFullMapNode start, ClientFullMapNode target, boolean collectedTreasure) {

		Position startPos = new Position(start.getX(), start.getY());
		Position targetPos = new Position(target.getX(), target.getY());

		List<Position> path = dijkstraSearch.findPath(startPos, targetPos, collectedTreasure);

		if (path.isEmpty()) {
			logger.warn("No path found to target ({}, {})", target.getX(), target.getY());
			return null;
		}

		Position step = path.get(0);
		return direction(startPos, step);
	}

	
	private ClientFullMapNode findClosestUnvisited(ClientFullMapNode me, boolean hasTreasure) {

		ClientFullMapNode best = null;
		int bestScore = Integer.MAX_VALUE;

		for (ClientFullMapNode node : map.getMapNodes()) {

			Position position = new Position(node.getX(), node.getY()); 

			if (memory.visited(position)) continue;
			if (memory.isUnreachableTarget(position)) continue;
			if (!map.isPassable(node)) continue;

			List<Position> path = dijkstraSearch.findPath(new Position(me.getX(), me.getY()), position, hasTreasure);
			if (path.isEmpty()) continue;

			int score = path.size();

			// before treasure prefer mountains 
			if (!hasTreasure && node.getTerrain() == EClientTerrain.Mountain) {
				score -= 2;
			}

			// after treasure prefer enemy half fields
			if (hasTreasure && isEnemyHalf(position)) {
				score -= 3;
			}

			if (score < bestScore) {
				bestScore = score;
				best = node;
			}
		}

		return best;
	}

	private int fallbackMoveCount = 0;
	public EClientMove fallbackMove(ClientFullMapNode me, boolean collectedTreasure) {
		fallbackMoveCount++; 
		logger.warn("Using fallback move {} times", fallbackMoveCount);

		Position p = new Position(me.getX(), me.getY());

		Position best = null;
		int bestScore = Integer.MAX_VALUE;

		for (Position neighbor : neighbors(p)) {
			ClientFullMapNode node = getNode(neighbor);

			if (node == null || !map.isPassable(node)) continue;

			int score = terrainCost.getCost(node, collectedTreasure);

			if (memory.visited(neighbor)) {
				score += 5;
			}

			if (memory.recentlyVisited(neighbor)) {
				score += 10;
			}

			if (collectedTreasure && isEnemyHalf(neighbor)) {
				score -= 4;
			}

			if (score < bestScore) {
				bestScore = score;
				best = neighbor;
			}
		}

		if (best != null) {
			return direction(p, best);
		}

		return EClientMove.Right;
	}


	private boolean isImportantTarget(ClientFullMapNode node) {
		return node.getTreasureState() == EClientTreasureState.MyTreasureIsPresent ||
			   node.getFortState() == EClientFortState.EnemyFortPresent;
	}

	private List<Position> neighbors(Position p) {
		return List.of(
			new Position(p.x() + 1, p.y()),
			new Position(p.x() - 1, p.y()),
			new Position(p.x(), p.y() + 1),
			new Position(p.x(), p.y() - 1)
		);
	}

	private EClientMove direction(Position from, Position to) {
		if (to.x() == from.x() + 1) return EClientMove.Right;
		if (to.x() == from.x() - 1) return EClientMove.Left;
		if (to.y() == from.y() + 1) return EClientMove.Down;
		if (to.y() == from.y() - 1) return EClientMove.Up;

		return EClientMove.Right;
	}


	private boolean isEnemyHalf(Position p) {
	    return map != null && map.isEnemyHalf(p.x(), p.y());
	}

	private ClientFullMapNode getNode(Position p) {
	    return map.getNode(p.x(), p.y());
	}
	
}