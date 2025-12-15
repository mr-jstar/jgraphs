package graphs;

/**
 *  Undirected edge in a weighted graph (see equals method)
 *
 * @author jstar
 */
public interface EdgeInterface extends Comparable<EdgeInterface> {   

    public int getVertexA();

    public int getVertexB();

    public double getWeight();
    
    public String getName();

}
