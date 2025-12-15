package graphs_old;

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
    
    default public Edge getEdge( int v1, int v2 ) {
        Set<Edge> e1 = getConnectionsList(v1);
        e1.retainAll(getConnectionsList(v2));
        Iterator<Edge> it = e1.iterator();
        return it.hasNext() ? it.next() : null;
    }
    
    @Override
    default public Iterator<Edge> iterator() {
        return getAllEdges().iterator();
    }

}
