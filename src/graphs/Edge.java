package graphs;

import java.util.Objects;

/**
 * Undirected edge in a weighted graph (see equals method)
 *
 * @author jstar
 */
public class Edge implements EdgeInterface {

    private int vertexA;
    private int vertexB;
    private double weight;
    private String name;

    public Edge(int nA, int nB, double w) {
        vertexA = nA;
        vertexB = nB;
        weight = w;
    }

    /**
     * @return the vertexA
     */
    public int getVertexA() {
        return vertexA;
    }

    /**
     * @param nodeA the vertexA to set
     */
    public void setNodeA(int nodeA) {
        this.vertexA = nodeA;
    }

    /**
     * @return the vertexB
     */
    public int getVertexB() {
        return vertexB;
    }

    /**
     * @param nodeB the vertexB to set
     */
    public void setNodeB(int nodeB) {
        this.vertexB = nodeB;
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

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return (name == null ? "" : name+" ") + vertexA + "-(" + weight + ")-" + vertexB;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Edge
                && (((Edge) o).vertexA == vertexA && ((Edge) o).vertexB == vertexB || ((Edge) o).vertexA == vertexB && ((Edge) o).vertexB == vertexA)
                && ((Edge) o).weight == weight;
    }

    @Override
    public int hashCode() {
        return 7 * vertexA + 17 * vertexB + 251 * Objects.hash(weight) + 31 * Objects.hash(name);
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
