package graphs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jstar
 */
public interface Graph extends Iterable<Edge> {

    public int getNumVertices();
    
    default public int maxVertexNo() {
        TreeSet<Integer> sorted = new TreeSet<>(getVerticesNumbers());
        return sorted.isEmpty() ? 0 : sorted.getLast();
    }
    
    public boolean hasVertex(int number);

    public Set<Integer> getVerticesNumbers();

    public Set<Edge> getAllEdges();

    public double getMinEdgeWeight();

    public double getMaxEdgeWeight();

    public String getVertexLabel(int n);

    public Set<Edge> getConnectionsList(int nodeNumber);
    
    default public Set<Integer> getNeighbours(int vertexNumber) {
        Set<Integer> neighbours = new HashSet<>();
        for( Edge e : getConnectionsList(vertexNumber)) {
            if( e.getVertexA() != vertexNumber )
                neighbours.add(e.getVertexA());
            else
                neighbours.add(e.getVertexB());
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
