package swinggui;

import graphs_old.Graph;
import graphs_old.GridGraph;
import java.awt.Point;

/**
 *
 * @author jstar
 */
public class GridGraphView implements GraphView {

    final static int MINNODESIZE = 10;

    private final GridGraph graph;
    private final int[][] rc;
    private int leftSep, topSep;
    private int nodeSep;
    private int nodeSize;

    public GridGraphView(GridGraph graph) {
        this.graph = graph;
        rc = new int[graph.getNumNodes()][2];
    }
    
    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public void recalculateNodeCoordinates(int width, int height, int nodeSize, int leftSep, int topSep) {
        System.out.println(width + " x " + height);
        this.topSep = topSep;
        this.leftSep = leftSep;
        int rows = graph.getNumRows();
        int cols = graph.getNumColumns();
        nodeSep = (int) ((height - topSep - nodeSize) / cols);
        if (leftSep + rows * nodeSep + nodeSize > width) {
            nodeSep = (int) ((width - leftSep - nodeSize) / rows);
        }
        if (topSep + cols * nodeSep + nodeSize > height) {
            nodeSep = (int) ((height - topSep - nodeSize) / cols);
        }
        //System.out.println("Node sep: " + nodeSep);
        if (nodeSep < 1) {
            return;
        }
        this.nodeSize = (int) (nodeSep / 5.0);
        this.nodeSize = this.nodeSize < MINNODESIZE ? MINNODESIZE : this.nodeSize;
        System.out.println("Node size: " + this.nodeSize);
        for (int n = 0; n < graph.getNumNodes(); n++) {
            rc[n][0] = n % rows;  // column
            rc[n][1] = n / rows;  // row
            //System.out.println(n + "-> r=" + rc[n][0] + " c=" + rc[n][1]);
        }
    }

    @Override
    public Point getPosition(int nodeNum) {
        return new Point(leftSep + rc[nodeNum][0] * nodeSep, topSep + rc[nodeNum][1] * nodeSep);
    }

    @Override
    public int getNodeSize() {
        return nodeSize;
    }

    @Override
    public int getNodeNum(int x, int y) {
        int c = (int) ((x - leftSep) / nodeSep);
        int r = (int) ((y - topSep) / nodeSep);
        return r * graph.getNumColumns() + c;
    }

}
