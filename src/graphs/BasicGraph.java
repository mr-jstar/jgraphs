package graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jstar
 */
public class BasicGraph implements Graph {

    protected int nextVertexNo = 0;
    protected HashMap<Integer, HashSet<Edge>> connectLists = new HashMap<>();
    protected HashMap<Integer, String> vertexLabels = new HashMap<>();

    private double minEdgeWeight, maxEdgeWeight;

    public BasicGraph() {}

    public BasicGraph(int nextVertexNo,HashMap<Integer, HashSet<Edge>> connectLists  ) {
        this.nextVertexNo = nextVertexNo;
        this.connectLists = connectLists;
    }

    /**
     * @return the number of Nodes
     */
    @Override
    public int getNumVertices() {
        return connectLists == null ? 0 : connectLists.size();
    }
    
    /**
     * @return true if this graph contains vertex numbered number
     */
    @Override
    public boolean hasVertex(int number) {
        return connectLists.containsKey(number);
    }

    /**
     * @return numbers of Nodes
     */
    @Override
    public Set<Integer> getVerticesNumbers() {
        return connectLists.keySet();
    }

    /**
     * @return all egdes
     */
    @Override
    public Set<Edge> getAllEdges() {
        Set<Edge> all = new HashSet<>();
        for (Integer n : connectLists.keySet()) {
            all.addAll(connectLists.get(n));
        }
        return all;
    }

    /**
     * @return the label of given node
     */
    @Override
    public String getVertexLabel(int n) {
        if (connectLists != null && connectLists.containsKey(n)) {
            return vertexLabels.get(n);
        } else {
            return null;
        }
    }

    /**
     * @return the minimum of Edges' weights
     */
    @Override
    public double getMinEdgeWeight() {
        if (minEdgeWeight == 0.0 && maxEdgeWeight == 0.0) {
            updateEdgesWeights();
        }
        return minEdgeWeight;
    }

    /**
     * @return the maximum of Edges' weights
     */
    @Override
    public double getMaxEdgeWeight() {
        if (minEdgeWeight == 0.0 && maxEdgeWeight == 0.0) {
            updateEdgesWeights();
        }
        return maxEdgeWeight;
    }

    protected void updateEdgesWeights() {
        minEdgeWeight = Double.POSITIVE_INFINITY;
        maxEdgeWeight = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < nextVertexNo; i++) {
            for (Edge e : connectLists.get(i)) {
                double w = e.getWeight();
                if (w < minEdgeWeight) {
                    minEdgeWeight = w;
                }
                if (w > maxEdgeWeight) {
                    maxEdgeWeight = w;
                }

            }
        }
        if (minEdgeWeight == Double.POSITIVE_INFINITY && maxEdgeWeight == Double.NEGATIVE_INFINITY) {
            minEdgeWeight = maxEdgeWeight = 0.0;
        }
    }

    /**
     * @param n - node number
     * @return the connectLists
     */
    @Override
    public HashSet<Edge> getConnectionsList(int n) {
        HashSet<Edge> s = connectLists == null ? null : connectLists.get(n);
        HashSet<Edge> copy = new HashSet<>();
        if( s != null )
            for( Edge e : s )
                copy.add(e);
        return copy;
    }

    @Override
    public String toString() {
        String s = new String(connectLists.size() + "");
        for (Integer i : connectLists.keySet()) {
            s += "\n\t" + i + ":";
            for (Edge e : connectLists.get(i)) {
                s += " " + e;
            }
        }
        return s;
    }
}
