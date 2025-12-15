package graphs;

/**
 *  Undirected edge in a weighted graph (see equals method)
 *
 * @author jstar
 */
public interface EdgeInterface extends Comparable<EdgeInterface> {   

    public int getNodeA();

    public int getNodeB();
    
    public int getMinNodeNo();
    
    public int getMaxNodeNo();

    public double getWeight();
    
    public String getName();

}
