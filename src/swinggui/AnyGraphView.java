package swinggui;

import graphs.Graph;
import graphs.GraphAlgorithms;
import graphs.SingleSourceGraphPaths;
import java.awt.Point;
import java.text.Normalizer;
import java.util.Random;
import sparsematrices.EigenValues;
import sparsematrices.SparseMatrix;

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
        /*
        Graph bnd = GraphAlgorithms.boundary(graph);
        System.out.println( bnd );     
        //SingleSourceGraphPaths p = GraphAlgorithms.bfs(bnd,GraphAlgorithms.initialV(graph));
        SingleSourceGraphPaths p = GraphAlgorithms.dfs(bnd);
        System.out.println( "from " + p.src + ":" + p.dMin + " to " + p.farthest + ":" + p.dMax );
        for( int i= 0; i < p.d.length; i++ )
            System.out.println( i + ":" + p.d[i] + " p=" + p.p[i]);
        int [] path = new int[(int)(p.dMax+1)];
        int i= path.length-1;
        path[i] = p.farthest;
        while( p.p[path[i]] != -1 ) {
            int prev = p.p[path[i]];
            path[--i] = prev;
        }
        path[0] = p.src;
        for( int v : path )
            System.out.print( v + "(" + p.d[v] + ") " );
        System.out.println();
        */
        if (last_width == -1 || last_height == -1) {
            SparseMatrix L = GraphAlgorithms.weightedLaplacian(graph);
            System.out.println(L);
            double [] x = new double[graph.getNumNodes()];
            double [] y = new double[x.length];
            EigenValues.powerIteration(L, 1e-6, x);
            EigenValues.powerIterationSecondEigen(L, 1e-6, x, y);
            this.nodeSize = (int) (height / graph.getNumNodes());
            this.nodeSize = this.nodeSize > MAX_NODE_SIZE ? MAX_NODE_SIZE : this.nodeSize;
            this.nodeSize = this.nodeSize < MIN_NODE_SIZE ? MIN_NODE_SIZE : this.nodeSize;
            leftSep = leftSep < 2 * nodeSize ? 2 * nodeSize : leftSep;
            topSep = leftSep;
            normalize( x );
            normalize( y );
            for (int v = 0; v < graph.getNumNodes(); v++) {
                rc[v][0] = leftSep + (int) ((width - 2 * leftSep) * x[v]);
                rc[v][1] = topSep + (int) ((height - 2 * topSep) * y[v]);
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
    
    private static void normalize( double [] x ) {
            double min = x[0], max = x[0];
            for( double c : x ) {
                if( c < min ) min = c;
                if( c > max ) max = c;
            }
            double delta = max - min;
            for( int i = 0; i < x.length; i++ )
                x[i] = (x[i]-min)/delta;
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
