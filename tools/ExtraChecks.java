import java.util.Arrays;
import java.util.Random;

public class ExtraChecks {
    static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    static void smallMazes() {
        for (int rows = 1; rows <= 7; rows++) {
            for (int cols = 1; cols <= 7; cols++) {
                Input.Walls w = Generator.generate(rows, cols, rows * 100L + cols);
                int n = w.cells();
                int[] broken = Maze.breakWalls(w);
                require(broken.length == n - 1, "Tree edge count");
                long cost = 0;
                for (int k : Maze.cheapestMaze(w)) cost += w.cost()[k];
                long[] best = new long[n];
                Arrays.fill(best, Long.MAX_VALUE);
                best[0] = 0;
                boolean[] used = new boolean[n];
                long reference = 0;
                for (int step = 0; step < n; step++) {
                    int u = -1;
                    for (int v = 0; v < n; v++) {
                        if (!used[v] && (u == -1 || best[v] < best[u])) u = v;
                    }
                    require(best[u] != Long.MAX_VALUE, "Connected grid");
                    reference += best[u];
                    used[u] = true;
                    for (int k = 0; k < w.count(); k++) {
                        int v = w.a()[k] == u ? w.b()[k] : w.b()[k] == u ? w.a()[k] : -1;
                        if (v >= 0 && !used[v]) best[v] = Math.min(best[v], w.cost()[k]);
                    }
                }
                require(cost == reference && Maze.primCost(w) == reference, "MST reference");
                Escape.Result result = Escape.escape(w, broken);
                require(result.path()[0] == 0 && result.path()[result.path().length - 1] == n - 1, "Path endpoints");
                int[][] graph = Graphs.open(w, broken);
                int[] expected = new int[n];
                boolean[] seen = new boolean[n];
                int[] length = {0}, visited = {0};
                require(referenceDfs(0, n - 1, w.cols(), graph, seen, expected, 0, length, visited), "Reference path");
                require(Arrays.equals(result.path(), Arrays.copyOf(expected, length[0])), "DFS path order");
                require(result.visited() == visited[0], "DFS marking count");
                int deadEnds = 0;
                for (int[] neighbours : graph) if (neighbours.length == 1) deadEnds++;
                require(result.deadEnds() == deadEnds, "Dead ends");
            }
        }
        System.out.println("PASS: 49 small grids, independent MST and recursive DFS references");
    }

    // Only the tiny reference uses recursion; submitted algorithms use explicit stacks.
    static boolean referenceDfs(int u, int goal, int cols, int[][] graph, boolean[] seen,
                                int[] path, int depth, int[] length, int[] visited) {
        seen[u] = true;
        visited[0]++;
        path[depth] = u;
        if (u == goal) { length[0] = depth + 1; return true; }
        int[] targets = {u - cols, u % cols + 1 < cols ? u + 1 : -1,
                         u + cols, u % cols > 0 ? u - 1 : -1};
        for (int target : targets) {
            for (int v : graph[u]) {
                if (v == target && !seen[v] && referenceDfs(v, goal, cols, graph, seen, path, depth + 1, length, visited)) return true;
            }
        }
        return false;
    }

    static void randomSlides() {
        Random random = new Random(250767);
        for (int run = 0; run < 150; run++) {
            int n = 1 + random.nextInt(24);
            int m = n == 1 ? 0 : n - 1 + random.nextInt(n * 4);
            int[] from = new int[m], to = new int[m], time = new int[m];
            boolean[][] reach = new boolean[n][n];
            for (int i = 0; i < n; i++) reach[i][i] = true;
            for (int i = 0; i < m; i++) {
                from[i] = i < n - 1 ? i : random.nextInt(n);
                to[i] = i < n - 1 ? i + 1 : random.nextInt(n - 1);
                if (i >= n - 1 && to[i] >= from[i]) to[i]++;
                time[i] = 1 + random.nextInt(100);
                reach[from[i]][to[i]] = true;
            }
            for (int k = 0; k < n; k++) for (int a = 0; a < n; a++) for (int b = 0; b < n; b++)
                reach[a][b] |= reach[a][k] && reach[k][b];
            Input.Slides s = new Input.Slides(n, from, to, time);
            int[] zone = Zones.kosaraju(s);
            int count = Arrays.stream(zone).max().orElse(-1) + 1;
            for (int a = 0; a < n; a++) for (int b = 0; b < n; b++)
                require((zone[a] == zone[b]) == (reach[a][b] && reach[b][a]), "SCC reachability oracle");
            for (int i = 0; i < m; i++) require(zone[from[i]] <= zone[to[i]], "Topological zone IDs");

            // Bellman-Ford on cells: all movement inside a reference SCC costs zero.
            long[] dist = new long[n];
            Arrays.fill(dist, Long.MAX_VALUE);
            dist[0] = 0;
            int[] longest = new int[count];
            Arrays.fill(longest, 1);
            for (int pass = 0; pass < n; pass++) {
                for (int i = 0; i < m; i++) {
                    int a = from[i], b = to[i];
                    long cost = reach[a][b] && reach[b][a] ? 0 : time[i];
                    if (dist[a] != Long.MAX_VALUE) dist[b] = Math.min(dist[b], dist[a] + cost);
                    if (zone[a] != zone[b]) longest[zone[b]] = Math.max(longest[zone[b]], longest[zone[a]] + 1);
                }
            }
            long[] route = Zones.routes(s, zone, count);
            require(route[1] == dist[n - 1], "Shortest route reference");
            require(route[0] == Arrays.stream(longest).max().orElse(0), "Longest chain reference");
        }
        System.out.println("PASS: 150 random directed graphs, reachability and Bellman-Ford references");

        Input.Slides duplicate = new Input.Slides(5,
                new int[]{0, 0, 0, 1, 2, 2, 3},
                new int[]{1, 1, 4, 4, 1, 3, 1},
                new int[]{20, 3, 30, 4, 1, 1, 1});
        int[] z = Zones.kosaraju(duplicate);
        require(Arrays.equals(Zones.routes(duplicate, z, 5), new long[]{4, 7}),
                "Parallel minimum edge, global chain and unreachable source");
        Input.Slides largeCost = new Input.Slides(3, new int[]{0, 1}, new int[]{1, 2},
                new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE});
        require(Zones.routes(largeCost, Zones.kosaraju(largeCost), 3)[1] == 4294967294L, "Long cost arithmetic");
        System.out.println("PASS: duplicate slides, unreachable incoming zones and costs above int range");
    }

    static void millionCellMaze() {
        int rows = 1000, cols = 1000, n = rows * cols;
        int[] a = new int[n - 1], b = new int[n - 1], cost = new int[n - 1], broken = new int[n - 1];
        int previous = 0, k = 0;
        for (int r = 0; r < rows; r++) {
            for (int j = 0; j < cols; j++) {
                int c = r % 2 == 0 ? j : cols - 1 - j;
                int current = r * cols + c;
                if (r != 0 || j != 0) {
                    a[k] = previous; b[k] = current; cost[k] = k + 1; broken[k] = k; k++;
                }
                previous = current;
            }
        }
        Escape.Result result = Escape.escape(new Input.Walls(rows, cols, a, b, cost), broken);
        require(result.path().length == 999001 && result.visited() == 999001 && result.deadEnds() == 2,
                "Million-cell snake maze");
        System.out.println("PASS: 1000 x 1000 snake maze, DFS depth 999001 without stack overflow");
    }

    static void millionCellSlides() {
        int n = 1_000_000;
        int[] from = new int[n - 1], to = new int[n - 1], time = new int[n - 1];
        for (int i = 0; i < n - 1; i++) { from[i] = i; to[i] = i + 1; time[i] = 1; }
        int[] zone = Zones.kosaraju(new Input.Slides(n, from, to, time));
        for (int i = 0; i < n; i++) require(zone[i] == i, "Million-cell Kosaraju chain");
        System.out.println("PASS: 1000000-cell directed chain, iterative Kosaraju");
    }

    public static void main(String[] args) {
        smallMazes();
        randomSlides();
        millionCellMaze();
        millionCellSlides();
        System.out.println("All additional checks passed.");
    }
}
