package graphs;

import java.util.Objects;

/**
 *  Undirected edge in a weighted graph (see equals method)
 *
 * @author jstar
 */
public class Edge implements Comparable<Edge> {   

    private int nodeA;
    private int nodeB;
    private double weight;

    public Edge(int nA, int nB, double w) {
        nodeA = nA;
        nodeB = nB;
        weight = w;
    }

    /**
     * @return the nodeA
     */
    public int getNodeA() {
        return nodeA;
    }

    /**
     * @param nodeA the nodeA to set
     */
    public void setNodeA(int nodeA) {
        this.nodeA = nodeA;
    }

    /**
     * @return the nodeB
     */
    public int getNodeB() {
        return nodeB;
    }

    /**
     * @param nodeB the nodeB to set
     */
    public void setNodeB(int nodeB) {
        this.nodeB = nodeB;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return nodeA + "-(" + weight + ")-" + nodeB;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Edge
                && (((Edge) o).nodeA == nodeA && ((Edge) o).nodeB == nodeB || ((Edge) o).nodeA == nodeB && ((Edge) o).nodeB == nodeA )
                && ((Edge) o).weight == weight;
    }

    @Override
    public int hashCode() {
        return 7 * nodeA + 17 * nodeB + 251 * Objects.hash(weight);
    }

    @Override
    public int compareTo(Edge o) {
        return o.getWeight() > weight ? -1 : (o.getWeight() == weight ? 0 : 1);
    }

}
