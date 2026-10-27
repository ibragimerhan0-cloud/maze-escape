/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet sets = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int count = 0;
        for (int k = 0; k < w.count() && count < broken.length; k++) {
            if (sets.union(w.a()[k], w.b()[k])) {
                broken[count++] = k;
            }
        }
        return Arrays.copyOf(broken, count);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];
        for (int k = 0; k < order.length; k++) order[k] = k;
        Arrays.sort(order, Comparator.comparingInt(k -> w.cost()[k]));

        DisjointSet sets = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int count = 0;
        for (int k : order) {
            if (count == broken.length) break;
            if (sets.union(w.a()[k], w.b()[k])) {
                broken[count++] = k;
            }
        }
        return Arrays.copyOf(broken, count);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n <= 1) return 0;

        int[] degree = new int[n];
        for (int k = 0; k < w.count(); k++) {
            degree[w.a()[k]]++;
            degree[w.b()[k]]++;
        }
        int[][] edges = new int[n][];
        for (int u = 0; u < n; u++) edges[u] = new int[degree[u]];
        int[] filled = new int[n];
        for (int k = 0; k < w.count(); k++) {
            edges[w.a()[k]][filled[w.a()[k]]++] = k;
            edges[w.b()[k]][filled[w.b()[k]]++] = k;
        }

        boolean[] visited = new boolean[n];
        PriorityQueue<Integer> queue = new PriorityQueue<>(
                Comparator.comparingInt(k -> w.cost()[k]));
        visited[0] = true;
        int reached = 1;
        long total = 0;
        for (int k : edges[0]) queue.add(k);

        while (!queue.isEmpty() && reached < n) {
            int k = queue.remove();
            int a = w.a()[k];
            int b = w.b()[k];
            if (visited[a] && visited[b]) continue;

            int next = visited[a] ? b : a;
            visited[next] = true;
            reached++;
            total += w.cost()[k];
            for (int edge : edges[next]) {
                int other = w.a()[edge] == next ? w.b()[edge] : w.a()[edge];
                if (!visited[other]) queue.add(edge);
            }
        }
        return total;
    }
}
