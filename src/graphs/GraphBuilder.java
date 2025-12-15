package graphs;

/**
 *
 * @author jstar
 */
public interface GraphBuilder  {

    public void addVertex(); // with subsequent number

    public boolean hasEdge(int nodeA, int nodeB);

    public void addVertex(int number);

    public void addEdge(int first, int second);  // weight == 1

    public void addEdge(int first, int second, double weight);

    public void addEdge(Edge  e);

    public void addGraph(Graph g);

    public void setVertexLabel(int n, String label);

    public Graph getGraph();
}
