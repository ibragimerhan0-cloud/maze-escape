/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        check(!d.union(2, 2), "union of a cell with itself returns false");
        check(d.count() == 3, "self-union does not change count");
        d.union(4, 5);
        d.union(0, 4);
        check(d.count() == 1, "all six cells are in one set");
        check(!d.union(5, 2), "union inside a connected set returns false");

        DisjointSet empty = new DisjointSet(0);
        check(empty.count() == 0, "empty DSU has zero sets");
        DisjointSet single = new DisjointSet(1);
        check(single.find(0) == 0 && !single.union(0, 0), "single-cell DSU");

        int n = 100_000;
        DisjointSet chain = new DisjointSet(n);
        boolean allJoined = true;
        for (int i = 1; i < n; i++) allJoined &= chain.union(i - 1, i);
        check(allJoined, "every union in a large chain joins different sets");
        check(chain.count() == 1, "one set after 99,999 unions");
        check(chain.connected(0, n - 1), "first and last cells of the chain are connected");
        check(!chain.union(n - 1, 0) && chain.count() == 1, "repeated large-chain union keeps count");

        DisjointSet balanced = new DisjointSet(16);
        for (int step = 1; step < 16; step *= 2) {
            for (int i = 0; i < 16; i += 2 * step) balanced.union(i, i + step);
        }
        boolean sameRoot = true;
        for (int i = 0; i < 16; i++) sameRoot &= balanced.find(i) == balanced.find(0);
        check(sameRoot && balanced.count() == 1, "balanced merges and repeated finds preserve one set");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
        if (failed != 0) throw new AssertionError(failed + " DSU checks failed");
    }
}
