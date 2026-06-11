package client.AI;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class PathMemory {

	private static final int MAX_RECENT_POSITIONS = 6;

	private final Set<Position> visited = new HashSet<>();
	private final Set<Position> unreachableTargets = new HashSet<>();
	private final Deque<Position> recentPositions = new ArrayDeque<>();

	private Position lastPosition;
	private int stuckCounter = 0;

	public void updatePosition(Position myPosition) {

		if (lastPosition != null && lastPosition.equals(myPosition)) {
			stuckCounter++;
		} else {
			stuckCounter = 0;
		}

		visited.add(myPosition);
		lastPosition = myPosition;

		recentPositions.addLast(myPosition);

		while (recentPositions.size() > MAX_RECENT_POSITIONS) {
			recentPositions.removeFirst();
		}
	}

	public boolean visited(Position position) {
		return visited.contains(position);
	}

	public boolean recentlyVisited(Position position) {
		return recentPositions.contains(position);
	}

	public boolean isUnreachableTarget(Position position) {
		return unreachableTargets.contains(position);
	}

	public void addUnreachableTarget(Position position) {
		unreachableTargets.add(position);
	}

	public boolean isStuck() {
		return stuckCounter >= 6;
	}

	public void resetStuckCounter() {
		stuckCounter = 0;
	}
}