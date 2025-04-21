package graphs;

/**
 *
 * @author jstar
 */
public interface GraphBuilder  {

    public void addNode(); // with subsequent number

    public boolean hasNode(int number);

    public boolean hasEdge(int nodeA, int nodeB);

    public void addNode(int number);

    public void addEdge(int first, int second);  // weight == 1

    public void addEdge(int first, int second, double weight);

    public void addEdge(Edge  e);

    public void addGraph(Graph g);

    public void setNodeLabel(int n, String label);

    public Graph getGraph();
}
