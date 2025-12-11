package swinggui;

import graphs.Graph;
import graphs.GraphAlgorithms;
import java.awt.Point;
import java.util.Random;

/**
 *
 * @author jstar
 */
public class AnyGraphView implements GraphView {

    private static Random rand = new Random();

    private static final int MIN_NODE_SIZE = 8;
    private static final int MAX_NODE_SIZE = 16;

    private final Graph graph;
    private final int[][] rc;
    private int last_width = -1;
    private int last_height = -1;
    private int nodeSize;

    public AnyGraphView(Graph graph) {
        this.graph = graph;
        rc = new int[graph.getNumNodes()][2];
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public void recalculateNodeCoordinates(int width, int height, int nodeSize, int leftSep, int topSep) {
        if (last_width == width && last_height == height)
            return;
        GraphAlgorithms.boundary(graph);
        if (last_width == -1 || last_height == -1) {
            this.nodeSize = (int) (height / graph.getNumNodes());
            this.nodeSize = this.nodeSize > MAX_NODE_SIZE ? MAX_NODE_SIZE : this.nodeSize;
            this.nodeSize = this.nodeSize < MIN_NODE_SIZE ? MIN_NODE_SIZE : this.nodeSize;
            leftSep = leftSep < 2 * nodeSize ? 2 * nodeSize : leftSep;
            topSep = leftSep;
            double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
            double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
            for (int v = 0; v < graph.getNumNodes(); v++) {
                rc[v][0] = leftSep + (int) ((width - 2 * leftSep) * rand.nextDouble());
                rc[v][1] = topSep + (int) ((height - 2 * topSep) * rand.nextDouble());
            }
        } else {
            double w_ratio = (double) width / last_width;
            double h_ratio = (double) height / last_height;
            for (int v = 0; v < graph.getNumNodes(); v++) {
                rc[v][0] = (int) (rc[v][0] * w_ratio);
                rc[v][1] = (int) (rc[v][1] * h_ratio);
            }
        }
        last_height = height;
        last_width = width;
    }

    @Override
    public int getNodeNum(int x, int y) {
        int i = -1;
        double d2 = Double.POSITIVE_INFINITY;
        for (int v = 0; v < graph.getNumNodes(); v++) {
            double cd2 = (x - rc[v][0]) * (x - rc[v][0]) + (y - rc[v][1]) * (y - rc[v][1]);
            if (cd2 < d2) {
                d2 = cd2;
                i = v;
            }
        }
        return i;
    }

    @Override
    public Point getPosition(int nodeNum) {
        return new Point(rc[nodeNum][0], rc[nodeNum][1]);
    }

    @Override
    public int getNodeSize() {
        return nodeSize;
    }

}
