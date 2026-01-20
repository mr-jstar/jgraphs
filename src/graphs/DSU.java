package graphs;

import java.util.Arrays;

/**
 * DSU / Union-Find with:
 *  - makeSet(x): initialize element x
 *  - find(x): representative (with path compression)
 *  - union(a,b): union by rank
 *
 * Elements are integers in [0..n-1].
 */
public final class DSU {
    private final int[] parent;
    private final int[] rank;   // "approximate height"

    public DSU(int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be > 0");
        this.parent = new int[n];
        this.rank = new int[n];
        // make-set for all elements
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    /** make-set(x): resets x to be a singleton set */
    public void makeSet(int x) {
        checkIndex(x);
        parent[x] = x;
        rank[x] = 0;
    }

    /** find(x): returns representative of the set containing x (path compression) */
    public int find(int x) {
        checkIndex(x);
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // path compression on the fly
        }
        return parent[x];
    }

    /**
     * union(a,b): merges sets containing a and b.
     * @return true if merged (they were different), false if already same set.
     */
    public boolean union(int a, int b) {
        checkIndex(a);
        checkIndex(b);

        int ra = find(a);
        int rb = find(b);
        if (ra == rb) return false;

        // union by rank
        if (rank[ra] < rank[rb]) {
            parent[ra] = rb;
        } else if (rank[ra] > rank[rb]) {
            parent[rb] = ra;
        } else {
            parent[rb] = ra;
            rank[ra]++;
        }
        return true;
    }

    /** convenience: are a and b in the same set? */
    public boolean sameSet(int a, int b) {
        return find(a) == find(b);
    }
    
    /** convenience: are a and b in the different sets? */
    public boolean differentSets(int a, int b) {
        return find(a) != find(b);
    }

    private void checkIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("x=" + x + " out of range [0.." + (parent.length - 1) + "]");
        }
    }

    @Override
    public String toString() {
        return "parent=" + Arrays.toString(parent) + "\nrank  =" + Arrays.toString(rank);
    }

    // quick demo
    public static void main(String[] args) {
        DSU dsu = new DSU(8);

        dsu.union(0, 1);
        dsu.union(1, 2);
        dsu.union(3, 4);
        dsu.union(4, 5);
        dsu.union(2, 5); // merge {0,1,2} with {3,4,5}

        System.out.println(dsu);
        System.out.println("sameSet(0,5) = " + dsu.sameSet(0, 5)); // true
        System.out.println("sameSet(6,7) = " + dsu.sameSet(6, 7)); // false
    }
}
