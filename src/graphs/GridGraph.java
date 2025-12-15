package graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author jstar
 */
public class GridGraph extends BasicGraph {

    private int numColumns;
    private int numRows;
    private static final Random rand = new Random();

    private double minEdgeWeight, maxEdgeWeight;

    public GridGraph() {
        super();
        numColumns = numRows = 0;
    }

    public GridGraph(int nC, int nR, double wMin, double wMax, double avgEdgesPerNode) {
        super();
        numColumns = nC;
        numRows = nR;
        nextVertexNo = numColumns * numRows;
        int nMax = numColumns * numRows;
        double dW = wMax - wMin;
        for (int c = 0; c < numColumns; c++) {
            for (int r = 0; r < numRows; r++) {
                int nn = c * numRows + r;
                connectLists.put(nn, new HashSet<>());
            }
        }
        for (int c = 0; c < numColumns; c++) {
            for (int r = 1; r < numRows; r++) {
                int n2 = c * numRows + r;
                int n1 = n2 - 1;
                HashSet<Edge> l1 = connectLists.get(n1);
                HashSet<Edge> l2 = connectLists.get(n2);
                double w12 = wMin + dW * rand.nextDouble();
                if (rand.nextDouble() < avgEdgesPerNode / 4) {
                    l1.add(new Edge(n1, n2, w12));
                    l2.add(new Edge(n2, n1, w12));
                }
            }
        }
        for (int c = 1; c < numColumns; c++) {
            for (int r = 0; r < numRows; r++) {
                int n2 = c * numRows + r;
                int n1 = n2 - numRows;
                HashSet<Edge> l1 = connectLists.get(n1);
                HashSet<Edge> l2 = connectLists.get(n2);
                double w12 = wMin + dW * rand.nextDouble();
                if (rand.nextDouble() < avgEdgesPerNode / 4) {
                    l1.add(new Edge(n1, n2, w12));
                    l2.add(new Edge(n2, n1, w12));
                }
            }
        }
        updateEdgesWeights();
    }

    public GridGraph(int nC, int nR, Graph toCopy ) {
        super();
        numColumns = nC;
        numRows = nR;
        nextVertexNo = numColumns * numRows;
        int nMax = numColumns * numRows;
        for (int c = 0; c < numColumns; c++) {
            for (int r = 0; r < numRows; r++) {
                int nn = c * numRows + r;
                connectLists.put(nn, new HashSet<>(toCopy.getConnectionsList(nn)));
            }
        }
    }

    public GridGraph(int nC, int nR, HashMap<Integer, HashSet<Edge>> connectLists ) {
        super();
        numColumns = nC;
        numRows = nR;
        nextVertexNo = numColumns * numRows;
        int nMax = numColumns * numRows;
        this.connectLists = connectLists;
        updateEdgesWeights();
    }

    /**
     * @return the node number given row and column
     */
    public int nodeNum(int r, int c) {
        return c * numRows + r;
    }

    /**
     * @return the row number given node number
     */
    public int row(int n) {
        return n % numRows;
    }

    /**
     * @return the column number given node number
     */
    public int col(int n) {
        return (int) (n / numRows);
    }

    /**
     * @return the number of Nodes
     */
    @Override
    public int getNumVertices() {
        return numColumns * numRows;
    }

    /**
     * @return the label of given node
     */
    @Override
    public String getVertexLabel(int n) {
        if (n >= 0 && n < numColumns * numRows) {
            return "" + n;
        } else {
            return null;
        }
    }

    /**
     * @return the numColumns
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * @return the numRows
     */
    public int getNumRows() {
        return numRows;
    }

    @Override
    public String toString() {
        String s = new String(""+ numColumns + ' ' + numRows +'\n');
        for (Integer i : connectLists.keySet()) {
            s += "\n\t" + i + ":";
            for (Edge e : connectLists.get(i)) {
                s += " " + e;
            }
        }
        return s;
    }

}
