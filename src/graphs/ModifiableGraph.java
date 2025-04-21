package graphs;

import java.util.HashSet;

/**
 *
 * @author jstar
 */
public class ModifiableGraph extends BasicGraph implements GraphBuilder  {

    @Override
    public boolean hasNode(int number) {
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
            int nA = e.getNodeA();
            int nB = e.getNodeB();
            if (nA == nodeA && nB == nodeB || nA == nodeB && nB == nodeA) {
                return true;
            }
        }
        for (Edge e : connectLists.get(nodeB)) {
            int nA = e.getNodeA();
            int nB = e.getNodeB();
            if (nA == nodeA && nB == nodeB || nA == nodeB && nB == nodeA) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addNode() {
        connectLists.put(++nextNodeNo, new HashSet<>());
        nodeLabels.put(nextNodeNo, "" + nextNodeNo);
    }

    @Override
    public void addNode(int number) {
        if (!connectLists.containsKey(number)) {
            connectLists.put(number, new HashSet<>());
            nodeLabels.put(number, "" + number);
            if (number >= nextNodeNo) {
                nextNodeNo = number + 1;
            }
        }
    }

    @Override
    public void addEdge(int first, int second) {
        addEdge(first, second, 1.0);
    }

    @Override
    public void addEdge(int first, int second, double weight) {
        addNode(first);
        addNode(second);
        connectLists.get(first).add(new Edge(first, second, weight));
        connectLists.get(second).add(new Edge(second, first, weight));
    }

    @Override
    public void addEdge(Edge e) {
        addEdge(e.getNodeA(),e.getNodeB(),e.getWeight());
    }

    @Override
    public void addGraph( Graph  g ) {
        for( Integer i : g.getNodeNumbers()) {
            addNode(i);
        }
        for( Integer i : g.getNodeNumbers()) {
            for( Edge e : g.getConnectionsList(i))
                addEdge(e);
        }
    }

    @Override
    public void setNodeLabel(int n, String label) {
        if (nodeLabels.containsKey(n)) {
            nodeLabels.remove(n);
        }
        nodeLabels.put(n, label);
    }

    @Override
    public Graph getGraph() {
        return this;
    }
}
