package graphs;

import java.util.HashSet;
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
    
    default public Set<Integer> getNeighbours(int nodeNumber) {
        Set<Integer> neighbours = new HashSet<>();
        for( Edge e : getConnectionsList(nodeNumber)) {
            if( e.getNodeA() != nodeNumber )
                neighbours.add(e.getNodeA());
            else
                neighbours.add(e.getNodeB());
        }
        return neighbours;
    }
    
    @Override
    default public Iterator<Edge> iterator() {
        return getAllEdges().iterator();
    }

}
