/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
import java.util.Arrays;

public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        int[][] neighbours = Graphs.open(w, broken);
        int deadEnds = 0;
        for (int[] list : neighbours) {
            if (list.length == 1) deadEnds++;
        }
        if (n == 0) return new Result(new int[0], 0, deadEnds);

        boolean[] seen = new boolean[n];
        int[] nextDirection = new int[n];
        int[] stack = new int[n];
        int top = 0;
        stack[0] = 0;
        seen[0] = true;
        int visited = 1;

        while (top >= 0) {
            int u = stack[top];
            if (u == n - 1) {
                return new Result(Arrays.copyOf(stack, top + 1), visited, deadEnds);
            }
            if (nextDirection[u] == 4) {
                top--;
                continue;
            }

            int direction = nextDirection[u]++;
            int next = switch (direction) {
                case 0 -> u >= w.cols() ? u - w.cols() : -1;
                case 1 -> u % w.cols() + 1 < w.cols() ? u + 1 : -1;
                case 2 -> u + w.cols() < n ? u + w.cols() : -1;
                default -> u % w.cols() > 0 ? u - 1 : -1;
            };
            if (next == -1 || seen[next]) continue;

            for (int v : neighbours[u]) {
                if (v == next) {
                    seen[v] = true;
                    visited++;
                    stack[++top] = v;
                    break;
                }
            }
        }
        return new Result(new int[0], visited, deadEnds);
    }
}
