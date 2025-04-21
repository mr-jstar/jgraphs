package swinggui;

import fem.Util;
import fem.mesh.IMesh;
import graphs.Graph;
import java.awt.Point;

/**
 *
 * @author jstar
 */
public class MeshGraphView implements GraphView {

    private final IMesh mesh;
    private final Graph graph;
    private final int[][] rc;
    private int nodeSize;

    public MeshGraphView(IMesh mesh) {
        this.mesh = mesh;
        this.graph = Util.graphOfIMesh(mesh);
        rc = new int[graph.getNumNodes()][2];
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public void recalculateNodeCoordinates(int width, int height, int nodeSize, int leftSep, int topSep) {
        this.nodeSize = (int) (height / mesh.getNoVertices());
        this.nodeSize = this.nodeSize > 16 ? 16 : this.nodeSize;
        this.nodeSize = this.nodeSize < 8 ? 8 : this.nodeSize;
        leftSep = leftSep < 2 * nodeSize ? 2 * nodeSize : leftSep;
        topSep = leftSep;
        double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
        for (int v = 0; v < mesh.getNoVertices(); v++) {
            double[] xy = mesh.getVertex(v).getX();
            if (xy[0] < xmin) {
                xmin = xy[0];
            }
            if (xy[0] > xmax) {
                xmax = xy[0];
            }
            if (xy[1] < ymin) {
                ymin = xy[1];
            }
            if (xy[1] > ymax) {
                ymax = xy[1];
            }
        }
        double dx = (xmax - xmin) / (width - 2 * leftSep);
        double dy = (ymax - ymin) / (height - 2 * topSep);
        //System.err.println( dx + "," + dy );
        double d = dx > dy ? dx : dy;
        d *= 1.05;
        double xspan = width * d;
        double yspan = height * d;
        double xc = 0.5 * (xmin + xmax);
        double yc = 0.5 * (ymin + ymax);
        xmin = xc - xspan / 2;
        xmax = xc + xspan / 2;
        ymin = yc - yspan / 2;
        ymax = yc + yspan / 2;
        //System.err.println("<"+xmin+","+xmax+"> x <"+ymin+","+ymax+">");
        for (int v = 0; v < mesh.getNoVertices(); v++) {
            double[] xy = mesh.getVertex(v).getX();
            rc[v][0] = leftSep + (int) ((width - 2 * leftSep) * (xy[0] - xmin) / xspan);
            rc[v][1] = topSep + (int) ((height - 2 * topSep) * (ymax - xy[1]) / yspan);
        }
        System.err.println(this.nodeSize);
    }

    @Override
    public int getNodeNum(int x, int y) {
        int i = -1;
        double d2 = Double.MAX_VALUE;
        for (int v = 0; v < mesh.getNoVertices(); v++) {
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
