package fem.mesh;

import java.util.TreeSet;

/**
 *
 * @author jstar
 */
public interface IMesh {

    public int getNoVertices();

    public Vertex getVertex(int v);

    public int getNoElems();

    public int getNoFaces();

    public int getNoEdges();

    public Elem getElem(int e);

    public Elem getFace(int f);

    public Elem getEdge(int e);
    
    default int getDim() {
        return getVertex(0).getDim();
    }
    
    default public int getNoSubdomains() {
        TreeSet<Integer> subdomains = new TreeSet<>();
        for( int e= 0; e < getNoElems(); e++ )
            subdomains.add( getElem(e).getSubdomain());
        return subdomains.size();
    }
}
