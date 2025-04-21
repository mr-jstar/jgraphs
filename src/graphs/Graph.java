package graphs;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author jstar
 */
public interface Graph extends Iterable<Edge> {

    public int getNumNodes();

    public Set<Integer> getNodeNumbers();

    public Set<Edge> getAllEdges();

    public double getMinEdgeWeight();

    public double getMaxEdgeWeight();

    public String getNodeLabel(int n);

    public Set<Edge> getConnectionsList(int nodeNumber);
    
    @Override
    default public Iterator<Edge> iterator() {
        return getAllEdges().iterator();
    }

}
