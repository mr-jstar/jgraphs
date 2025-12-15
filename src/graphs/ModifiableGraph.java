package graphs;

import java.util.HashSet;

/**
 *
 * @author jstar
 */
public class ModifiableGraph extends BasicGraph implements GraphBuilder  {

    @Override
    public boolean hasVertex(int number) {
        return connectLists.containsKey(number);
    }

    @Override
    public boolean hasEdge(int nodeA, int nodeB) {
        if (!connectLists.containsKey(nodeA)) {
            return false;
        }
        if (!connectLists.containsKey(nodeB)) {
            return false;
        }
        for (Edge e : connectLists.get(nodeA)) {
            int nA = e.getVertexA();
            int nB = e.getVertexB();
            if (nA == nodeA && nB == nodeB || nA == nodeB && nB == nodeA) {
                return true;
            }
        }
        for (Edge e : connectLists.get(nodeB)) {
            int nA = e.getVertexA();
            int nB = e.getVertexB();
            if (nA == nodeA && nB == nodeB || nA == nodeB && nB == nodeA) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addVertex() {
        connectLists.put(++nextVertexNo, new HashSet<>());
        vertexLabels.put(nextVertexNo, "" + nextVertexNo);
    }

    @Override
    public void addVertex(int number) {
        if (!connectLists.containsKey(number)) {
            connectLists.put(number, new HashSet<>());
            vertexLabels.put(number, "" + number);
            if (number >= nextVertexNo) {
                nextVertexNo = number + 1;
            }
        }
    }

    @Override
    public void addEdge(int first, int second) {
        addEdge(first, second, 1.0);
    }

    @Override
    public void addEdge(int first, int second, double weight) {
        addVertex(first);
        addVertex(second);
        connectLists.get(first).add(new Edge(first, second, weight));
        connectLists.get(second).add(new Edge(second, first, weight));
    }

    @Override
    public void addEdge(Edge e) {
        addVertex(e.getVertexA());
        addVertex(e.getVertexB());
        connectLists.get(e.getVertexA()).add(e);
        connectLists.get(e.getVertexB()).add(e);
    }

    @Override
    public void addGraph( Graph  g ) {
        for( Integer i : g.getVerticesNumbers()) {
            addVertex(i);
        }
        for( Integer i : g.getVerticesNumbers()) {
            for( Edge e : g.getConnectionsList(i))
                addEdge(e);
        }
    }

    @Override
    public void setVertexLabel(int n, String label) {
        if (vertexLabels.containsKey(n)) {
            vertexLabels.remove(n);
        }
        vertexLabels.put(n, label);
    }

    @Override
    public Graph getGraph() {
        return this;
    }
}
