package swinggui;

import graphs.Graph;
import java.awt.Point;

/**
 *
 * @author jstar
 */
public interface GraphView {
    public Graph getGraph();
    public void recalculateNodeCoordinates(int width, int height, int nodeSize, int leftSep, int topSep);
    public int getNodeNum(int x, int y);
    public Point getPosition( int nodeNum );
    public int getNodeSize();
}
