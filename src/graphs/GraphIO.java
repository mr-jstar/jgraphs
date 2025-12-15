package graphs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author jstar
 */
public class GraphIO {

    public static void saveBasicGraph(BasicGraph g, PrintWriter pw) throws IOException {
        // file format: List of adjacency - first line contains # of nodes, next lines list of the edges connected
        // <n_nodes>
        // <node_to> <weight> ...
        // ...
        pw.println(g.getNumVertices());
        for (Integer v : g.getVerticesNumbers() ) {
            HashSet<Edge> edges = g.getConnectionsList(v);
            pw.print("\t");
            if (edges != null) {
                for (Edge e : edges) {
                    pw.print(" " + e.getVertexB() + " :" + e.getWeight() + " ");
                }
            }
            pw.println();
        }
        pw.close();
    }

    public static BasicGraph readBasicGraph(Reader r) throws IOException {
        // file format: List of adjacency - first line contains # of nodes, next lines list of the edges connected
        // <n_nodes>
        // <node_to> <weight> ...
        // ...
        try {
            BufferedReader br = new BufferedReader(r);
            String[] words = br.readLine().split("\\s*");
            int noOfNodes = Integer.parseInt(words[0]);
            HashMap<Integer, HashSet<Edge>> connectLists = new HashMap<>();
            for (int i = 0; i < noOfNodes; i++) {
                HashSet<Edge> edges = new HashSet<>();
                words = br.readLine().split("[\\s:]*");
                for (int j = 0; j < words.length; j += 2) {
                    edges.add(new Edge(i, Integer.parseInt(words[j]), Double.parseDouble(words[j + 1])));
                }
                connectLists.put(i, edges);
            }
            return new BasicGraph(noOfNodes, connectLists);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IOException("GridGraph can not read graph: " + e.getMessage());
        }
    }

    public static ModifiableGraph readAdjacencyList(Reader r) throws IOException {
        // file format: List of adjacency - first line contains # of nodes, next lines list of the edges connected
        // <n_nodes>
        // <node_to> <weight> ...
        // ...
        try {
            ModifiableGraph g = new ModifiableGraph();
            BufferedReader br = new BufferedReader(r);
            String[] words = br.readLine().split("\\s*");
            int nNodes = Integer.parseInt(words[0]);
            for (int i = 0; i < nNodes; i++) {
                g.addVertex(i);
                words = br.readLine().split("[\\s:]*");
                for (int j = 0; j < words.length; j += 2) {
                    g.addEdge(i, Integer.parseInt(words[j]), Double.parseDouble(words[j + 1]));
                }
            }
            br.close();
            r.close();
            return g;
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IOException("Can not read graph: " + e.getMessage());
        }
    }

    public static ModifiableGraph readEdgeList(Reader r) throws IOException {
        // file format: List of edges - each line contain an edge
        // <name> <node_from> <node_to> <weight>
        // ...
        try {
            ModifiableGraph g = new ModifiableGraph();
            BufferedReader br = new BufferedReader(r);
            String[] words;
            String line;
            while ((line = br.readLine()) != null) {
                words = line.split("[\\s]+");
                Edge e = new Edge(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Double.parseDouble(words[3]));
                e.setName(words[0]);
                g.addEdge(e);
            }
            br.close();
            r.close();
            return g;
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IOException("Can not read graph: " + e.getMessage());
        }
    }

    public static GridGraph readGridGraph(Reader r) throws IOException {
        // file format: List of adjacency - first line contains # of nodes, next lines list of the edges connected
        // <n_rows> <n_cols>
        // <node_to> <weight> ...
        // ...
        try {
            BufferedReader br = new BufferedReader(r);
            String[] words = br.readLine().trim().split("\\s+");
            int numColumns = Integer.parseInt(words[0]);
            int numRows = Integer.parseInt(words[1]);
            HashMap<Integer, HashSet<Edge>> connectLists = new HashMap<>();
            //System.out.println(numColumns+" "+numRows);
            for (int i = 0; i < numColumns * numRows; i++) {
                HashSet<Edge> edges = new HashSet<>();
                words = br.readLine().trim().split("[\\s:]+");
                //System.out.println(i+":"+words.length);
                for (int j = 0; j + 1 < words.length; j += 2) {
                    Edge e = new Edge(i, Integer.parseInt(words[j]), Double.parseDouble(words[j + 1]));
                    edges.add(e);
                }
                connectLists.put(i, edges);
            }
            return new GridGraph(numColumns, numRows, connectLists);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new IOException("GridGraph can not read graph: " + e);
        }
    }

    public static void saveGridGraph(GridGraph g, PrintWriter pw) throws IOException {
        // file format: List of adjacency - first line contains # of nodes, next lines list of the edges connected
        // <n_rows> <n_cols>
        // <node_to> <weight> ...
        // ...
        pw.println(g.getNumColumns() + " " + g.getNumRows());
        for (int i = 0; i < g.getNumColumns() * g.getNumRows(); i++) {
            HashSet<Edge> edges = g.getConnectionsList(i);
            pw.print("\t");
            if (edges != null) {
                for (Edge e : edges) {
                    pw.print(" " + e.getVertexB() + " :" + e.getWeight() + " ");
                }
            }
            pw.println();
        }
        pw.close();
    }

}
