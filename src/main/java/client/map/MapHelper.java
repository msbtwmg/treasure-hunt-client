package client.map;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import client.AI.PathFinder;
import client.map.model.ClientFullMapNode;
import client.map.model.EClientFortState;
import client.map.model.EClientPositionState;
import client.map.model.EClientTerrain;
import client.map.model.EClientTreasureState;

public class MapHelper {

    private static final int[][] DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    /** 
	 * Making sure there are no islands and all walkable fields are reachable  -> floodfill algorithm
	 * @param grid
	 * @return
	 */
    public static boolean isConnected(EClientTerrain[][] grid) {

        int height = grid.length;
        int width = grid[0].length;

        int startX = -1;
        int startY = -1;
        int walkableFields = 0;

        // find first walkable field + count
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isWalkable(grid[y][x])) {
                    walkableFields++;
                    if (startX == -1) {
                        startX = x;
                        startY = y;
                    }
                }
            }
        }
        if (startX == -1) return false;

        boolean[][] visited = new boolean[height][width];
        Queue<int[]> queue = new ArrayDeque<>();

        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        int reached = 0;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            reached++;

            for (int[] d : DIRECTIONS) {
                int nx = current[0] + d[0];
                int ny = current[1] + d[1];

                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (visited[ny][nx]) continue;
                if (!isWalkable(grid[ny][nx])) continue;

                visited[ny][nx] = true;
                queue.add(new int[]{nx, ny});
            }
        }

        return reached == walkableFields;
    }

    
    /**
     * Check if >=  51% of  edge is accessible
     */
    public static boolean isBorderAccessible(EClientTerrain[][] grid) {

        int width = grid[0].length;
        int height = grid.length;

        int top = 0, bottom = 0, left = 0, right = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (!isEdgeField(x, y, width, height)) continue;
                if (!isWalkable(grid[y][x])) continue;

                if (y == 0) top++;
                if (y == height - 1) bottom++;
                if (x == 0) left++;
                if (x == width - 1) right++;
            }
        }

        int minTopBottom = (int) Math.ceil(width * 0.51);
        int minLeftRight = (int) Math.ceil(height * 0.51);

        return top >= minTopBottom &&
               bottom >= minTopBottom &&
               left >= minLeftRight &&
               right >= minLeftRight;
    }

    /**
     * Check minimum terrain distribution
     */
    public static boolean isTerrainValid(EClientTerrain[][] grid,  int minGrass,  int minWater,   int minMountain) {

        int grass = 0, water = 0, mountain = 0;

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                switch (grid[y][x]) {
                    case Grass -> grass++;
                    case Water -> water++;
                    case Mountain -> mountain++;
                }
            }
        }

        return grass >= minGrass &&
               water >= minWater &&
               mountain >= minMountain;
    }



    public static boolean isWalkable(EClientTerrain terrain) {
        return switch (terrain) {
            case Grass, Mountain -> true;
            case Water -> false;
        };
    }

  
    public static boolean isEdgeField(int x, int y, int width, int height) {
        return x == 0 || y == 0 || x == width - 1 || y == height - 1;
    }
    
    // TODO: statt ws25 gamestate, position direkt rausholen für weniger visibility
    public static ClientFullMapNode findMyPosition(Map<PathFinder.Position, ClientFullMapNode> nodes) {

        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        for (ClientFullMapNode node : nodes.values()) {
            var pos = node.getPlayerPositionState();

            if (pos == EClientPositionState.MyPlayerPosition ||
                pos == EClientPositionState.BothPlayerPosition) {
                return node;
            }
        }

        return null;
    }
    
    public static boolean isFortPositionValid(EClientTerrain[][] grid, int fortX, int fortY) {

        if (fortX < 0 || fortY < 0) return false;
        if (fortY >= grid.length || fortX >= grid[0].length) return false;

        return grid[fortY][fortX] == EClientTerrain.Grass;
    }
    
    
    public static void printMap(Map<PathFinder.Position, ClientFullMapNode> nodes) {

        if (nodes == null || nodes.isEmpty()) {
            System.out.println("Map is empty");
            return;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (PathFinder.Position p : nodes.keySet()) {
            minX = Math.min(minX, p.x());
            maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = Math.max(maxY, p.y());
        }

        System.out.println("\n=== MAP DEBUG ===");

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                ClientFullMapNode node = nodes.get(new PathFinder.Position(x, y));

                if (node == null) {
                    System.out.print(" ? ");
                    continue;
                }

                char c;

                // Player  > TODO: hinzufuegen my node helpers . node.hasmyplayer zum bsp. 
                if (node.getPlayerPositionState() == EClientPositionState.MyPlayerPosition ||
                    node.getPlayerPositionState() == EClientPositionState.BothPlayerPosition) {
                    c = 'P';
                }
                // Fort
                else if (node.getFortState() == EClientFortState.EnemyFortPresent) {
                    c = 'F';
                }
                // Treasure
                else if (node.getTreasureState() == EClientTreasureState.MyTreasureIsPresent) {
                    c = 'T';
                }
                // Terrain
                else {
                    c = switch (node.getTerrain()) {
                        case Grass -> '.';
                        case Mountain -> 'M';
                        case Water -> 'W';
                    };
                }

                System.out.print(" " + c + " ");
            }
            System.out.println();
        }

        System.out.println("=================\n");
    }
    
}