package graphs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Set;

/**
 *
 * @author jstar
 */
public interface Graph {

    public int getNumNodes();

    public Set<Integer> getNodeNumbers();

    public Set<Edge> getAllEdges();

    public double getMinEdgeWeight();

    public double getMaxEdgeWeight();

    public String getNodeLabel(int n);

    public Set<Edge> getConnectionsList(int nodeNumber);

}
