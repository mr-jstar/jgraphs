import java.util.*;

/**
 * Uogólniona rekonstrukcja elementów siatki planarne: wydobywa ściany (trójkąty/czworokąty/ngony)
 * z rotation system (kolejność sąsiadów wokół wierzchołka), a następnie trianguluje każdą ścianę.
 *
 * Rotation system: rotation.get(v) = int[] sąsiadów v w kolejności CCW.
 */
public class PlanarMeshReconstruction {

    public interface Edge {
        int vertexA();
        int vertexB();
    }

    public static final class Result {
        public final int[][] triangles;        // wszystkie elementy jako trójkąty (a,b,c) bez gwarancji sortowania
        public final List<int[]> faces;        // ściany jako cykle wierzchołków (bez powtórzenia pierwszego na końcu)
        public final boolean[] boundaryEdge;   // oznaczenie wejściowych krawędzi (brzeg = true)
        public final int[] edgeFaceCount;      // ile ścian dotyka danej krawędzi wejściowej (0/1/2/...)
        Result(int[][] triangles, List<int[]> faces, boolean[] boundaryEdge, int[] edgeFaceCount) {
            this.triangles = triangles;
            this.faces = faces;
            this.boundaryEdge = boundaryEdge;
            this.edgeFaceCount = edgeFaceCount;
        }
    }

    // --- narzędzia: klucz krawędzi nieskierowanej i półkrawędzi ---
    private static long undirectedKey(int u, int v) {
        int a = Math.min(u, v), b = Math.max(u, v);
        return (((long)a) << 32) | (b & 0xffffffffL);
    }
    private static long directedKey(int from, int to) {
        return (((long)from) << 32) | (to & 0xffffffffL);
    }

    private static final class IntList {
        int[] a = new int[2];
        int n = 0;
        void add(int x){ if(n==a.length) a=Arrays.copyOf(a, a.length*2); a[n++]=x; }
        int get(int i){ return a[i]; }
        int size(){ return n; }
    }

    /**
     * Główne API.
     *
     * @param edges lista krawędzi grafu (nieskierowane)
     * @param rotation rotation system: dla każdego wierzchołka v -> sąsiedzi w CCW
     */
    public static Result reconstructAndTriangulate(List<Edge> edges,
                                                   Map<Integer, int[]> rotation) {
        // 1) mapowanie wejściowych krawędzi do indeksów (na wypadek duplikatów)
        HashMap<Long, IntList> keyToIndices = new HashMap<>(edges.size() * 2);
        // 2) szybkie sprawdzanie istnienia krawędzi w grafie
        HashSet<Long> undirectedEdgeSet = new HashSet<>(edges.size() * 2);

        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            int u = e.vertexA(), v = e.vertexB();
            if (u == v) continue;
            long uk = undirectedKey(u, v);
            undirectedEdgeSet.add(uk);
            keyToIndices.computeIfAbsent(uk, k -> new IntList()).add(i);
        }

        // 3) budujemy funkcję nextHalfEdge: dla półkrawędzi (u->v) zwraca (v->w),
        //    gdzie w jest "następnym" sąsiadem po u w kolejności CCW wokół v.
        HashMap<Long, Long> next = new HashMap<>(edges.size() * 4);
        for (Map.Entry<Integer, int[]> entry : rotation.entrySet()) {
            int v = entry.getKey();
            int[] neigh = entry.getValue();
            if (neigh == null || neigh.length == 0) continue;

            // mapa: neighbor -> pozycja w tablicy CCW, żeby O(1) znaleźć "następnego"
            HashMap<Integer, Integer> pos = new HashMap<>(neigh.length * 2);
            for (int i = 0; i < neigh.length; i++) pos.put(neigh[i], i);

            // dla każdego sąsiada u: next(u->v) = v -> nextCCWAfter(u)
            for (int u : neigh) {
                Integer p = pos.get(u);
                if (p == null) continue;
                int w = neigh[(p + 1) % neigh.length];
                // półkrawędź (u->v) musi istnieć w grafie
                if (!undirectedEdgeSet.contains(undirectedKey(u, v))) continue;
                // półkrawędź (v->w) musi istnieć w grafie
                if (!undirectedEdgeSet.contains(undirectedKey(v, w))) continue;

                next.put(directedKey(u, v), directedKey(v, w));
            }
        }

        // 4) obchód ścian: każda półkrawędź należy do dokładnie jednej ściany po lewej stronie
        HashSet<Long> usedHalfEdges = new HashSet<>(edges.size() * 4);
        ArrayList<int[]> faces = new ArrayList<>();

        for (Edge e : edges) {
            int a = e.vertexA(), b = e.vertexB();
            if (a == b) continue;

            // startujemy z obu kierunków
            collectFaceIfAny(a, b, next, usedHalfEdges, faces);
            collectFaceIfAny(b, a, next, usedHalfEdges, faces);
        }

        // 5) (opcjonalnie) odfiltruj "zewnętrzną" ścianę.
        // Bez geometrii najbezpieczniej zostawić wszystkie i ewentualnie usuwać największą długością.
        // Tu: usuwamy najdłuższy cykl (często odpowiada zewnętrznej granicy).
        if (!faces.isEmpty()) {
            int maxLen = -1, idx = -1;
            for (int i = 0; i < faces.size(); i++) {
                int len = faces.get(i).length;
                if (len > maxLen) { maxLen = len; idx = i; }
            }
            // jeśli graf ma brzeg, najdłuższa ściana bywa "outside face"
            // ale jeśli masz "dziury", to może nie być idealne – wtedy tego nie rób.
            faces.remove(idx);
        }

        // 6) triangulacja każdej ściany (fan triangulation)
        ArrayList<int[]> triangles = new ArrayList<>();
        HashMap<Long, Integer> edgeFaceCountByKey = new HashMap<>(edges.size() * 2);

        for (int[] face : faces) {
            int k = face.length;
            if (k < 3) continue;

            // zlicz krawędzie ściany
            for (int i = 0; i < k; i++) {
                int u = face[i];
                int v = face[(i + 1) % k];
                edgeFaceCountByKey.merge(undirectedKey(u, v), 1, Integer::sum);
            }

            // triangulacja: (v0, vi, vi+1)
            int v0 = face[0];
            for (int i = 1; i + 1 < k; i++) {
                triangles.add(new int[]{v0, face[i], face[i + 1]});
            }
        }

        // 7) oznacz krawędzie brzegowe w wejściowej liście: count == 1
        boolean[] boundary = new boolean[edges.size()];
        int[] perInputCount = new int[edges.size()];

        for (Map.Entry<Long, IntList> entry : keyToIndices.entrySet()) {
            long key = entry.getKey();
            int cnt = edgeFaceCountByKey.getOrDefault(key, 0);
            IntList idxs = entry.getValue();
            boolean isBoundary = (cnt == 1);
            for (int j = 0; j < idxs.size(); j++) {
                int idx = idxs.get(j);
                boundary[idx] = isBoundary;
                perInputCount[idx] = cnt;
            }
        }

        // 8) wynik
        int[][] triArr = new int[triangles.size()][3];
        for (int i = 0; i < triangles.size(); i++) triArr[i] = triangles.get(i);

        return new Result(triArr, faces, boundary, perInputCount);
    }

    private static void collectFaceIfAny(int from, int to,
                                         HashMap<Long, Long> next,
                                         HashSet<Long> usedHalfEdges,
                                         ArrayList<int[]> faces) {
        long start = directedKey(from, to);
        if (usedHalfEdges.contains(start)) return;
        Long nxt = next.get(start);
        if (nxt == null) return; // brak rotation info / nieplanarne / dziura w danych

        // idziemy po "next" aż wrócimy do startu
        ArrayList<Integer> cycle = new ArrayList<>();
        long cur = start;
        int guard = 0;

        while (true) {
            if (guard++ > 2_000_000) return; // bezpiecznik na dane uszkodzone
            if (usedHalfEdges.contains(cur)) return; // już przypisane do ściany

            int u = (int)(cur >> 32);
            cycle.add(u);
            usedHalfEdges.add(cur);

            Long n = next.get(cur);
            if (n == null) return; // niekompletna rotacja
            cur = n;

            if (cur == start) break;
        }

        // cycle zawiera wierzchołki ściany (bez domknięcia)
        if (cycle.size() >= 3) {
            int[] face = new int[cycle.size()];
            for (int i = 0; i < face.length; i++) face[i] = cycle.get(i);
            faces.add(face);
        }
    }
}


/*
Da się, ale jest jeden twardy warunek: żeby poprawnie odzyskać wielokąty-ściany z samego grafu krawędzi, musisz znać (albo wyznaczyć) cykliczną kolejność sąsiadów wokół każdego wierzchołka (tzw. rotation system / planar embedding).

Dla „czystej” siatki trójkątnej wystarczyło wykrywać 3-cykle (jak wcześniej).

Dla siatki mieszanej (tria/quad/ngon) same krawędzie nie mówią, które cykle są ścianami, a które przypadkowymi cyklami w grafie.

Najrozsądniejsze uogólnienie to:

Planarne osadzenie grafu → dostajesz kolejność CCW sąsiadów dla każdego wierzchołka.

Obchód ścian (face-walk) po półkrawędziach → dostajesz listę ścian jako cykle wierzchołków (trójkąty, czworokąty, …).

Triangulacja ścian: jeśli ściana ma 
k≥3
k≥3, rozbijasz ją na trójkąty (np. „wachlarz” od jednego wierzchołka).

Krawędzie brzegowe: to te półkrawędzie, które należą tylko do jednej ściany.

Poniżej daję gotowy kod kroków 2–4 zakładając, że masz rotation system (czyli dla każdego wierzchołka v tablicę sąsiadów w kolejności CCW). Jeśli nie masz – na końcu piszę, co z tym zrobić.
*/

/*
Co dostajesz

faces: lista ścian jako cykle wierzchołków (trójkąty, quady, ngony)

triangles: wszystko zamienione na trójkąty (quady/ngony triangulowane)

boundaryEdge[i]: czy edges.get(i) jest brzegowa (należy do 1 ściany)

edgeFaceCount[i]: 0/1/2/… (świetne do diagnostyki)

A co z rotation system, jeśli go nie mam?

Masz 3 opcje:

Masz współrzędne (choćby przybliżone) albo da się je zrekonstruować (np. z FEM) → sortujesz sąsiadów wierzchołka po kącie atan2 i gotowe.

Masz tylko graf → musisz zrobić planarne osadzenie grafu (algorytm Boyer–Myrvold) i z niego dostaniesz rotation system.
W praktyce najłatwiej użyć biblioteki (np. JGraphT ma planarity/embedding narzędzia w nowszych wersjach), albo zaimplementować własny embedding.

Heurystyka tylko dla “prawie-siatek” (typowa FEM): często da się odtworzyć kolejność sąsiadów przez lokalne „spacerowanie” po trójkątach, ale to już zależy od danych i łatwo się wykoleja na brzegach/dziurach.

Jeśli powiesz mi jedno zdanie:

czy graf jest gwarantowanie planarny i manifold 2D?

czy masz jakiekolwiek współrzędne (nawet zgrubne)?
to dopiszę Ci wersję, która sama buduje rotation (wariant z geometrią albo wariant z embeddingiem planarnym).
*/