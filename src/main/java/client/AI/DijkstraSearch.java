package client.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import client.map.model.ClientFullMap;
import client.map.model.ClientFullMapNode;

public class DijkstraSearch {

	private final ClientFullMap map;
	private final PathMemory memory;
	private final TerrainCost terrainCost = new TerrainCost();


	public DijkstraSearch(ClientFullMap map, PathMemory memory) {
		this.map = map;
		this.memory = memory;
	}
	
	/**
	 * Finds the path using Dijkstra
	 * 
	 * @param start start position
	 * @param target target position
	 * @param collectedTreasure for terrain costs
	 * @return list of positions without start, first element is next step
	 */
	public List<Position> findPath(Position start, Position target, boolean collectedTreasure) {

		if (start.equals(target)) {
			return List.of();
		}

		Map<Position, Integer> distance = new HashMap<>();
		Map<Position, Position> previous = new HashMap<>();

		PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));

		distance.put(start, 0);
		queue.add(new Node(start, 0));

		while (!queue.isEmpty()) {
			Node currentNode = queue.poll();
			Position current = currentNode.position;

			if (currentNode.distance != distance.get(current)) continue;
			if (current.equals(target)) break;

			for (Position neighbor : neighbors(current)) {

				ClientFullMapNode node = getNode(neighbor);
				if (node == null || !map.isPassable(node)) continue;

				int cost = terrainCost.getCost(node, collectedTreasure);
				
				// Avoiding already visited tiles
				if (memory.visited(neighbor)) {
					cost += 4;
				}
				
				// Avoiding loop
				if (memory.recentlyVisited(neighbor)) {
					cost += 6;
				}

				int newDistance = distance.get(current) + cost;

				if (newDistance < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
					distance.put(neighbor, newDistance);
					previous.put(neighbor, current);
					queue.add(new Node(neighbor, newDistance));
				}
			}
		}

		if (!previous.containsKey(target)) {
			return List.of();
		}

		List<Position> path = new ArrayList<>();
		Position step = target;

		while (!step.equals(start)) {
			path.add(step);
			step = previous.get(step);
		}

		Collections.reverse(path);
		return path;
	}

	private static class Node {

		Position position;
		int distance;

		Node(Position position, int distance) {
			this.position = position;
			this.distance = distance;
		}
	}

	private List<Position> neighbors(Position position) {
		return List.of(
			new Position(position.x() + 1, position.y()),
			new Position(position.x() - 1, position.y()),
			new Position(position.x(), position.y() + 1),
			new Position(position.x(), position.y() - 1)
		);
	}

	private ClientFullMapNode getNode(Position position) {
		return map.getNode(position.x(), position.y());
	}

}