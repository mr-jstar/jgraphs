package graphs;

import java.util.Objects;

/**
 *  Undirected edge in a weighted graph (see equals method)
 *
 * @author jstar
 */
public interface EdgeInterface extends Comparable<EdgeInterface> {   

    public int getNodeA();

    public int getNodeB();

    public double getWeight();

}
