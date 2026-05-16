package client.AI;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.game.model.ClientGameState;
import client.game.model.EClientMove;
import client.map.MapHelper;
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
	private final Set<Position> unreachableTargets = new HashSet<>();
	private final Deque<Position> recentPositions = new ArrayDeque<>();

	private Position lastPosition = null;
	private int stuckCounter = 0;

	private static final int MAX_RECENT_POSITIONS = 6;

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
	// TODO: add strategy pattern maybe ad stamp coupling reduction mit hasTreasure ?? 
	public EClientMove decideNextMove(ClientGameState gameState) {
		logger.debug("~ Deciding next move");
		MapHelper.printMap(nodes);

		if (nodes.isEmpty()) return EClientMove.Right;

		ClientFullMapNode me = findMyPosition();
		if (me == null) return EClientMove.Right;

		Position myPos = new Position(me.getX(), me.getY());
		boolean hasTreasure = gameState.collectedTreasure();

		updatePositionMemory(myPos);

		// reset if target reached
		if (currentTarget != null &&
			currentTarget.getX() == me.getX() &&
			currentTarget.getY() == me.getY()) {

			logger.debug("Target reached");
			currentTarget = null;
		}

		// only reset when stuck for multiple turns
		
		if (stuckCounter >= 3) {
			logger.warn("Probably stuck ..resetting target");

			if (currentTarget != null && !isImportantTarget(currentTarget)) {
				unreachableTargets.add(new Position(currentTarget.getX(), currentTarget.getY()));
			}

			currentTarget = null;
			stuckCounter = 0;
		}

		// selecting target
		if (currentTarget == null) {

			if (!hasTreasure) {
				logger.debug("Searching for treasure..");

				ClientFullMapNode treasure = findTreasure();
				currentTarget = (treasure != null) ? treasure: findClosestUnvisited(me, hasTreasure);
			}

			else if (!isEnemyHalf(myPos)) {
				logger.debug("Crossing to enemy half..");
				currentTarget = findClosestEnemyHalfEntry(me, hasTreasure);
			}

			else {
				logger.debug("Searching for enemy fort...");

				ClientFullMapNode fort = findEnemyFort();
				currentTarget = (fort != null)
						? fort
						: findClosestUnvisited(me, hasTreasure);
			}
		}

		if (currentTarget != null) {
			EClientMove step = nextStep(me, currentTarget, hasTreasure);

			if (step != null) {
				logger.debug("Moving to target ({}, {})", currentTarget.getX(), currentTarget.getY());
				return step;
			}

			logger.warn("Target not reachable , resetting");

			if (!isImportantTarget(currentTarget)) {
				unreachableTargets.add(new Position(currentTarget.getX(), currentTarget.getY()));
			}

			currentTarget = null;
		}
		// Fallback in case the others fail
		return fallbackMove(me, hasTreasure);
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

		List<Position> path = findPath(startPos, targetPos, collectedTreasure);

		if (path.isEmpty()) {
			logger.warn("No path found to target ({}, {})", target.getX(), target.getY());
			return null;
		}

		Position step = path.get(0);
		return direction(startPos, step);
	}

	/**
	 * Finds the path using Dijkstra
	 * 
	 * @param start start position
	 * @param target target position
	 * @param collectedTreasure for terrain costs
	 * @return list of positions without start, first element is next step
	 */
	private List<Position> findPath(Position start, Position target, boolean collectedTreasure) {

		if (start.equals(target)) {
			return List.of();
		}

		Map<Position, Integer> dist = new HashMap<>();
		Map<Position, Position> prev = new HashMap<>();

		class Node {
			Position pos;
			int distances;

			Node(Position p, int d) {
				pos = p;
				distances = d;
			}
		}

		PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distances));

		dist.put(start, 0);
		pq.add(new Node(start, 0));

		while (!pq.isEmpty()) {
			Node curNode = pq.poll();
			Position cur = curNode.pos;

			if (curNode.distances != dist.get(cur)) continue;
			if (cur.equals(target)) break;

			for (Position nb : neighbors(cur)) {

				ClientFullMapNode node = nodes.get(nb);
				if (node == null || !isPassable(node)) continue;

				int cost = terrainCost(node, collectedTreasure);

				// Avoiding already visited tiles
				if (visited.contains(nb)) {
					cost += 4;
				}

				// Avoiding loop
				if (recentPositions.contains(nb)) {
					cost += 6;
				}

				int nd = dist.get(cur) + cost;

				if (nd < dist.getOrDefault(nb, Integer.MAX_VALUE)) {
					dist.put(nb, nd);
					prev.put(nb, cur);
					pq.add(new Node(nb, nd));
				}
			}
		}

		if (!prev.containsKey(target)) {
			return List.of();
		}

		List<Position> path = new ArrayList<>();
		Position step = target;

		while (!step.equals(start)) {
			path.add(step);
			step = prev.get(step);
		}

		Collections.reverse(path);
		return path;
	}

	private ClientFullMapNode findClosestUnvisited(ClientFullMapNode me, boolean hasTreasure) {

		ClientFullMapNode best = null;
		int bestScore = Integer.MAX_VALUE;

		for (ClientFullMapNode node : nodes.values()) {

			Position p = new Position(node.getX(), node.getY()); 

			if (visited.contains(p)) continue;
			if (unreachableTargets.contains(p)) continue;
			if (!isPassable(node)) continue;

			List<Position> path = findPath(new Position(me.getX(), me.getY()), p, hasTreasure);
			if (path.isEmpty()) continue;

			int score = path.size();

			// before treasure prefer mountains 
			if (!hasTreasure && node.getTerrain() == EClientTerrain.Mountain) {
				score -= 2;
			}

			// after treasure prefer enemy half fields
			if (hasTreasure && isEnemyHalf(p)) {
				score -= 3;
			}

			if (score < bestScore) {
				bestScore = score;
				best = node;
			}
		}

		return best;
	}

	private ClientFullMapNode findClosestEnemyHalfEntry(ClientFullMapNode me, boolean hasTreasure) {

		ClientFullMapNode best = null;
		int bestScore = Integer.MAX_VALUE;

		for (ClientFullMapNode node : nodes.values()) {

			Position position = new Position(node.getX(), node.getY());

			if (!isEnemyHalf(position)) continue;
			if (unreachableTargets.contains(position)) continue;
			if (!isPassable(node)) continue;

			List<Position> path = findPath(new Position(me.getX(), me.getY()), position, hasTreasure);
			if (path.isEmpty()) continue;

			int score = path.size();

			// prefer fields near the middle 
			score += distanceToMapMiddle(position);

			if (score < bestScore) {
				bestScore = score;
				best = node;
			}
		}

		return best;
	}

	public EClientMove fallbackMove(ClientFullMapNode me, boolean hasTreasure) {
		logger.warn("Using fallback move");

		Position p = new Position(me.getX(), me.getY());

		Position best = null;
		int bestScore = Integer.MAX_VALUE;

		for (Position nb : neighbors(p)) {
			ClientFullMapNode n = nodes.get(nb);

			if (n == null || !isPassable(n)) continue;

			int score = terrainCost(n, hasTreasure);

			if (visited.contains(nb)) {
				score += 5;
			}

			if (recentPositions.contains(nb)) {
				score += 10;
			}

			// after treasure we prefer moving into enemy half
			if (hasTreasure && isEnemyHalf(nb)) {
				score -= 4;
			}

			if (score < bestScore) {
				bestScore = score;
				best = nb;
			}
		}

		if (best != null) {
			return direction(p, best);
		}

		return EClientMove.Right;
	}

	private void updatePositionMemory(Position myPos) {

		if (lastPosition != null && lastPosition.equals(myPos)) {
			stuckCounter++;
		} else {
			stuckCounter = 0;
		}

		visited.add(myPos);
		lastPosition = myPos;

		recentPositions.addLast(myPos);

		while (recentPositions.size() > MAX_RECENT_POSITIONS) {
			recentPositions.removeFirst();
		}
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

	private boolean isPassable(ClientFullMapNode n) {
		return n.getTerrain() != EClientTerrain.Water;
	}

	private int terrainCost(ClientFullMapNode n, boolean collectedTreasure) {

		return switch (n.getTerrain()) {
			case Grass -> collectedTreasure ? 1 : 3;
			case Mountain -> collectedTreasure ? 4 : 1; // High Mountain priority before finding treasure
			case Water -> Integer.MAX_VALUE / 4;
		};
	}

	private EClientMove direction(Position from, Position to) {
		if (to.x() == from.x() + 1) return EClientMove.Right;
		if (to.x() == from.x() - 1) return EClientMove.Left;
		if (to.y() == from.y() + 1) return EClientMove.Down;
		if (to.y() == from.y() - 1) return EClientMove.Up;

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

		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		for (Position pos : nodes.keySet()) {
			minX = Math.min(minX, pos.x());
			maxX = Math.max(maxX, pos.x());
			minY = Math.min(minY, pos.y());
			maxY = Math.max(maxY, pos.y());
		}

		if (isHorizontalSplit()) {
			double middleX = (minX + maxX) / 2.0;
			boolean myFortIsLeft = fort.getX() < middleX;

			return myFortIsLeft ? p.x() > middleX : p.x() < middleX;
		} else {
			double middleY = (minY + maxY) / 2.0;
			boolean myFortIsTop = fort.getY() < middleY;

			return myFortIsTop ? p.y() > middleY : p.y() < middleY;
		}
	}
	// TODO: maybe put it somewhere else

	private int distanceToMapMiddle(Position p) {
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		for (Position pos : nodes.keySet()) {
			minX = Math.min(minX, pos.x());
			maxX = Math.max(maxX, pos.x());
			minY = Math.min(minY, pos.y());
			maxY = Math.max(maxY, pos.y());
		}

		if (isHorizontalSplit()) {
			double middleX = (minX + maxX) / 2.0;
			return (int) Math.abs(p.x() - middleX);
		} else {
			double middleY = (minY + maxY) / 2.0;
			return (int) Math.abs(p.y() - middleY);
		}
	}

	public ClientFullMapNode getNode(int x, int y) {
		return nodes.get(new Position(x, y));
	}
}