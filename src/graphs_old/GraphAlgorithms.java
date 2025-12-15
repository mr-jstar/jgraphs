package graphs_old;

import java.util.*;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import sparsematrices.HashMapSparseMatrix;
import sparsematrices.SparseMatrix;

/**
 *
 * @author jstar
 */
public class GraphAlgorithms {

    static final int WHITE = 0;
    static final int GRAY = 1;
    static final int BLACK = 2;

    public static String lastError = null;

    // Self made priority queue for Dijkstra and classical (Cormen-Leiserson-Rivest-Stein) version of Prim's algorithm
    private static class HeapPQ {

        private int[] h;    // heap
        private int n;      // actual length of heap
        private double[] d; // distances of all indexToVertex to the source
        private int[] pos;  // positions of all indexToVertex to the source
        // pos is used to find the position of node x on the heap at O(1)

        HeapPQ(double[] d) {
            this.d = d;              // copy of the table used in Dijkstra algorithm
            pos = new int[d.length];
            Arrays.fill(pos, -1);    // none of the indexToVertex is on the heap
            h = new int[d.length];
            n = 0;                   // initially the heap is empty
        }

        boolean isEmpty() {
            return n == 0;
        }

        void heapUp(int c) {
            while (c > 0) {
                int p = (c - 1) / 2;
                if (d[h[p]] > d[h[c]]) {
                    int tmp = h[p];
                    h[p] = h[c];
                    h[c] = tmp;
                    pos[h[p]] = p;
                    pos[h[c]] = c;
                    c = p;
                } else {
                    return;
                }
            }
        }

        void add(int i, double dst) {
            d[i] = dst;
            h[n++] = i;
            pos[i] = n - 1;
            heapUp(n - 1);
        }

        int poll() {
            if (n == 0) {
                throw new IllegalStateException("GraphUtils::HeapPQ: trying to pop from empty priority queue!");
            }
            int ret = h[0];
            h[0] = h[--n];
            pos[ret] = -1;
            pos[h[0]] = 0;
            int p = 0;  // heap down
            int c = 2 * p + 1;
            while (c < n) {
                if (c + 1 < n && d[h[c + 1]] < d[h[c]]) {
                    c++;
                }
                if (d[h[p]] <= d[h[c]]) {
                    break;
                }
                int tmp = h[p];
                h[p] = h[c];
                h[c] = tmp;
                pos[h[p]] = p;
                pos[h[c]] = c;
                p = c;
                c = 2 * p + 1;
            }
            return ret;
        }

        void update(int x, double dst) {
            d[x] = dst;
            if (pos[x] == -1) { //  there was no x in heap yet
                h[n++] = x;
                pos[x] = n - 1;
            }
            //System.out.println(); print();
            heapUp(pos[x]);
            //print();
        }

        public String toString() {
            String ret = "[";
            for (int i = 0; i < n; i++) {
                ret = ret + " (" + h[i] + ":" + d[h[i]] + ")";
            }
            return ret + " ]";
        }
    }

    public static boolean valid(Graph g, int startNode) {
        if (g == null || g.getNumNodes() < 1 || startNode < 0 || startNode >= g.getNumNodes()) {
            return false;
        }
        for (int currentNode = 0; currentNode < g.getNumNodes(); currentNode++) {
            for (Edge e : g.getConnectionsList(currentNode)) {
                if (e.getNodeA() != currentNode) {
                    lastError = "bfs: graph given as argument is not valid: edge starting from node " + currentNode + " has first node == " + e.getNodeA() + " instead of " + currentNode;
                    return false;
                }
            }
        }

        return true;
    }

    public static double weightSum(Graph g) {
        double sum = 0.0;
        for (Edge e : g.getAllEdges()) {
            sum += e.getWeight();
        }
        return sum;
    }

    public static Graph prim(Graph g) {
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        ModifiableGraph mst = new ModifiableGraph();
        boolean[] inMST = new boolean[g.getNumNodes()];
        Arrays.fill(inMST, false);
        mst.addNode(0);
        inMST[0] = true;
        for (Edge e : g.getConnectionsList(0)) {
            pq.add(e);
        }
        int ne=0;
        while (!pq.isEmpty() && mst.getNumNodes() < g.getNumNodes()) {
            //System.out.println( "Prim: |V.mst|="+mst.getNumNodes()+"  |PQ|="+pq.size());
            Edge se = pq.poll();
            int nA = se.getNodeA();
            int nB = se.getNodeB();
            //System.out.println( "Edge " + nA + "-" + nB + " inMST(" + nA + ")=" + inMST[nA] + " inMST(" + nB + ")=" + inMST[nB] );
            if (inMST[nA] && !inMST[nB]) {
                mst.addNode(nB);
                inMST[nB] = true;
                mst.addEdge(se);
                ne++;
                //System.out.println(ne++ + " + added " + se );
                for (Edge e : g.getConnectionsList(nB)) {
                    pq.add(e);
                }
            } else if (!inMST[nA] && inMST[nB]) {
                mst.addNode(nA);
                inMST[nA] = true;
                mst.addEdge(se);
                ne++;
                //System.out.println(ne++ + " + added " + se );
                for (Edge e : g.getConnectionsList(nA)) {
                    pq.add(e);
                }
            }
        }
        System.out.println("Total " + ne + " edges, weight " + weightSum(mst));
        return mst;
    }

    public static Graph classical_prim(Graph g) {
        double[] cheapest = new double[g.getNumNodes()];
        Arrays.fill(cheapest, Double.POSITIVE_INFINITY);
        HeapPQ pq = new HeapPQ(cheapest);
        Edge[] connection = new Edge[g.getNumNodes()];
        Arrays.fill(connection, null);
        boolean[] inMST = new boolean[g.getNumNodes()];
        Arrays.fill(inMST, false);
        ModifiableGraph mst = new ModifiableGraph();
        pq.add(0, 0.0);
        int ne= 0;
        while (!pq.isEmpty()) {
            int u = pq.poll();
            mst.addNode(u);
            inMST[u] = true;
            if (connection[u] != null) {
                mst.addEdge(connection[u]);
                ne++;
            }
            //System.out.println("Added " + u);
            for (Edge e : g.getConnectionsList(u)) {
                //System.out.println(e);
                int v = e.getNodeA();
                if (v == u) {
                    v = e.getNodeB();
                }
                if (!inMST[v] && e.getWeight() < cheapest[v]) {
                    cheapest[v] = e.getWeight();
                    connection[v] = new Edge(u, v, cheapest[v]);
                    pq.update(v, cheapest[v]);
                }
            }
            //System.out.println(pq);
        }
        System.out.println("Total " + ne + " edges, total weight " + weightSum(mst));
        return mst;
    }

    static class Forest { // set of grah-trees, very simple (not effective) implementation 

        private final ModifiableGraph[] f;
        int n;
        private final int[] node2TreeMap;
        int nTrees;

        public Forest(int size) {
            f = new ModifiableGraph[size];
            node2TreeMap = new int[size];
            n = 0;
            nTrees = 0;
        }

        public void add(ModifiableGraph g) {
            node2TreeMap[n] = n;
            f[n++] = g;
            nTrees++;
        }

        public int getTreeWithNode(int node) {
            return node2TreeMap[node];
        }

        public void joinTrees(int main, int joining) {
            for (int i = 0; i < n; i++) {
                if (node2TreeMap[i] == joining) {
                    node2TreeMap[i] = main;
                }
            }
            nTrees--;
        }

        public int size() {
            return nTrees;
        }

        public ModifiableGraph get(int i) {
            return f[i];
        }

        public String toString() {
            String ret = "[";
            for (int i = 0; i < n; i++) {
                ret += " " + node2TreeMap[i];
            }
            return ret + " ]";
        }
    }

    public static Graph kruskal(Graph g) {
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        Forest forest = new Forest(g.getNumNodes());
        for (int i = 0; i < g.getNumNodes(); i++) {
            ModifiableGraph t = new ModifiableGraph();
            t.addNode(i);
            forest.add(t);
            for (Edge e : g.getConnectionsList(i)) {
                pq.add(e);
            }
        }

        while (!pq.isEmpty() && forest.size() > 1) {
            Edge se = pq.poll();
            int nA = se.getNodeA();
            int nB = se.getNodeB();

            int iA = forest.getTreeWithNode(nA);
            int iB = forest.getTreeWithNode(nB);

            //System.out.println("Edge " + se + "  iA=" + iA + "  iB=" + iB);
            if (iA != iB) {
                // add tB to tA
                ModifiableGraph tA = forest.get(iA);
                tA.addGraph(forest.get(iB));
                tA.addEdge(se);
                forest.joinTrees(iA, iB);
                //System.out.println(forest);
            }
        }
        Graph mst = forest.get(forest.getTreeWithNode(0));
        System.out.println("Total weight " + weightSum(mst));
        return mst;
    }

    public static SingleSourceGraphPaths bfs(Graph g, int startNode) {
        if (g == null || g.getNumNodes() < 1 || startNode < 0 || startNode >= g.getNumNodes()) {
            return null;
        }
        int[] p = new int[g.getNumNodes()];
        double[] d = new double[g.getNumNodes()];
        java.util.Arrays.fill(d, -1);    // distance equal -1 marks node which is not connected to the start node
        java.util.Arrays.fill(p, -1);    //  same is valid for precedessor

        int[] c = new int[g.getNumNodes()];
        java.util.Arrays.fill(c, WHITE);

        c[startNode] = GRAY;
        d[startNode] = 0;
        p[startNode] = -1;  // made by Arrays.fill, repeated here for clarity
        java.util.Deque<Integer> fifo = new java.util.ArrayDeque<>();
        fifo.add(startNode);
        int currentNode;
        while (!fifo.isEmpty()) {
            currentNode = fifo.pop();
            for (Edge e : g.getConnectionsList(currentNode)) {
                int n = e.getNodeB() == currentNode ? e.getNodeA() : e.getNodeB();
                if (c[n] == WHITE) {
                    c[n] = GRAY;
                    p[n] = currentNode;
                    d[n] = d[currentNode] + 1;
                    fifo.add(n);
                }
            }
            c[currentNode] = BLACK;
        }

        return new SingleSourceGraphPaths(d, p);
    }

    public static SingleSourceGraphPaths dfs(Graph g) {
        if (g == null || g.getNumNodes() < 1) {
            return null;
        }
        int[] d = new int[g.getNumNodes()];
        int[] f = new int[g.getNumNodes()];
        int[] p = new int[g.getNumNodes()];
        java.util.Arrays.fill(d, -1);    // discovery "time"  -1 means "not visited"
        java.util.Arrays.fill(f, -1);    // finish "time"
        java.util.Arrays.fill(p, -1);    // parent

        int[] c = new int[g.getNumNodes()];
        java.util.Arrays.fill(c, WHITE);

        try {
            for (int n = 0; n < g.getNumNodes(); n++) {
                if (c[n] == WHITE) {
                    d[n] = 0;
                    dfs_visit(g, n, d, f, p, c, 0);
                }
            }
        } catch (StackOverflowError e) {
            throw new IllegalArgumentException("Recursive DFS: graph is to big/complicated");
        }

        return new SingleSourceGraphPaths(p, d, f);
    }

    private static void dfs_visit(Graph g, int currentNode, int[] d, int[] f, int[] p, int[] c, int time) {

        c[currentNode] = GRAY;
        d[currentNode] = time;
        for (Edge e : g.getConnectionsList(currentNode)) {
            int n = e.getNodeB();
            if (c[n] == WHITE) {
                p[n] = currentNode;
                dfs_visit(g, n, d, f, p, c, time + 1);
            }
        }
        c[currentNode] = BLACK;
        f[currentNode] = time + 1;
    }

    public static SingleSourceGraphPaths dfs_iterative(Graph g) {
        if (g == null || g.getNumNodes() < 1) {
            return null;
        }
        int[] d = new int[g.getNumNodes()];
        int[] f = new int[g.getNumNodes()];
        int[] p = new int[g.getNumNodes()];
        java.util.Arrays.fill(d, -1);    // discovery "time"  -1 means "not visited"
        java.util.Arrays.fill(f, -1);    // finish "time"
        java.util.Arrays.fill(p, -1);    // parent

        int[] c = new int[g.getNumNodes()];
        java.util.Arrays.fill(c, WHITE);

        int time = 0;
        java.util.Deque<Integer> stack = new java.util.ArrayDeque<>();
        for (int n = 0; n < g.getNumNodes(); n++) {
            if (c[n] == WHITE) {
                time = 0;
                c[n] = GRAY;
                d[n] = time++;
                stack.push(n);
                while (!stack.isEmpty()) {
                    int currentNode = stack.pop();
                    boolean isFinished = true;
                    for (Edge e : g.getConnectionsList(currentNode)) {
                        int neighbour = e.getNodeB();
                        if (c[neighbour] == WHITE) {
                            c[neighbour] = GRAY;
                            p[neighbour] = currentNode;
                            d[neighbour] = time++;
                            stack.push(neighbour);
                            isFinished = false;
                            break;
                        }
                    }
                    if (isFinished) {
                        c[currentNode] = BLACK;
                        f[currentNode] = time++;
                        if (p[currentNode] != -1) {
                            stack.push(p[currentNode]);
                        }
                    }
                }
            }
        }

        return new SingleSourceGraphPaths(p, d, f);

    }

    public static SingleSourceGraphPaths dijkstra(Graph g, int startNode) {
        if (g == null || g.getNumNodes() < 1 || startNode < 0 || startNode >= g.getNumNodes()) {
            return null;
        }
        //System.out.println("Dijkstra, source=" + startNode);
        int[] p = new int[g.getNumNodes()];
        double[] d = new double[g.getNumNodes()];
        java.util.Arrays.fill(d, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(p, -1);

        p[startNode] = -1;  // made by Arrays.fill, repeated here for clarity
        HeapPQ queue = new HeapPQ(d);
        queue.add(startNode, 0.0);
        int currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.poll();
            //System.out.println("current: " + currentNode);
            for (Edge e : g.getConnectionsList(currentNode)) {
                int n = e.getNodeB();
                //System.out.print("\t" + n + ": ");
                if (d[n] > d[currentNode] + e.getWeight()) {
                    //System.out.print(d[n] + "->" + (d[currentNode] + e.getWeight()));
                    d[n] = d[currentNode] + e.getWeight();
                    queue.update(n, d[n]);
                    p[e.getNodeB()] = currentNode;
                }
                //System.out.println();
            }
        }

        return new SingleSourceGraphPaths(d, p);
    }

    public static SingleSourceGraphPaths bellmanFord(Graph g, int startNode) {
        if (g == null || g.getNumNodes() < 1 || startNode < 0 || startNode >= g.getNumNodes()) {
            return null;
        }
        //System.out.println("Dijkstra, source=" + startNode);
        int nn = g.getNumNodes();
        int[] p = new int[nn];
        double[] d = new double[nn];
        java.util.Arrays.fill(d, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(p, -1);

        p[startNode] = -1;  // made by Arrays.fill, repeated here for clarity
        d[startNode] = 0;
        Set<Edge> allEdges = g.getAllEdges();
        for (int i = 0; i < nn; i++) {
            for (Edge e : allEdges) {
                int nA = e.getNodeA();
                int nB = e.getNodeB();
                double w = e.getWeight();
                if (d[nA] > d[nB] + w) {
                    d[nA] = d[nB] + w;
                    p[nA] = nB;
                }
            }
        }

        return new SingleSourceGraphPaths(d, p);
    }

    private static final DecimalFormat df = new DecimalFormat("0.00");

    //for testing
    private static void printArray(double[][] d) {
        int nn = d.length;
        for (int i = 0; i < nn; i++) {
            for (int j = 0; j < nn; j++) {
                System.out.print(" " + df.format(d[i][j]));
            }
            System.out.println();
        }
    }

    //for testing
    private static void printArray(int[][] d) {
        int nn = d.length;
        for (int i = 0; i < nn; i++) {
            for (int j = 0; j < nn; j++) {
                System.out.print(" " + d[i][j]);
            }
            System.out.println();
        }
    }

    public static AllToAllGraphPaths floydWarshall(Graph g) {
        if (g == null || g.getNumNodes() < 1) {
            return null;
        }

        int nn = g.getNumNodes();
        int[][] p = new int[nn][nn];
        double[][] d = new double[nn][nn];
        for (int i = 0; i < nn; i++) {
            java.util.Arrays.fill(d[i], Double.POSITIVE_INFINITY);
            java.util.Arrays.fill(p[i], -1);
        }
        for (int i = 0; i < nn; i++) {
            d[i][i] = 0;
        }
        Set<Edge> allEdges = g.getAllEdges();
        for (Edge e : allEdges) {
            int nA = e.getNodeA();
            int nB = e.getNodeB();
            double w = e.getWeight();
            d[nA][nB] = d[nB][nA] = w;
            p[nA][nB] = nA;
            p[nB][nA] = nB;
        }

        for (int m = 0; m < nn; m++) {
            for (int src = 0; src < nn; src++) {
                for (int dst = 0; dst < nn; dst++) {
                    if (d[src][dst] > d[src][m] + d[m][dst]) {
                        d[src][dst] = d[src][m] + d[m][dst];
                        p[src][dst] = p[m][dst];
                    }
                }
            }
        }

        return new AllToAllGraphPaths(d, p);
    }

    public static List<List<Edge>> partition_Kernighan_Lin(Graph graph, int startNode, int iter_limit) {
        Set<Integer> A = new HashSet<>(), B = new HashSet<>();
        List<Set<Integer>> neighbours = new ArrayList<>(graph.getNumNodes());
        for (int i = 0; i < graph.getNumNodes(); i++) {
            neighbours.add(graph.getNeighbours(i));
        }

        // Inicjalny podział węzłów wg odległości od węzła startNode
        SingleSourceGraphPaths p0 = dijkstra(graph, startNode);
        if (p0 == null) {
            System.err.println("Can't find shortest paths from node #" + startNode + " of " + graph.getNumNodes());
            Thread.currentThread().interrupt();
            return null;
        }
        List<Integer> nodeList = new ArrayList<>();
        for (int i = 0; i < graph.getNumNodes(); i++) {
            nodeList.add(i);
        }
        nodeList.sort((Integer i, Integer j) -> p0.d[i] < p0.d[j] ? -1 : (p0.d[i] == p0.d[j] ? 0 : 1));
        int half = nodeList.size() / 2;
        A.addAll(nodeList.subList(0, half));
        B.addAll(nodeList.subList(half, nodeList.size()));

        boolean improvement = true;
        int nit = 0;
        while (improvement) {
            System.err.println("Kernighan-Lin iteration #" + nit + ":");
            //System.err.println("A: " + A);
            //System.err.println("B: " + B);
            improvement = false;
            List<Swap> swaps = new ArrayList<>();

            // Oblicz D dla każdego węzła
            Map<Integer, Double> D = calculateD(graph, A, B);

            // Wybieranie par do zamiany
            Set<Integer> usedA = new HashSet<>();
            Set<Integer> usedB = new HashSet<>();

            while (usedA.size() < A.size() && usedB.size() < B.size()) {
                Swap bestSwap = findBestSwap(graph, A, B, D, usedA, usedB, neighbours);
                if (bestSwap == null) {
                    break;
                }
                //System.err.println("Best swap: " + bestSwap);
                swaps.add(bestSwap);
                usedA.add(bestSwap.nodeA);
                usedB.add(bestSwap.nodeB);

                // Aktualizacja D po każdej zamianie
                D.put(bestSwap.nodeA, D.get(bestSwap.nodeA) - bestSwap.gain);
                D.put(bestSwap.nodeB, D.get(bestSwap.nodeB) - bestSwap.gain);

                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
            }

            // Znalezienie optymalnego momentu zatrzymania
            double maxGain = 0;
            int maxIndex = -1;
            double totalGain = 0;
            for (int i = 0; i < swaps.size(); i++) {
                totalGain += swaps.get(i).gain;
                if (totalGain > maxGain) {
                    maxGain = totalGain;
                    maxIndex = i;
                }
            }

            if (maxGain > 0) {
                improvement = true;
                //System.out.println("Max Gain: " + maxGain);
                //System.out.println("Swaps: " + swaps);
                for (int i = 0; i <= maxIndex; i++) {
                    A.remove(swaps.get(i).nodeA);
                    B.remove(swaps.get(i).nodeB);
                    A.add(swaps.get(i).nodeB);
                    B.add(swaps.get(i).nodeA);
                }
            }
            if (++nit > iter_limit) {
                break;
            }
        }

        //System.out.println("Final :" + A + B);
        // Tworzenie dwóch grafów na podstawie podziału
        List<Edge> edgesA = new ArrayList<>(), edgesB = new ArrayList<>(), edgesCut = new ArrayList<>();
        for (Edge edge : graph) {
            if (A.contains(edge.getNodeA()) && A.contains(edge.getNodeB())) {
                edgesA.add(edge);
            } else if (B.contains(edge.getNodeA()) && B.contains(edge.getNodeB())) {
                edgesB.add(edge);
            } else {
                edgesCut.add(edge);
            }
        }

        List<List<Edge>> result = new ArrayList<>();
        result.add(edgesCut);
        result.add(edgesA);
        result.add(edgesB);
        return result;
    }

    private static Map<Integer, Double> calculateD(Graph graph, Set<Integer> A, Set<Integer> B) {
        Map<Integer, Double> D = new HashMap<>();
        for (Integer node : A) {
            D.put(node, 0.0);
        }
        for (Integer node : B) {
            D.put(node, 0.0);
        }

        for (Edge edge : graph) {
            if (A.contains(edge.getNodeA()) && B.contains(edge.getNodeB())
                    || A.contains(edge.getNodeB()) && B.contains(edge.getNodeA())) {
                // koszt zewnętrzny
                D.put(edge.getNodeA(), D.get(edge.getNodeA()) + edge.getWeight());
                D.put(edge.getNodeB(), D.get(edge.getNodeB()) + edge.getWeight());
            } else {
                // koszt wewnętrzny
                D.put(edge.getNodeA(), D.get(edge.getNodeA()) - edge.getWeight());
                D.put(edge.getNodeB(), D.get(edge.getNodeB()) - edge.getWeight());
            }
        }
        return D;
    }

    private static Swap findBestSwap(Graph graph, Set<Integer> A, Set<Integer> B, Map<Integer, Double> D, Set<Integer> usedA, Set<Integer> usedB, List<Set<Integer>> neighbours) {
        Swap bestSwap = null;
        double maxGain = Double.NEGATIVE_INFINITY;

        for (Integer a : A) {
            if (usedA.contains(a)) {
                continue;
            }
            for (Integer b : neighbours.get(a)) {
                if (usedB.contains(b)) {
                    continue;
                }

                double edgeWeight = 0;
                for (Edge edge : graph.getConnectionsList(b)) {
                    if ((edge.getNodeA() == a && edge.getNodeB() == b)
                            || (edge.getNodeB() == a && edge.getNodeA() == b)) {
                        edgeWeight = edge.getWeight();
                        break;
                    }
                }
                double gain = D.get(a) + D.get(b) - 2 * edgeWeight;
                if (gain > maxGain) {
                    maxGain = gain;
                    bestSwap = new Swap(a, b, gain);
                }
            }
        }
        return bestSwap;
    }

    private static Swap findBestSwapSlowly(Graph graph, Set<Integer> A, Set<Integer> B, Map<Integer, Double> D, Set<Integer> usedA, Set<Integer> usedB) {
        Swap bestSwap = null;
        double maxGain = Double.NEGATIVE_INFINITY;

        for (Integer a : A) {
            if (usedA.contains(a)) {
                continue;
            }
            for (Integer b : B) {
                if (usedB.contains(b)) {
                    continue;
                }

                double edgeWeight = 0;
                for (Edge edge : graph.getConnectionsList(b)) {
                    if ((edge.getNodeA() == a && edge.getNodeB() == b)
                            || (edge.getNodeB() == a && edge.getNodeA() == b)) {
                        edgeWeight = edge.getWeight();
                        break;
                    }
                }
                double gain = D.get(a) + D.get(b) - 2 * edgeWeight;
                if (gain > maxGain) {
                    maxGain = gain;
                    bestSwap = new Swap(a, b, gain);
                }
            }
        }
        return bestSwap;
    }

    private static class Swap {

        Integer nodeA, nodeB;
        double gain;

        Swap(Integer nodeA, Integer nodeB, double gain) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.gain = gain;
        }

        @Override
        public String toString() {
            return nodeA + "<->" + nodeB + " : " + gain;
        }
    }

    public static void edgeListToVertexSet(List<Edge> edges, Set<Integer> vertices) {
        vertices.clear();
        for (Edge e : edges) {
            vertices.add(e.getNodeA());
            vertices.add(e.getNodeB());
        }
    }

    public static List<Edge> boundary_tst(Graph g) {
        Integer[] nodes = g.getNodeNumbers().toArray(new Integer[0]);
        Arrays.sort(nodes);
        int[] nonWghtDeg = new int[nodes.length];
        double avg = 0.0;
        for (int i = 0; i < nonWghtDeg.length; i++) {
            nonWghtDeg[i] = g.getNeighbours(nodes[i]).size();
            avg += nonWghtDeg[i];
        }
        avg /= nodes.length;

        Arrays.sort(nodes, (i, j) -> Integer.compare(nonWghtDeg[i], nonWghtDeg[j]));
        int i = 0;
        System.out.print(avg + " -> BND: ");
        while (nonWghtDeg[nodes[i]] < avg) {
            //System.out.print( indexToVertex[i] + "(" + nonWghtDeg[indexToVertex[i]] + ") ");
            i++;
        }
        List<Integer> bnd = Arrays.stream(nodes).limit(i).collect(Collectors.toList());
        for (Integer n : bnd) {
            System.out.print(n + " (" + nonWghtDeg[n] + ") ");
        }
        System.out.println(" razem " + bnd.size() + ".");
        Set<Integer> n0 = g.getNeighbours(bnd.get(0));
        n0.retainAll(bnd);
        int k = n0.iterator().next();
        for (Integer ki : n0) {
            if (nonWghtDeg[ki] < nonWghtDeg[k]) {
                k = ki;
            }
        }
        Set<Edge> bE = g.getConnectionsList(bnd.get(0));
        bE.retainAll(g.getConnectionsList(k));
        List<Edge> bEL = new ArrayList<>(bE);
        System.out.println(bEL.get(0));
        int endV = bEL.get(0).getNodeA() == bnd.get(0) ? bEL.get(0).getNodeB() : bEL.get(0).getNodeA();
        int startV = bnd.get(0);
        bnd.remove(0);
        bnd.remove((Integer) endV);
        while (true) {
            System.out.println(endV);
            int oE = endV;
            for (int v = 0; v < bnd.size(); v++) {
                Edge e = g.getEdge(endV, bnd.get(v));
                if (e != null) {
                    bEL.add(e);
                    endV = e.getNodeA() == endV ? e.getNodeB() : e.getNodeA();
                    bnd.remove((Integer) endV);
                    break;
                } else {
                    System.out.println("NO " + endV + "->" + bnd.get(v));
                }
            }
            if (endV == startV || bnd.isEmpty() || endV == oE) {
                break;
            }
        }

        return bEL;
    }

    public static int initialV(Graph g) {
        Integer[] nodes = g.getNodeNumbers().toArray(new Integer[0]);
        Arrays.sort(nodes);
        int[] nonWghtDeg = new int[nodes.length];
        for (int i = 0; i < nonWghtDeg.length; i++) {
            nonWghtDeg[i] = g.getNeighbours(nodes[i]).size();
        }
        Arrays.sort(nodes, (i, j) -> Integer.compare(nonWghtDeg[i], nonWghtDeg[j]));
        System.out.println("init -> " + nodes[0]);
        return nodes[0];
    }

    public static Graph boundary(Graph g) {
        Integer[] nodes = g.getNodeNumbers().toArray(new Integer[0]);
        Arrays.sort(nodes);
        int[] nonWghtDeg = new int[nodes.length];
        double avg = 0.0;
        for (int i = 0; i < nonWghtDeg.length; i++) {
            nonWghtDeg[i] = g.getNeighbours(nodes[i]).size();
            avg += nonWghtDeg[i];
        }
        avg /= nodes.length;

        Arrays.sort(nodes, (i, j) -> Integer.compare(nonWghtDeg[i], nonWghtDeg[j]));
        ModifiableGraph b = new ModifiableGraph();
        int i = 0;
        System.out.print(avg + " -> BND: ");
        while (nonWghtDeg[nodes[i]] < avg) {
            System.out.print(nodes[i] + " ");
            Set<Edge> es = g.getConnectionsList(nodes[i]);
            for (Edge e : es) {
                b.addEdge(e);
            }
            i++;
        }
        return prim(b);
    }

    public static SparseMatrix laplacian(Graph g) {
        Integer[] indexToVertex = g.getNodeNumbers().toArray(new Integer[0]);
        Arrays.sort(indexToVertex);
        for (Integer v : indexToVertex) {
            System.err.print(v + " ");
        }
        System.err.println();

        Map<Integer, Integer> vertexToIndex = new HashMap<>();
        for (int i = 0; i < indexToVertex.length; i++) {
            vertexToIndex.put(indexToVertex[i], i);
        }

        for (Integer v : vertexToIndex.keySet()) {
            System.err.print(v + "->" + vertexToIndex.get(v) + " ");
        }
        System.err.println();

        HashMapSparseMatrix L = new HashMapSparseMatrix(indexToVertex.length);
        for (int i = 0; i < indexToVertex.length; i++) {
            Set<Integer> neighbours = g.getNeighbours(indexToVertex[i]);
            L.set(i, i, neighbours.size());
            for (Integer v : neighbours) {
                L.set(i, vertexToIndex.get(v), -1);
                L.set(vertexToIndex.get(v), i, -1);
            }
        }
        return L.toCRSsorted();
    }

    public static SparseMatrix weightedLaplacian(Graph g) {
        Integer[] indexToVertex = g.getNodeNumbers().toArray(new Integer[0]);
        Arrays.sort(indexToVertex);
        /*
        for (Integer v : indexToVertex) {
            System.err.print(v + " ");
        }
        System.err.println();
*/
        Map<Integer, Integer> vertexToIndex = new HashMap<>();
        for (int i = 0; i < indexToVertex.length; i++) {
            vertexToIndex.put(indexToVertex[i], i);
        }

        /*
        for (Integer v : vertexToIndex.keySet()) {
            System.err.print(v + "->" + vertexToIndex.get(v) + " ");
        }
        System.err.println();
*/

        HashMapSparseMatrix L = new HashMapSparseMatrix(indexToVertex.length);
        for (int i = 0; i < indexToVertex.length; i++) {
            Set<Edge> edges = g.getConnectionsList(indexToVertex[i]);
            double diag = 0.0;
            for (Edge e : edges) {
                diag += e.getWeight();
            }
            L.set( i, i, diag);
        }
        Set<Edge> es = g.getAllEdges();
        for (Edge e : es) {
            Integer iA = vertexToIndex.get(e.getNodeA());
            Integer iB = vertexToIndex.get(e.getNodeB());
            double w = - e.getWeight();
            L.set( iA, iB, w );
            L.set( iB, iA, w );
        }
        return L.toCRSsorted();
    }
}
