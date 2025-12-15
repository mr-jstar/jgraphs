package fem;

import fem.mesh.Elem;
import fem.mesh.IMesh;
import fem.mesh.Mesh;
import fem.mesh.Segment;
import fem.mesh.Triangle;
import fem.mesh.Vertex;
import graphs_old.Edge;
import graphs_old.Graph;
import graphs_old.ModifiableGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jstar
 */
public class Util {

    public static String nextLine(BufferedReader r) throws Exception {
        String line;
        do {
            if ((line = r.readLine()) == null) {
                return null;
            }
            line = line.trim();
        } while (line.length() == 0 || line.startsWith("#"));
        return line;
    }

    public static IMesh loadTriangleMesh(String path) throws Exception {
        if( path.endsWith(".poly")) path = path.replaceFirst("\\.poly$", "\\.node");
        String nodeFile = path.replaceFirst("\\.ele$", ".node");
        String eleFile = path.replaceFirst("\\.node$", ".ele");
        String edgeFile = eleFile.replaceFirst("\\.ele", ".edge");
        //System.out.println(nodeFile);
        //System.out.println(eleFile);
        String line = null;
        List<Vertex> vl;
        List<Elem> trl;
        int firstNodeNo = 0;
        try (BufferedReader r = new BufferedReader(new FileReader(nodeFile))) {
            line = nextLine(r);
            String[] w = line.split("\\s+");
            int nVerts = Integer.parseInt(w[0]);
            int dim = Integer.parseInt(w[1]);
            int nAttributes = Integer.parseInt(w[2]);
            int nMarkers = Integer.parseInt(w[3]);
            vl = new ArrayList<>(nVerts);
            line = nextLine(r);
            w = line.split("\\s+");
            firstNodeNo = Integer.parseInt(w[0]);
            for (int v = 0; v < nVerts; v++) {
                w = line.split("\\s+");
                double[] x = new double[2];
                x[0] = Double.parseDouble(w[1]);
                x[1] = Double.parseDouble(w[2]);
                double[] attrib = null;
                if (nAttributes > 0) {
                    attrib = new double[nAttributes];
                    for (int i = 0; i < nAttributes; i++) {
                        attrib[i] = Double.parseDouble(w[i + 3]);
                    }
                }
                int mark;
                if (nMarkers > 0) {
                    mark = Integer.parseInt(w[3 + nAttributes]);
                    vl.add(new Vertex(x, mark, attrib));
                } else {
                    vl.add(new Vertex(x, attrib));
                }
                line = nextLine(r);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("File " + nodeFile + " does not contain nodes of a triangle mesh (" + line + " => " + e.getClass() + "=>" + e.getMessage() + ")");
        }
        List<Elem> edgl = new ArrayList<>();
        if (Files.isReadable((new File(edgeFile)).toPath())) {
            try (BufferedReader r = new BufferedReader(new FileReader(edgeFile))) {
                line = nextLine(r);
                String[] w = line.split("\\s+");
                int nEdges = Integer.parseInt(w[0]);
                int nBndMarks = Integer.parseInt(w[1]);
                for (int e = 0; e < nEdges; e++) {
                    int[] nds = new int[2];
                    line = nextLine(r);
                    w = line.split("\\s+");
                    nds[0] = Integer.parseInt(w[1]) - firstNodeNo;
                    nds[1] = Integer.parseInt(w[2]) - firstNodeNo;
                    if (nBndMarks > 0) {
                        int bMark = Integer.parseInt(w[2]);
                        edgl.add(new Segment(nds, bMark));
                    } else {
                        edgl.add(new Segment(nds));
                    }
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("File " + edgeFile + " does not contain edges of a triangle mesh (" + line + " => " + e.getMessage() + ")");
            }
        }
        try (BufferedReader r = new BufferedReader(new FileReader(eleFile))) {
            line = nextLine(r);
            String[] w = line.split("\\s+");
            int nTrngls = Integer.parseInt(w[0]);
            int nNodes = Integer.parseInt(w[1]);
            int nAttributes = Integer.parseInt(w[2]);
            trl = new ArrayList<>(nTrngls);
            for (int e = 0; e < nTrngls; e++) {
                int[] nds = new int[nNodes];
                line = nextLine(r);
                w = line.split("\\s+");
                for (int i = 0; i < nNodes; i++) {
                    nds[i] = Integer.parseInt(w[i + 1]) - firstNodeNo;
                }
                int[] attrib = null;
                if (nAttributes > 0) {
                    attrib = new int[nAttributes];
                    for (int i = 0; i < nAttributes; i++) {
                        attrib[i] = (int) Double.parseDouble(w[i + 1 + nNodes]);
                    }
                    trl.add(new Triangle(nds, attrib));
                } else {
                    trl.add(new Triangle(nds));
                }
            }
            return new Mesh(vl, trl, new ArrayList<>(), edgl);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("File " + eleFile + " does not contain triangles of a triangle mesh (" + line + " => " + e.getMessage() + ")");
        }
    }
    
    public static Graph graphOfIMesh( IMesh mesh ) {
        ModifiableGraph g = new ModifiableGraph();
        for( int v = 0; v < mesh.getNoVertices(); v++ )
            g.addNode(v);
        Set<Edge> edges = new HashSet<>();
        for( int e = 0; e < mesh.getNoElems(); e++ ) {
            int [] vs = mesh.getElem(e).getVertices();
            for(int v = 1; v < vs.length; v++ )
                edges.add(new Edge(vs[v-1],vs[v], 1.0));
            edges.add( new Edge(vs[vs.length-1],vs[0],1.0));
        }
        for( Edge e : edges )
            g.addEdge(e);
        return g;
    }

}
