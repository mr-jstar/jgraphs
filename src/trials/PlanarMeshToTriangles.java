/*
Założenia (realistyczne dla siatek FEM / manifold 2D):

wejściem jest graf nieskierowany jako lista EdgeInterface

siatka może zawierać:

trójkąty (wykrywane jako 3-cykle),

czworokąty bez przekątnej (wykrywane jako chordless 4-cycle),

większe wielokąty są trudne do odtworzenia z samego grafu bez embeddingu – więc:

jeśli są już rozcięte na trójkąty (czyli w grafie istnieją przekątne → 3-cykle), to wszystko działa,

jeśli masz „czysty” ngon (>4) bez żadnych przekątnych i bez geometrii – z samej topologii nie da się jednoznacznie odtworzyć ścian w ogólnym przypadku.

Klasa robi:

buduje sąsiedztwo,

wykrywa trójkąty,

wykrywa quady bez przekątnej,

usuwa trójkąty, które leżą w wykrytych quadach (żeby nie dublować),

trianguluje quady,

wyznacza krawędzie brzegowe (countFaces==1) dla oryginalnej listy krawędzi.
*/

import  graphs.*;

import java.util.*;

/**
 * Reconstructs (as triangles) a planar 2D-manifold mesh given only an undirected edge list,
 * WITHOUT coordinates and WITHOUT external libraries.
 *
 * What it can reconstruct robustly:
 *  - Triangles: detected as 3-cycles in the graph.
 *  - Quads without diagonal: detected as chordless 4-cycles (a-b-c-d-a) with no (a-c) and no (b-d).
 *    Such quads are then triangulated by adding one diagonal (chosen deterministically).
 *
 * Limits:
 *  - General ngons (>4) WITHOUT diagonals cannot be uniquely reconstructed from the graph alone
 *    unless you have a planar embedding (rotation system) or coordinates.
 */
public final class PlanarMeshToTriangles {

    private PlanarMeshToTriangles() {}

    public static final class Result {
        /** All elements represented as triangles (each row is 3 vertex ids). */
        public final int[][] triangles;

        /** For each input edge (same index as input list), true if it is a boundary edge. */
        public final boolean[] boundaryEdge;

        /** For each input edge, how many reconstructed faces touch it (0/1/2/...). */
        public final int[] edgeFaceCount;

        /** Optional: reconstructed quads (each row is 4 vertex ids in cycle order a-b-c-d). */
        public final int[][] quads;

        private Result(int[][] triangles, boolean[] boundaryEdge, int[] edgeFaceCount, int[][] quads) {
            this.triangles = triangles;
            this.boundaryEdge = boundaryEdge;
            this.edgeFaceCount = edgeFaceCount;
            this.quads = quads;
        }
    }

    /**
     * Main entry.
     *
     * @param edges edge list of an undirected graph
     * @param alsoDetectQuads if true, detect chordless quads without diagonals and triangulate them
     */
    public static Result reconstruct(Set<EdgeInterface> edgesSet, boolean alsoDetectQuads) {
        List<EdgeInterface> edges = new ArrayList<>(edgesSet);
        // ---- build adjacency and edge maps ----
        Map<Integer, IntHashSet> adj = new HashMap<>(edges.size() * 2);
        HashSet<Long> edgeSet = new HashSet<>(edges.size() * 2);
        HashMap<Long, IntList> keyToInputIdx = new HashMap<>(edges.size() * 2);

        for (int i = 0; i < edges.size(); i++) {
            EdgeInterface e = edges.get(i);
            int u = e.getVertexA();
            int v = e.getVertexB();
            if (u == v) continue;

            long k = edgeKey(u, v);
            edgeSet.add(k);
            keyToInputIdx.computeIfAbsent(k, kk -> new IntList()).add(i);

            adj.computeIfAbsent(u, kk -> new IntHashSet()).add(v);
            adj.computeIfAbsent(v, kk -> new IntHashSet()).add(u);
        }

        // ---- 1) triangles from 3-cycles ----
        ArrayList<Tri> tris = new ArrayList<>();
        HashSet<Tri> triSet = new HashSet<>();

        // iterate edges and intersect neighbor sets
        for (EdgeInterface e : edges) {
            int u0 = e.getVertexA();
            int v0 = e.getVertexB();
            if (u0 == v0) continue;

            int a = Math.min(u0, v0);
            int b = Math.max(u0, v0);

            IntHashSet Na = adj.get(a);
            IntHashSet Nb = adj.get(b);
            if (Na == null || Nb == null) continue;

            // iterate smaller set
            IntHashSet small = (Na.size() <= Nb.size()) ? Na : Nb;
            IntHashSet large = (small == Na) ? Nb : Na;

            for (int w : small.values()) {
                if (w == a || w == b) continue;
                if (!large.contains(w)) continue;

                // canonical (x<y<z)
                int x = a, y = b, z = w;
                if (z < y) { int tmp = z; z = y; y = tmp; }
                if (y < x) { int tmp = y; y = x; x = tmp; }
                if (z < y) { int tmp = z; z = y; y = tmp; }

                Tri t = new Tri(x, y, z);
                if (triSet.add(t)) tris.add(t);
            }
        }

        // ---- 2) chordless quads (optional) ----
        ArrayList<Quad> quads = new ArrayList<>();
        if (alsoDetectQuads) {
            quads = detectChordlessQuads(adj, edgeSet);
            // remove triangles that are exactly the two halves of a detected quad
            // (quad a-b-c-d => potential triangle halves are (a,b,c) and (a,c,d) if diagonal a-c existed,
            //  but since diagonal does NOT exist, those triangles won't be detected. However, in messy input
            //  you might have both patterns due to extra edges; we defensively remove triangle subsets.)
            if (!quads.isEmpty()) {
                HashSet<Tri> toRemove = new HashSet<>();
                for (Quad q : quads) {
                    // any triangle using 3 vertices out of quad that forms a 3-cycle in the graph is "inside"
                    int a = q.a, b = q.b, c = q.c, d = q.d;
                    // Try all 4 choose 3 triplets (if they exist as triangles in triSet)
                    maybeMark(toRemove, triSet, a, b, c);
                    maybeMark(toRemove, triSet, b, c, d);
                    maybeMark(toRemove, triSet, c, d, a);
                    maybeMark(toRemove, triSet, d, a, b);
                }
                if (!toRemove.isEmpty()) {
                    triSet.removeAll(toRemove);
                    tris.removeIf(toRemove::contains);
                }
            }
        }

        // ---- 3) Build face-edge incidence counts from reconstructed faces (triangles + quads) ----
        HashMap<Long, Integer> edgeFaceCountByKey = new HashMap<>(edges.size() * 2);

        // triangles are faces
        for (Tri t : tris) {
            inc(edgeFaceCountByKey, edgeKey(t.a, t.b));
            inc(edgeFaceCountByKey, edgeKey(t.b, t.c));
            inc(edgeFaceCountByKey, edgeKey(t.c, t.a));
        }

        // quads are faces (before triangulation)
        for (Quad q : quads) {
            inc(edgeFaceCountByKey, edgeKey(q.a, q.b));
            inc(edgeFaceCountByKey, edgeKey(q.b, q.c));
            inc(edgeFaceCountByKey, edgeKey(q.c, q.d));
            inc(edgeFaceCountByKey, edgeKey(q.d, q.a));
        }

        // ---- 4) boundary edges for input list: count == 1 ----
        boolean[] boundary = new boolean[edges.size()];
        int[] perInputCnt = new int[edges.size()];

        for (Map.Entry<Long, IntList> en : keyToInputIdx.entrySet()) {
            long k = en.getKey();
            int cnt = edgeFaceCountByKey.getOrDefault(k, 0);
            boolean isBoundary = (cnt == 1);

            IntList idxs = en.getValue();
            for (int j = 0; j < idxs.size(); j++) {
                int idx = idxs.get(j);
                boundary[idx] = isBoundary;
                perInputCnt[idx] = cnt;
            }
        }

        // ---- 5) final triangulation output: triangles + triangulated quads ----
        ArrayList<int[]> outTris = new ArrayList<>(tris.size() + 2 * quads.size());

        for (Tri t : tris) {
            outTris.add(new int[]{t.a, t.b, t.c});
        }

        for (Quad q : quads) {
            // deterministically pick a diagonal: connect min vertex to the opposite one in the cycle
            // quad cycle: a-b-c-d-a
            // candidates: (a,c) or (b,d). Choose the one with smaller undirectedKey.
            long kAC = edgeKey(q.a, q.c);
            long kBD = edgeKey(q.b, q.d);

            if (kAC <= kBD) {
                outTris.add(new int[]{q.a, q.b, q.c});
                outTris.add(new int[]{q.a, q.c, q.d});
            } else {
                outTris.add(new int[]{q.b, q.c, q.d});
                outTris.add(new int[]{q.b, q.d, q.a});
            }
        }

        int[][] triArr = new int[outTris.size()][3];
        for (int i = 0; i < outTris.size(); i++) triArr[i] = outTris.get(i);

        int[][] quadArr = new int[quads.size()][4];
        for (int i = 0; i < quads.size(); i++) {
            Quad q = quads.get(i);
            quadArr[i] = new int[]{q.a, q.b, q.c, q.d};
        }

        return new Result(triArr, boundary, perInputCnt, quadArr);
    }

    // ----------------- Quad detection -----------------

    /**
     * Detect chordless 4-cycles a-b-c-d-a such that:
     *  - edges (a,b), (b,c), (c,d), (d,a) exist
     *  - diagonals (a,c) and (b,d) do NOT exist
     *
     * Returns quads in canonical rotation (start at smallest vertex, choose direction deterministically).
     */
    private static ArrayList<Quad> detectChordlessQuads(Map<Integer, IntHashSet> adj,
                                                        HashSet<Long> edgeSet) {
        HashSet<Quad> quadSet = new HashSet<>();
        ArrayList<Quad> out = new ArrayList<>();

        // For each oriented edge (a-b), try to complete a-b-c-d-a
        for (Map.Entry<Integer, IntHashSet> en : adj.entrySet()) {
            int a = en.getKey();
            IntHashSet Na = en.getValue();
            if (Na == null) continue;

            for (int b : Na.values()) {
                if (b == a) continue;

                IntHashSet Nb = adj.get(b);
                if (Nb == null) continue;

                // pick c neighbor of b, c != a
                for (int c : Nb.values()) {
                    if (c == a || c == b) continue;
                    // diagonal a-c must NOT exist
                    if (edgeSet.contains(edgeKey(a, c))) continue;

                    IntHashSet Nc = adj.get(c);
                    if (Nc == null) continue;

                    // pick d neighbor of c, d != b and must connect back to a
                    for (int d : Nc.values()) {
                        if (d == a || d == b || d == c) continue;
                        if (!edgeSet.contains(edgeKey(d, a))) continue;

                        // diagonal b-d must NOT exist
                        if (edgeSet.contains(edgeKey(b, d))) continue;

                        // also ensure we truly have the 4-cycle edges (a,b),(b,c),(c,d),(d,a)
                        if (!edgeSet.contains(edgeKey(a, b))) continue;
                        if (!edgeSet.contains(edgeKey(b, c))) continue;
                        if (!edgeSet.contains(edgeKey(c, d))) continue;

                        Quad q = Quad.canonical(a, b, c, d);
                        if (quadSet.add(q)) out.add(q);
                    }
                }
            }
        }

        return out;
    }

    // ----------------- Helpers -----------------

    private static void maybeMark(Set<Tri> toRemove, Set<Tri> triSet, int x, int y, int z) {
        Tri t = Tri.ofUnsorted(x, y, z);
        if (triSet.contains(t)) toRemove.add(t);
    }

    private static void inc(HashMap<Long, Integer> map, long key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }

    private static long edgeKey(int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    // ----------------- Small int collections (no boxing) -----------------

    /** Minimal int-list for mapping an undirected edge to multiple input indices (duplicates). */
    private static final class IntList {
        int[] a = new int[2];
        int n = 0;
        void add(int x) {
            if (n == a.length) a = Arrays.copyOf(a, a.length * 2);
            a[n++] = x;
        }
        int get(int i) { return a[i]; }
        int size() { return n; }
    }

    /**
     * Tiny int set with open addressing for small degrees (mesh graphs typically have small degree).
     * Not a full-featured HashSet; enough for contains/add/iterate.
     */
    private static final class IntHashSet {
        private static final int EMPTY = Integer.MIN_VALUE;
        private int[] table;
        private int size;

        IntHashSet() {
            table = new int[16];
            Arrays.fill(table, EMPTY);
        }

        int size() { return size; }

        boolean contains(int x) {
            int[] t = table;
            int m = t.length - 1;
            int i = mix(x) & m;
            while (true) {
                int v = t[i];
                if (v == EMPTY) return false;
                if (v == x) return true;
                i = (i + 1) & m;
            }
        }

        void add(int x) {
            if ((size + 1) * 10 >= table.length * 7) rehash(); // load ~0.7
            int[] t = table;
            int m = t.length - 1;
            int i = mix(x) & m;
            while (true) {
                int v = t[i];
                if (v == EMPTY) {
                    t[i] = x;
                    size++;
                    return;
                }
                if (v == x) return;
                i = (i + 1) & m;
            }
        }

        int[] values() {
            int[] out = new int[size];
            int k = 0;
            for (int v : table) if (v != EMPTY) out[k++] = v;
            return out;
        }

        private void rehash() {
            int[] old = table;
            int[] neu = new int[old.length * 2];
            Arrays.fill(neu, EMPTY);
            table = neu;
            size = 0;
            for (int v : old) {
                if (v != EMPTY) add(v);
            }
        }

        private static int mix(int x) {
            x ^= (x >>> 16);
            x *= 0x7feb352d;
            x ^= (x >>> 15);
            x *= 0x846ca68b;
            x ^= (x >>> 16);
            return x;
        }
    }

    // ----------------- Triangle / Quad value objects -----------------

    private static final class Tri {
        final int a, b, c; // a<b<c

        Tri(int a, int b, int c) {
            this.a = a; this.b = b; this.c = c;
        }

        static Tri ofUnsorted(int x, int y, int z) {
            int a = x, b = y, c = z;
            // sort 3 ints
            if (b < a) { int t = a; a = b; b = t; }
            if (c < b) { int t = b; b = c; c = t; }
            if (b < a) { int t = a; a = b; b = t; }
            return new Tri(a, b, c);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Tri)) return false;
            Tri t = (Tri) o;
            return a == t.a && b == t.b && c == t.c;
        }

        @Override public int hashCode() {
            int h = a;
            h = 31 * h + b;
            h = 31 * h + c;
            return h;
        }
    }

    private static final class Quad {
        final int a, b, c, d; // in cycle order a-b-c-d-a, canonicalized

        Quad(int a, int b, int c, int d) {
            this.a = a; this.b = b; this.c = c; this.d = d;
        }

        static Quad canonical(int a, int b, int c, int d) {
            // rotate so that smallest vertex is first
            int[] v = new int[]{a, b, c, d};
            int minPos = 0;
            for (int i = 1; i < 4; i++) if (v[i] < v[minPos]) minPos = i;

            int[] r1 = new int[]{
                    v[minPos],
                    v[(minPos + 1) & 3],
                    v[(minPos + 2) & 3],
                    v[(minPos + 3) & 3]
            };
            // also consider reverse direction (still a cycle)
            int[] r2 = new int[]{
                    v[minPos],
                    v[(minPos + 3) & 3],
                    v[(minPos + 2) & 3],
                    v[(minPos + 1) & 3]
            };

            // choose lexicographically smaller of r1 vs r2
            for (int i = 0; i < 4; i++) {
                if (r1[i] < r2[i]) return new Quad(r1[0], r1[1], r1[2], r1[3]);
                if (r1[i] > r2[i]) return new Quad(r2[0], r2[1], r2[2], r2[3]);
            }
            return new Quad(r1[0], r1[1], r1[2], r1[3]);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Quad)) return false;
            Quad q = (Quad) o;
            return a == q.a && b == q.b && c == q.c && d == q.d;
        }

        @Override public int hashCode() {
            int h = a;
            h = 31 * h + b;
            h = 31 * h + c;
            h = 31 * h + d;
            return h;
        }
    }
}

