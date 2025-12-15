import java.util.*;

public class MeshTopoA {

    public interface Edge {
        int vertexA();
        int vertexB();
    }

    public static final class Reconstruction {
        public final int[][] triangles;        // T x 3, każdy wiersz: a<b<c
        public final boolean[] boundaryEdge;   // boundaryEdge[i] = czy edges.get(i) jest brzegowa
        public final int[] edgeTriCount;       // ile trójkątów przypada na daną krawędź (po kluczu)

        private Reconstruction(int[][] triangles, boolean[] boundaryEdge, int[] edgeTriCount) {
            this.triangles = triangles;
            this.boundaryEdge = boundaryEdge;
            this.edgeTriCount = edgeTriCount;
        }
    }

    // Minimalna “lista intów” do mapowania: klucz krawędzi -> indeksy w wejściowej liście
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

    // Klucz nieskierowanej krawędzi {u,v} spakowany w long
    private static long edgeKey(int u, int v) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    /**
     * Rekonstruuje trójkąty z listy krawędzi + oznacza krawędzie brzegowe.
     */
    public static Reconstruction reconstructTrianglesAndBoundary(List<Edge> edges) {
        // 1) Sąsiedztwo + mapowanie klucz krawędzi -> indeksy w liście wejściowej
        Map<Integer, HashSet<Integer>> adj = new HashMap<>(edges.size() * 2);
        HashMap<Long, IntList> keyToIndices = new HashMap<>(edges.size() * 2);

        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            int u = e.vertexA();
            int v = e.vertexB();
            if (u == v) continue; // ignoruj pętle własne

            adj.computeIfAbsent(u, k -> new HashSet<>()).add(v);
            adj.computeIfAbsent(v, k -> new HashSet<>()).add(u);

            long k = edgeKey(u, v);
            keyToIndices.computeIfAbsent(k, kk -> new IntList()).add(i);
        }

        // 2) Rekonstrukcja trójkątów + zliczanie incydencji krawędzi do trójkątów
        ArrayList<int[]> tris = new ArrayList<>();
        HashMap<Long, Integer> edgeTriCountByKey = new HashMap<>(edges.size() * 2);

        for (Edge e : edges) {
            int u = e.vertexA();
            int v = e.vertexB();
            if (u == v) continue;

            int a = Math.min(u, v);
            int b = Math.max(u, v);

            HashSet<Integer> Na = adj.get(a);
            HashSet<Integer> Nb = adj.get(b);
            if (Na == null || Nb == null) continue;

            HashSet<Integer> small = (Na.size() <= Nb.size()) ? Na : Nb;
            HashSet<Integer> large = (small == Na) ? Nb : Na;

            for (int w : small) {
                if (w == a || w == b) continue;
                if (!large.contains(w)) continue;

                // Kanonizacja: trójkąt dodajemy tylko raz jako (a<b<w)
                if (b < w) {
                    int c = w;
                    tris.add(new int[]{a, b, c});

                    // Zliczamy trzy krawędzie trójkąta
                    inc(edgeTriCountByKey, edgeKey(a, b));
                    inc(edgeTriCountByKey, edgeKey(a, c));
                    inc(edgeTriCountByKey, edgeKey(b, c));
                }
            }
        }

        // 3) Oznacz krawędzie brzegowe: count == 1
        boolean[] boundary = new boolean[edges.size()];

        // (opcjonalnie) wektor zliczeń dla krawędzi wejściowych, równoległy do listy edges
        int[] perInputEdgeCount = new int[edges.size()];

        for (Map.Entry<Long, IntList> entry : keyToIndices.entrySet()) {
            long k = entry.getKey();
            int cnt = edgeTriCountByKey.getOrDefault(k, 0);
            IntList idxs = entry.getValue();

            boolean isBoundary = (cnt == 1);
            for (int j = 0; j < idxs.size(); j++) {
                int idx = idxs.get(j);
                boundary[idx] = isBoundary;
                perInputEdgeCount[idx] = cnt;
            }
        }

        // 4) int[][] na wyjście
        int[][] triangles = new int[tris.size()][3];
        for (int i = 0; i < tris.size(); i++) triangles[i] = tris.get(i);

        return new Reconstruction(triangles, boundary, perInputEdgeCount);
    }

    private static void inc(HashMap<Long, Integer> map, long key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }
}


