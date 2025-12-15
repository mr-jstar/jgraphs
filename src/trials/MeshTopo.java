import java.util.*;

public class MeshTopo {

    public interface Edge {
        int vertexA();
        int vertexB();
    }

    /** Klucz krawędzi nieskierowanej: (u<v). */
    public static final class EdgeKey {
        public final int u;  // mniejszy
        public final int v;  // większy

        public EdgeKey(int a, int b) {
            if (a == b) throw new IllegalArgumentException("Self-loop edge: " + a);
            this.u = Math.min(a, b);
            this.v = Math.max(a, b);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EdgeKey)) return false;
            EdgeKey other = (EdgeKey) o;
            return u == other.u && v == other.v;
        }

        @Override public int hashCode() {
            // prosta i szybka kombinacja
            return 31 * u + v;
        }

        @Override public String toString() { return "(" + u + "," + v + ")"; }
    }

    /** Wynik: trójkąty + krawędzie brzegowe + liczniki incydencji (przydaje się do walidacji). */
    public static final class MeshTopology {
        public final int[][] triangles;                 // T x 3, w każdym wierszu a<b<c
        public final List<EdgeKey> boundaryEdges;       // krawędzie z count==1
        public final Map<EdgeKey, Integer> edgeUseCount;// ile trójkątów używa danej krawędzi

        public MeshTopology(int[][] triangles,
                            List<EdgeKey> boundaryEdges,
                            Map<EdgeKey, Integer> edgeUseCount) {
            this.triangles = triangles;
            this.boundaryEdges = boundaryEdges;
            this.edgeUseCount = edgeUseCount;
        }
    }

    /**
     * Rekonstrukcja trójkątów (3-cykli) + oznaczenie krawędzi brzegowych.
     *
     * Założenia:
     * - graf nieskierowany, krawędzie opisują topologię siatki trójkątnej
     * - duplikaty krawędzi w input nie przeszkadzają (HashSet w sąsiedztwie je zneutralizuje)
     */
    public static MeshTopology reconstructTrianglesAndBoundary(List<Edge> edges) {
        // 1) Sąsiedztwo
        Map<Integer, HashSet<Integer>> adj = new HashMap<>(edges.size() * 2);

        for (Edge e : edges) {
            int u = e.vertexA();
            int v = e.vertexB();
            if (u == v) continue; // ignoruj pętle własne, jeśli jakieś są

            adj.computeIfAbsent(u, k -> new HashSet<>()).add(v);
            adj.computeIfAbsent(v, k -> new HashSet<>()).add(u);
        }

        // 2) Trójkąty przez przecięcie sąsiadów na każdej krawędzi
        ArrayList<int[]> tris = new ArrayList<>();

        // Jeśli w input są duplikaty krawędzi, to poniższa pętla doda duplikaty trójkątów.
        // Dlatego iterujemy po unikalnych EdgeKey z mapy sąsiedztwa:
        HashSet<EdgeKey> uniqueEdges = new HashSet<>(edges.size() * 2);
        for (Edge e : edges) {
            int u = e.vertexA(), v = e.vertexB();
            if (u == v) continue;
            uniqueEdges.add(new EdgeKey(u, v));
        }

        for (EdgeKey ek : uniqueEdges) {
            int a = ek.u;
            int b = ek.v;

            HashSet<Integer> Na = adj.get(a);
            HashSet<Integer> Nb = adj.get(b);
            if (Na == null || Nb == null) continue;

            HashSet<Integer> small = (Na.size() <= Nb.size()) ? Na : Nb;
            HashSet<Integer> large = (small == Na) ? Nb : Na;

            for (int w : small) {
                if (w == a || w == b) continue;
                if (!large.contains(w)) continue;

                // kanonicznie a<b<w — wtedy każdy trójkąt pojawi się raz
                if (b < w) {
                    int c = w;
                    // a<b oraz b<c gwarantuje a<b<c
                    tris.add(new int[]{a, b, c});
                }
            }
        }

        int[][] triangles = new int[tris.size()][3];
        for (int i = 0; i < tris.size(); i++) triangles[i] = tris.get(i);

        // 3) Zliczanie użyć krawędzi przez trójkąty
        HashMap<EdgeKey, Integer> edgeUse = new HashMap<>(triangles.length * 3 * 2);

        for (int[] t : triangles) {
            int a = t[0], b = t[1], c = t[2];
            inc(edgeUse, new EdgeKey(a, b));
            inc(edgeUse, new EdgeKey(b, c));
            inc(edgeUse, new EdgeKey(a, c));
        }

        // 4) Krawędzie brzegowe: count == 1
        ArrayList<EdgeKey> boundary = new ArrayList<>();
        for (Map.Entry<EdgeKey, Integer> en : edgeUse.entrySet()) {
            if (en.getValue() == 1) boundary.add(en.getKey());
        }

        // (opcjonalnie) możesz posortować dla stabilności:
        boundary.sort(Comparator.<EdgeKey>comparingInt(e -> e.u).thenComparingInt(e -> e.v));

        return new MeshTopology(triangles, boundary, edgeUse);
    }

    private static void inc(HashMap<EdgeKey, Integer> map, EdgeKey key) {
        map.merge(key, 1, Integer::sum);
    }
}

