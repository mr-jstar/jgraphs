/*
 * Do what you want with this file
 */
package swinggui;

/**
 *
 * @author jstar
 */
import graphs.AllToAllGraphPaths;
import graphs.Edge;
import graphs.Graph;
import graphs.GraphUtils;
import graphs.GridGraph;
import graphs.SingleSourceGraphPaths;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Set;

public class SwingGUI extends JFrame {

    final static String[] algorithms = {"BFS", "DFS Recursive", "DFS Iterative", "Dijkstra", "Bellman-Ford", "Floyd-Warshall", "Kruskal", "Prim", "Prim_CLRS"};

    final private String nodeScaleViewLabelTxt = "Color scale for nodes (distance): ";
    final private String edgeScaleViewLabelTxt = "Color scale for edges (weights): ";

    final static int DEFAULTWIDTH = 1600;
    final static int DEFAULTHEIGHT = DEFAULTWIDTH - 200;

    final static int MINNODESIZE = 10;
    final static int BASICNODESEP = 80;
    final static int BASICNODESIZE = 20;

    private int leftSep = 10;
    private int topSep = 10;
    private int nodeSize = BASICNODESIZE;
    private int nodeSep = BASICNODESEP;
    private double minWght = 0;
    private double maxWght = 20;

    private JTextField gridSizeTextField, edgeWeightRangeTextField, edgesPerNodeTextField;
    final private JLabel nodeColorMapLabel, edgeColorMapLabel;

    private GridGraph graph;
    private JPanel canvas;

    private final ColorMap edgeCM = new ColorMap(minWght, maxWght);

    private double edgesPerNode = 4;

    private ColorMap nodeCM;
    private JLabel nodeScaleViewLabel;
    private JLabel edgeScaleViewLabel;

    private ButtonGroup algGroup;

    private SingleSourceGraphPaths pathsSS = null;
    private AllToAllGraphPaths pathsAll = null;
    private Graph mst = null;

    ActionListener ggal = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String[] cr = gridSizeTextField.getText().split("\\s*x\\s*");
                String[] mx = edgeWeightRangeTextField.getText().split("\\s*:\\s*");
                minWght = Double.parseDouble(mx[0]);
                maxWght = Double.parseDouble(mx[1]);
                edgeScaleViewLabel.setText(edgeScaleViewLabelTxt + "" + String.format("%.3g", minWght) + "--" + String.format("%.3g", maxWght));
                edgeCM.setMin(minWght);
                edgeCM.setMax(maxWght);
                edgesPerNode = Double.parseDouble(edgesPerNodeTextField.getText());
                long start = System.nanoTime();
                graph = new GridGraph(Integer.parseInt(cr[0]), Integer.parseInt(cr[1]), minWght, maxWght, edgesPerNode);
                edgeColorMapLabel.setIcon(new ImageIcon(edgeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
                long finish = System.nanoTime();
                System.out.println((finish - start) / 1000 + " microseconds");
                pathsSS = null;
                nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + " - none - ");
                System.out.println("Draw graph " + graph.getNumColumns() + "x" + graph.getNumRows());
                nodeSep = BASICNODESEP;
                drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }

    };

    public SwingGUI() {
        // Set up the frame
        setTitle("Kratka");
        setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel gridLabel = new JLabel("Grid size:");
        gridSizeTextField = new JTextField(10);
        gridSizeTextField.setText("10 x 10");
        gridSizeTextField.addActionListener(ggal);

        JLabel edgeLabel = new JLabel("Edge weight range:");
        edgeWeightRangeTextField = new JTextField(10);
        edgeWeightRangeTextField.setText("0 : 20");

        JLabel nodeLabel = new JLabel("Edges per node:");
        edgesPerNodeTextField = new JTextField(5);
        edgesPerNodeTextField.setText("4");

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(ggal);
        JButton redrawButton = new JButton("Redraw");
        redrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Redraw graph");
                drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                if (pathsSS != null) {
                    colorNodes(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                } else {
                    nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + " - none - ");
                }
            }
        });
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Delete graph");
                graph = null;
                pathsSS = null;
                nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + " - none - ");
                edgeColorMapLabel.setIcon(null);
                nodeColorMapLabel.setIcon(null);
                drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
            }
        });
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (graph != null) {
                    JFileChooser fileChooser = new JFileChooser();
                    changeFontSize(6f, fileChooser);
                    if (fileChooser.showSaveDialog(SwingGUI.this) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        System.out.println("Save graph");
                        try {
                            GraphUtils.saveGridGraph(graph, new PrintWriter(file));
                        } catch (IOException e) {
                            System.out.println("NOT SAVED: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        });
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                changeFontSize(6f, fileChooser);
                if (fileChooser.showOpenDialog(SwingGUI.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println("Load graph");
                    try {
                        Reader r = new FileReader(file);
                        graph = GraphUtils.readGridGraph(r);
                        r.close();
                        gridSizeTextField.setText(graph.getNumColumns() + " x " + graph.getNumRows());
                        minWght = graph.getMinEdgeWeight();
                        maxWght = graph.getMaxEdgeWeight();
                        edgeWeightRangeTextField.setText(String.format("%.3g", minWght) + " : " + String.format("%.3g", maxWght));
                        edgeScaleViewLabel.setText(edgeScaleViewLabelTxt + String.format("%.3g", minWght) + "--" + String.format("%.3g", maxWght));
                        edgeCM.setMin(minWght);
                        edgeCM.setMax(maxWght);
                        edgeColorMapLabel.setIcon(new ImageIcon(edgeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
                        drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                    } catch (IOException ex) {
                        System.out.println("NOT LOADED: " + ex.getLocalizedMessage());
                    }
                }
            }
        });
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }

        });

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(Color.LIGHT_GRAY);
        controlPanel.add(gridLabel);
        controlPanel.add(gridSizeTextField);
        controlPanel.add(edgeLabel);
        controlPanel.add(edgeWeightRangeTextField);
        controlPanel.add(nodeLabel);
        controlPanel.add(edgesPerNodeTextField);
        controlPanel.add(generateButton);
        controlPanel.add(redrawButton);
        controlPanel.add(deleteButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);
        controlPanel.add(exitButton);
        changeFontSize(6f, controlPanel);
        nodeScaleViewLabel = new JLabel(nodeScaleViewLabelTxt + " - none - ");
        edgeScaleViewLabel = new JLabel(edgeScaleViewLabelTxt + " - none - ");
        JPanel scales = new JPanel();
        scales.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        scales.setBackground(Color.LIGHT_GRAY);
        scales.add(nodeScaleViewLabel);
        nodeColorMapLabel = new JLabel();
        scales.add(nodeColorMapLabel);
        scales.add(edgeScaleViewLabel);
        edgeColorMapLabel = new JLabel();
        scales.add(edgeColorMapLabel);
        changeFontSize(6f, scales);

        algGroup = new ButtonGroup();
        JPanel abPanel = new JPanel();
        abPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        for (String s : algorithms) {
            JRadioButton aB = new JRadioButton(s);
            aB.setActionCommand(s);
            algGroup.add(aB);
            aB.setSelected(true);
            abPanel.add(aB);
        }
        JPanel algPanel = new JPanel();
        algPanel.setBackground(Color.LIGHT_GRAY);
        algPanel.add(new JLabel("Operation: "));
        algPanel.add(abPanel);
        algPanel.add(new JLabel(" Click node to start with."));
        changeFontSize(6f, algPanel);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(scales, BorderLayout.SOUTH);

        canvas = new JPanel();
        canvas.setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int c = (int) ((e.getX() - leftSep) / nodeSep);
                int r = (int) ((e.getY() - topSep) / nodeSep);

                System.out.println("(" + e.getX() + "," + e.getY() + ") -> " + "(" + c + "," + r + ")");

                //String selectedtAlgorithm = algorithms.getSelectionModel().getSelectedItem();
                String selectedtAlgorithm = algGroup.getSelection().getActionCommand();
                if (selectedtAlgorithm == null) {
                    selectedtAlgorithm = "";
                }
                try {
                    if (graph != null && c >= 0 && c < graph.getNumColumns() && r >= 0 && r < graph.getNumRows()) {
                        System.out.println("Node # " + graph.nodeNum(r, c));
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (selectedtAlgorithm.equals("Dijkstra")) {
                                System.out.println("Dijkstra");
                                long start = System.nanoTime();
                                pathsSS = GraphUtils.dijkstra(graph, graph.nodeNum(r, c));
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Bellman-Ford")) {
                                System.out.println("Bellman-Ford");
                                long start = System.nanoTime();
                                pathsSS = GraphUtils.bellmanFord(graph, graph.nodeNum(r, c));
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Floyd-Warshall")) {
                                System.out.println("Floyd-Warshall");
                                long start = System.nanoTime();
                                pathsAll = GraphUtils.floydWarshall(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                pathsSS = pathsAll.getSSPaths(graph.nodeNum(r, c));
                                mst = null;
                            } else if (selectedtAlgorithm.equals("BFS")) {
                                System.out.println("BFS");
                                long start = System.nanoTime();
                                pathsSS = GraphUtils.bfs(graph, graph.nodeNum(r, c));
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("DFS Recursive")) {
                                System.out.println("DFS Recursive");
                                long start = System.nanoTime();
                                pathsSS = GraphUtils.dfs(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("DFS Iterative")) {
                                System.out.println("Iterative DFS");
                                long start = System.nanoTime();
                                pathsSS = GraphUtils.dfs_iterative(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Kruskal")) {
                                System.out.println("MST by Kruskal");
                                long start = System.nanoTime();
                                mst = GraphUtils.kruskal(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                GraphUtils.saveGridGraph(new GridGraph(graph.getNumColumns(), graph.getNumRows(), mst), new PrintWriter(new File("LastMST")));
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Prim")) {
                                System.out.println("MST by Prim");
                                long start = System.nanoTime();
                                mst = GraphUtils.prim(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                GraphUtils.saveGridGraph(new GridGraph(graph.getNumColumns(), graph.getNumRows(), mst), new PrintWriter(new File("LastMST")));
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Prim_CLRS")) {
                                System.out.println("MST by Prim");
                                long start = System.nanoTime();
                                mst = GraphUtils.classical_prim(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                GraphUtils.saveGridGraph(new GridGraph(graph.getNumColumns(), graph.getNumRows(), mst), new PrintWriter(new File("LastMST")));
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            }
                            drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                            if (pathsSS != null) {
                                colorNodes(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                                ArrayList<Integer> longestPath = decodePathTo(pathsSS.farthest);
                                printPath(longestPath);
                            } else {
                                nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + " - none - ");
                            }
                            if (mst != null) {
                                drawMST(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                            }
                        }
                        if (e.getButton() == MouseEvent.BUTTON3 && pathsAll != null) {
                            pathsSS = pathsAll.getSSPaths(graph.nodeNum(r, c));
                            for (int i = 0; i < pathsSS.p.length; i++) {
                                System.out.print(" " + pathsSS.p[i]);
                            }
                            System.out.println();
                            colorNodes(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                        }
                        if (e.getButton() == MouseEvent.BUTTON2) {
                            if (pathsSS != null) {
                                int dn = graph.nodeNum(r, c);
                                System.out.println("Path to node " + dn);
                                ArrayList<Integer> path = decodePathTo(dn);
                                printPath(path);
                                drawPath(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight(), path);
                            } else {
                                System.out.println("No paths defined!");
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getLocalizedMessage());
                }

            }
        });
        add(canvas, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        add(algPanel, BorderLayout.SOUTH);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == '+') {
                    changeFontSize(4f, SwingGUI.this);
                }
                if (ke.getKeyChar() == '-') {
                    changeFontSize(-4f, SwingGUI.this);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();

        // Display the frame
        setVisible(true);
    }

    private void changeFontSize(float by, Container where) {
        for (Component component : where.getComponents()) {
            if (component instanceof Container) {
                changeFontSize(by, ((Container) component));
            }
            try {
                //System.err.print(component.getClass().getName() + ": " + component.getFont().getSize());
                Font font = component.getFont();
                float size = font.getSize() + by; // Change font size by by units
                component.setFont(font.deriveFont(size));
                //System.err.println(" -> " + component.getFont().getSize());
            } catch (Exception e) {
            }
        }
    }

    private void drawGraph(Graphics g, int width, int height) {
        Graphics2D gc = (Graphics2D) g;

        gc.setColor(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        if (graph == null || graph.getNumNodes() < 1) {
            return;
        }
        gc.setColor(Color.GRAY);
        gc.setStroke(new BasicStroke(2));
        nodeSep = BASICNODESEP;
        int rows = graph.getNumRows();
        int cols = graph.getNumColumns();
        if (leftSep + cols * nodeSep + nodeSize / 2 > width) {
            nodeSep = (int) ((width - leftSep - nodeSize / 2) / cols);
        }
        if (topSep + rows * nodeSep + nodeSize / 2 > height) {
            nodeSep = (int) ((height - topSep - nodeSize / 2) / rows);
        }
        if (nodeSep < 1) {
            return;
        }
        nodeSize = (int) (nodeSize * nodeSep / BASICNODESEP);
        nodeSize = nodeSize < MINNODESIZE ? MINNODESIZE : nodeSize;
        //System.out.println("Node size: " + nodeSize + " sep: " + nodeSep);
        int[][] rc = new int[graph.getNumNodes()][2];
        for (int n = 0; n < graph.getNumNodes(); n++) {
            rc[n][0] = n % rows;  // column
            rc[n][1] = n / rows;  // row
            //System.out.println(n + "-> r=" + rc[n][0] + " c=" + rc[n][1]);
        }

        gc.setColor(Color.DARK_GRAY);
        for (int r = 0; r < graph.getNumRows(); r++) {
            for (int c = 0; c < graph.getNumColumns(); c++) {
                gc.fillOval(leftSep + c * nodeSep, topSep + r * nodeSep, nodeSize, nodeSize);
            }
        }

        for (int n = 0; n < graph.getNumNodes(); n++) {
            Set<Edge> edges = graph.getConnectionsList(n);
            for (Edge e : edges) {
                Color c = edgeCM.getColorForValue(e.getWeight());
                //System.out.println(e.getNodeA() + "--" + e.getNodeB() + " : " + e.getWeight() + "->" + c);
                gc.setColor(c);
                int nA = e.getNodeA();
                int nB = e.getNodeB();
                gc.drawLine(leftSep + nodeSize / 2 + rc[nA][1] * nodeSep, topSep + nodeSize / 2 + rc[nA][0] * nodeSep, leftSep + nodeSize / 2 + rc[nB][1] * nodeSep, topSep + nodeSize / 2 + rc[nB][0] * nodeSep);
            }
        }

    }

    private void drawMST(Graphics g, int width, int height) {
        Graphics2D gc = (Graphics2D) g;
        gc.setColor(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        if (graph == null || mst == null || graph.getNumNodes() < 1) {
            return;
        }
        gc.setColor(Color.GRAY);
        gc.setStroke(new BasicStroke(2));
        int rows = graph.getNumRows();
        int cols = graph.getNumColumns();
        if (leftSep + cols * nodeSep + nodeSize / 2 > width) {
            nodeSep = (int) ((width - leftSep - nodeSize / 2) / cols);
        }
        if (topSep + rows * nodeSep + nodeSize / 2 > height) {
            nodeSep = (int) ((height - topSep - nodeSize / 2) / rows);
        }
        if (nodeSep < 1) {
            return;
        }
        nodeSize = (int) (nodeSize * nodeSep / BASICNODESEP);
        nodeSize = nodeSize < MINNODESIZE ? MINNODESIZE : nodeSize;
        //System.out.println("Node size: " + nodeSize + " sep: " + nodeSep);
        int[][] rc = new int[graph.getNumNodes()][2];
        for (int n = 0; n < graph.getNumNodes(); n++) {
            rc[n][0] = n % rows;  // column
            rc[n][1] = n / rows;  // row
            //System.out.println(n + "-> r=" + rc[n][0] + " c=" + rc[n][1]);
        }
        for (int n = 0; n < graph.getNumNodes(); n++) {
            Set<Edge> edges = mst.getConnectionsList(n);
            for (Edge e : edges) {
                Color c = edgeCM.getColorForValue(e.getWeight());
                //System.out.println(e.getNodeA() + "--" + e.getNodeB() + " : " + e.getWeight() + "->" + c);
                gc.setColor(c);
                int nA = e.getNodeA();
                int nB = e.getNodeB();
                gc.drawLine(leftSep + nodeSize / 2 + rc[nA][1] * nodeSep, topSep + nodeSize / 2 + rc[nA][0] * nodeSep, leftSep + nodeSize / 2 + rc[nB][1] * nodeSep, topSep + nodeSize / 2 + rc[nB][0] * nodeSep);
            }
        }
        for (int r = 0; r < graph.getNumRows(); r++) {
            for (int c = 0; c < graph.getNumColumns(); c++) {
                gc.fillOval(leftSep + c * nodeSep, topSep + r * nodeSep, nodeSize, nodeSize);
            }
        }
    }

    private void colorNodes(Graphics g, double width, double height) {
        Graphics2D gc = (Graphics2D) g;
        if (graph == null || graph.getNumNodes() < 1) {
            return;
        }
        double[] colors = pathsSS.d;
        nodeCM = new ColorMap(pathsSS.dMin, pathsSS.dMax);
        nodeColorMapLabel.setIcon(new ImageIcon(nodeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
        nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + String.format("%.3g", pathsSS.dMin) + " -- " + String.format("%.3g", pathsSS.dMax));
        for (int r = 0; r < graph.getNumRows(); r++) {
            for (int c = 0; c < graph.getNumColumns(); c++) {
                gc.setColor(nodeCM.getColorForValue(colors[graph.nodeNum(r, c)]));
                gc.fillOval(leftSep + c * nodeSep, topSep + r * nodeSep, nodeSize, nodeSize);
            }
        }
    }

    private void drawPath(Graphics g, double width, double height, ArrayList<Integer> path) {
        Graphics2D gc = (Graphics2D) g;
        if (graph == null || graph.getNumNodes() < 1) {
            return;
        }
        gc.setColor(Color.WHITE);
        gc.setStroke(new BasicStroke(4));
        int nA = path.get(0);
        int nB;
        for (int i = 1; i < path.size(); i++) {
            nB = path.get(i);
            gc.drawLine(leftSep + nodeSize / 2 + graph.col(nA) * nodeSep, topSep + nodeSize / 2 + graph.row(nA) * nodeSep, leftSep + nodeSize / 2 + graph.col(nB) * nodeSep, topSep + nodeSize / 2 + graph.row(nB) * nodeSep);
            nA = nB;
        }
    }

    private ArrayList<Integer> decodePathTo(int farthest) {
        ArrayList<Integer> path = new ArrayList<>();
        //restore from last to source
        for (; farthest >= 0; farthest = pathsSS.p[farthest]) {
            path.add(farthest);
        }
        //and reverse
        for (int i = 0, j = path.size() - 1; i < j; i++, j--) {
            int tmp = path.get(i);
            path.set(i, path.get(j));
            path.set(j, tmp);
        }
        return path;
    }

    private void printPath(ArrayList<Integer> path) {
        for (int i = 0; i < path.size(); i++) {
            System.out.print(" " + path.get(i));
        }
        System.out.println("\t length = " + pathsSS.d[path.get(path.size() - 1)]);
    }

    public static void main(String[] args) {
        // Run the application
        SwingUtilities.invokeLater(() -> new SwingGUI());
    }
}
