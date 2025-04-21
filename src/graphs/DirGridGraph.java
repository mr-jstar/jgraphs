package graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author jstar
 */
public class DirGridGraph extends GridGraph {

    private int numColumns;
    private int numRows;
    private static final Random rand = new Random();

    public DirGridGraph(int nC, int nR, double wMin, double wMax, double avgEdgesPerNode) {
        numColumns = nC;
        numRows = nR;
        nextNodeNo = numColumns * numRows;
        double dW = wMax - wMin;
        connectLists = new HashMap<>();
        for (int c = 0; c < numColumns; c++) {
            for (int r = 0; r < numRows; r++) {
                int nn = c * numRows + r;
                connectLists.put(nn, new HashSet<>());
                HashSet<Edge> list = connectLists.get(nn);
                if (nn >= numRows && rand.nextDouble() < avgEdgesPerNode / 4) {
                    list.add(new Edge(nn, nn - getNumRows(), wMin + dW * rand.nextDouble()));
                }
                if (nn < nextNodeNo - numRows && rand.nextDouble() < avgEdgesPerNode / 4) {
                    list.add(new Edge(nn, nn + getNumRows(), wMin + dW * rand.nextDouble()));
                }
                if (r > 0 && rand.nextDouble() < avgEdgesPerNode / 4) {
                    list.add(new Edge(nn, nn - 1, wMin + dW * rand.nextDouble()));
                }
                if (r < numRows - 1 && rand.nextDouble() < avgEdgesPerNode / 4) {
                    list.add(new Edge(nn, nn + 1, wMin + dW * rand.nextDouble()));
                }
            }
        }
        updateEdgesWeights();
    }
}
