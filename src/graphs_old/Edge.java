package graphs_old;

import java.util.Objects;

/**
 * Undirected edge in a weighted graph (see equals method)
 *
 * @author jstar
 */
public class Edge implements EdgeInterface {

    private int nodeA;
    private int nodeB;
    private double weight;
    private String name;

    public Edge(int nA, int nB, double w) {
        nodeA = nA;
        nodeB = nB;
        weight = w;
    }

    /**
     * @return the nodeA
     */
    @Override
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
    @Override
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
     * @return the node with smaller #
     */
    @Override
    public int getMinNodeNo() {
        return nodeA < nodeB ? nodeA : nodeB;
    }

    /**
     * @return the node with bigger #
     */
    @Override
    public int getMaxNodeNo() {
        return nodeA > nodeB ? nodeA : nodeB;
    }

    /**
     * @return the weight
     */
    @Override
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return (name == null ? "" : name + " ") + nodeA + "-(" + weight + ")-" + nodeB;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Edge
                && (((Edge) o).nodeA == nodeA && ((Edge) o).nodeB == nodeB || ((Edge) o).nodeA == nodeB && ((Edge) o).nodeB == nodeA)
                && ((Edge) o).weight == weight;
    }

    @Override
    public int hashCode() {
        return 7 * nodeA + 17 * nodeB + 251 * Objects.hash(weight) + 31 * Objects.hash(name);
    }

    @Override
    public int compareTo(EdgeInterface o) {
        return o.getWeight() > weight ? -1 : (o.getWeight() == weight ? 0 : 1);
    }

    @Override
    public String getName() {
        return name;
    }

}
