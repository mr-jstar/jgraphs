/*
 * Do what you want with this file
 */
package swinggui;

/**
 *
 * @author jstar
 */
import fem.mesh.IMesh;
import graphs.AllToAllGraphPaths;
import graphs.Edge;
import graphs.Graph;
import graphs.GraphAlgorithms;
import graphs.GridGraph;
import graphs.SingleSourceGraphPaths;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SwingGeneralGUI extends JFrame {

    final static String[] algorithms = {"BFS", "DFS Recursive", "DFS Iterative", "Dijkstra", "Bellman-Ford", "Floyd-Warshall", "Kruskal", "Prim", "Prim_CLRS", "Kernighan-Lin"};

    final private String nodeScaleViewLabelTxt = "Color scale for nodes (distance): ";
    final private String edgeScaleViewLabelTxt = "Color scale for edges (weights): ";

    final static int DEFAULTWIDTH = 2000;
    final static int DEFAULTHEIGHT = DEFAULTWIDTH - 200;

    final static int BASICNODESIZE = 20;

    private int leftSep = 10;
    private int topSep = 10;
    private double minWght = 0;
    private double maxWght = 20;

    private JTextField gridSizeTextField, edgeWeightRangeTextField, edgesPerNodeTextField;
    final private JLabel nodeColorMapLabel, edgeColorMapLabel;

    private Graph graph;
    private IMesh mesh;

    private GraphView graphView;
    private JPanel canvas;

    private final ColorMap edgeCM = new ColorMap(minWght, maxWght);

    private double edgesPerNode = 4;

    private ColorMap nodeCM;
    private JLabel nodeScaleViewLabel;
    private JLabel edgeScaleViewLabel;

    private ButtonGroup algGroup;
    private JPanel algPanel;

    private SingleSourceGraphPaths pathsSS = null;
    private AllToAllGraphPaths pathsAll = null;
    private Graph mst = null;

    private List<Thread> runningAlgorithms = new ArrayList<>();

    private List<Edge> division = null;

    private String lastUsedDirectory = ".";

    ActionListener generateGridGraph = new ActionListener() {
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
                graphView = new GridGraphView((GridGraph) graph);
                edgeColorMapLabel.setIcon(new ImageIcon(edgeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
                long finish = System.nanoTime();
                System.out.println((finish - start) / 1000 + " microseconds");
                pathsSS = null;
                pathsAll = null;
                division = null;
                nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + " - none - ");
                System.out.println("Draw graph " + ((GridGraph) graph).getNumColumns() + "x" + ((GridGraph) graph).getNumRows());
                drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
            } catch (Exception ex) {
                error(ex.getClass() + ": " + ex.getMessage());
            }
        }

    };

    public SwingGeneralGUI() {
        // Set up the frame
        setTitle("Swing Graph GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
        setLayout(new BorderLayout());

        JLabel gridLabel = new JLabel("Grid size:");
        gridSizeTextField = new JTextField(10);
        gridSizeTextField.setText("10 x 10");
        gridSizeTextField.addActionListener(generateGridGraph);

        JLabel edgeLabel = new JLabel("Edge weight range:");
        edgeWeightRangeTextField = new JTextField(10);
        edgeWeightRangeTextField.setText("0 : 20");

        JLabel nodeLabel = new JLabel("Edges per node:");
        edgesPerNodeTextField = new JTextField(5);
        edgesPerNodeTextField.setText("4");

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(generateGridGraph);

        JButton redrawButton = new JButton("Redraw");
        redrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Redraw graph");
                redrawContent(e);
            }
        });
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Delete graph");
                graph = null;
                graphView = null;
                pathsSS = null;
                pathsAll = null;
                division = null;
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
                if (graph != null && graph instanceof GridGraph gridGraph) {
                    JFileChooser fileChooser = new JFileChooser(lastUsedDirectory);
                    changeFontSize(6f, fileChooser);
                    if (fileChooser.showSaveDialog(SwingGeneralGUI.this) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        lastUsedDirectory = file.getParent();
                        System.out.println("Save graph");
                        try {
                            GraphAlgorithms.saveGridGraph(gridGraph, new PrintWriter(file));
                        } catch (IOException e) {
                            error("GRAPH NOT SAVED: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        });
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser(lastUsedDirectory);
                changeFontSize(6f, fileChooser);
                if (fileChooser.showOpenDialog(SwingGeneralGUI.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    lastUsedDirectory = file.getParent();
                    String name = file.getName();
                    division = null;
                    if (name.endsWith(".poly") || name.endsWith(".node") || name.endsWith(".ele")) {
                        System.out.println("Load mesh");
                        try {
                            mesh = fem.Util.loadTriangleMesh(file.getAbsolutePath());
                            graph = fem.Util.graphOfIMesh(mesh);
                            graphView = new MeshGraphView(mesh);
                            minWght = graph.getMinEdgeWeight();
                            maxWght = graph.getMaxEdgeWeight();
                            edgeWeightRangeTextField.setText(String.format(Locale.US, "%.3g", minWght) + " : " + String.format(Locale.US, "%.3g", maxWght));
                            edgeScaleViewLabel.setText(edgeScaleViewLabelTxt + String.format(Locale.US, "%.3g", minWght) + "--" + String.format(Locale.US, "%.3g", maxWght));
                            edgeCM.setMin(minWght);
                            edgeCM.setMax(maxWght);
                            edgeColorMapLabel.setIcon(new ImageIcon(edgeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
                            drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                        } catch (Exception ex) {
                            error("GRAPH NOT LOADED: " + ex.getLocalizedMessage());
                        }
                    } else {
                        System.out.println("Load graph");
                        try {
                            Reader r = new FileReader(file);
                            graph = GraphAlgorithms.readGridGraph(r);
                            graphView = new GridGraphView((GridGraph) graph);
                            r.close();
                            gridSizeTextField.setText(((GridGraph) graph).getNumColumns() + " x " + ((GridGraph) graph).getNumRows());
                            minWght = graph.getMinEdgeWeight();
                            maxWght = graph.getMaxEdgeWeight();
                            edgeWeightRangeTextField.setText(String.format("%.3g", minWght) + " : " + String.format("%.3g", maxWght));
                            edgeScaleViewLabel.setText(edgeScaleViewLabelTxt + String.format("%.3g", minWght) + "--" + String.format("%.3g", maxWght));
                            edgeCM.setMin(minWght);
                            edgeCM.setMax(maxWght);
                            edgeColorMapLabel.setIcon(new ImageIcon(edgeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
                            drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                        } catch (IOException ex) {
                            error("GRAPH NOT LOADED: " + ex.getLocalizedMessage());
                        }
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

        JPanel generatePanel = new JPanel();
        generatePanel.setBackground(Color.LIGHT_GRAY);
        generatePanel.add(gridLabel);
        generatePanel.add(gridSizeTextField);
        generatePanel.add(edgeLabel);
        generatePanel.add(edgeWeightRangeTextField);
        generatePanel.add(nodeLabel);
        generatePanel.add(edgesPerNodeTextField);
        generatePanel.add(generateButton);

        JPanel controlPanel = new JPanel();
        controlPanel.add(generatePanel);
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
        abPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        for (String s : algorithms) {
            JRadioButton aB = new JRadioButton(s);
            aB.setActionCommand(s);
            algGroup.add(aB);
            aB.setSelected(true);
            abPanel.add(aB);
        }
        algPanel = new JPanel();
        algPanel.setBackground(Color.LIGHT_GRAY);
        algPanel.add(new JLabel("Algorithm: "));
        algPanel.add(abPanel);
        algPanel.add(new JLabel(" Click the node you want to start with."));
        changeFontSize(6f, algPanel);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(scales, BorderLayout.SOUTH);

        canvas = new JPanel();
        canvas.setSize(DEFAULTWIDTH, DEFAULTHEIGHT);
        canvas.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent ce) {
                redrawContent(ce);
            }

            @Override
            public void componentMoved(ComponentEvent ce) {
                redrawContent(ce);
            }

            @Override
            public void componentShown(ComponentEvent ce) {
                redrawContent(ce);
            }

            @Override
            public void componentHidden(ComponentEvent ce) {
            }

        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (graphView == null) {
                    return;
                }

                int nodeNum = graphView.getNodeNum(e.getX(), e.getY());

                for (Thread t : runningAlgorithms) {
                    if (t.isAlive()) {
                        error("Something still running!");
                        return;
                    }
                }

                System.out.println("(" + e.getX() + "," + e.getY() + ") -> " + nodeNum);

                //String selectedtAlgorithm = algorithms.getSelectionModel().getSelectedItem();
                String selectedtAlgorithm = algGroup.getSelection().getActionCommand();
                if (selectedtAlgorithm == null) {
                    selectedtAlgorithm = "";
                } else {
                    algPanel.setForeground(Color.GREEN);
                    algPanel.repaint();
                    System.err.println(selectedtAlgorithm);
                }
                try {
                    if (graph != null && nodeNum >= 0) {
                        System.out.println("Node # " + nodeNum);
                        division = null;
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (selectedtAlgorithm.equals("Dijkstra")) {
                                System.out.println("Dijkstra");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.dijkstra(graph, nodeNum);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Bellman-Ford")) {
                                System.out.println("Bellman-Ford");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.bellmanFord(graph, nodeNum);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Floyd-Warshall")) {
                                System.out.println("Floyd-Warshall");
                                long start = System.nanoTime();
                                pathsAll = GraphAlgorithms.floydWarshall(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                pathsSS = pathsAll.getSSPaths(nodeNum);
                                mst = null;
                            } else if (selectedtAlgorithm.equals("BFS")) {
                                System.out.println("BFS");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.bfs(graph, nodeNum);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("DFS Recursive")) {
                                System.out.println("DFS Recursive");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.dfs(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("DFS Iterative")) {
                                System.out.println("Iterative DFS");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.dfs_iterative(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Kruskal")) {
                                System.out.println("MST by Kruskal");
                                long start = System.nanoTime();
                                mst = GraphAlgorithms.kruskal(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1e6 + " miliseconds");
                                if (graph instanceof GridGraph) {
                                    GraphAlgorithms.saveGridGraph(new GridGraph(((GridGraph) graph).getNumColumns(), ((GridGraph) graph).getNumRows(), mst), new PrintWriter(new File("LastMST")));
                                }
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Prim")) {
                                Thread t = new Thread() {
                                    {
                                        setDaemon(true);
                                    }

                                    @Override
                                    public void run() {
                                        System.out.println("MST by Prim");
                                        long start = System.nanoTime();
                                        mst = GraphAlgorithms.prim(graph);
                                        long finish = System.nanoTime();
                                        System.out.println("MST generated in " + (finish - start) / 1e6 + " miliseconds");
                                        if (graph instanceof GridGraph) {
                                            try {
                                                GraphAlgorithms.saveGridGraph(new GridGraph(((GridGraph) graph).getNumColumns(), ((GridGraph) graph).getNumRows(), mst), new PrintWriter(new File("LastMST")));
                                                System.out.println("MST saved as GridGraph to file \"LastMST\"");
                                            } catch (IOException ex) {
                                                error("MST not saved: " + ex.getLocalizedMessage());
                                            }
                                        }
                                        pathsSS = null;
                                        pathsAll = null;
                                    }
                                };
                                t.start();
                                runningAlgorithms.add(t);
                            } else if (selectedtAlgorithm.equals("Prim_CLRS")) {
                                Thread t = new Thread() {
                                    {
                                        setDaemon(true);
                                    }

                                    @Override
                                    public void run() {
                                        System.out.println("MST by Prim");
                                        long start = System.nanoTime();
                                        mst = GraphAlgorithms.classical_prim(graph);
                                        long finish = System.nanoTime();
                                        System.out.println("MST generated in " + (finish - start) / 1e6 + " miliseconds");
                                        if (graph instanceof GridGraph) {
                                            try {
                                                GraphAlgorithms.saveGridGraph(new GridGraph(((GridGraph) graph).getNumColumns(), ((GridGraph) graph).getNumRows(), mst), new PrintWriter(new File("LastMST")));
                                                System.out.println("MST saved as GridGraph to file \"LastMST\"");
                                            } catch (IOException ex) {
                                                error("MST not saved: " + ex.getLocalizedMessage());
                                            }
                                        }
                                        pathsSS = null;
                                        pathsAll = null;
                                    }
                                };
                                t.start();
                                runningAlgorithms.add(t);
                            } else if (selectedtAlgorithm.equals("Kernighan-Lin")) {
                                Thread t = new Thread() {
                                    {
                                        setDaemon(true);
                                    }

                                    @Override
                                    public void run() {
                                        System.out.println("Kernighan-Lin");
                                        long start = System.nanoTime();
                                        division = GraphAlgorithms.partition_Kernighan_Lin(graph, nodeNum).get(0);
                                        long finish = System.nanoTime();
                                        System.out.println((finish - start) / 1e6 + " miliseconds");
                                        pathsSS = null;
                                        pathsAll = null;
                                        drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
                                    }
                                };
                                t.start();
                                runningAlgorithms.add(t);
                            }

                            if (pathsSS != null) {
                                colorNodes(canvas.getGraphics());
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
                            pathsSS = pathsAll.getSSPaths(nodeNum);
                            for (int i = 0; i < pathsSS.p.length; i++) {
                                System.out.print(" " + pathsSS.p[i]);
                            }
                            System.out.println();
                            colorNodes(canvas.getGraphics()
                            );
                        }
                        if (e.getButton() == MouseEvent.BUTTON2) {
                            if (pathsSS != null) {
                                int dn = nodeNum;
                                System.out.println("Path to node " + dn);
                                ArrayList<Integer> path = decodePathTo(dn);
                                printPath(path);
                                drawPath(canvas.getGraphics(), path);
                            } else {
                                System.out.println("No paths defined!");
                            }
                        }
                    }
                } catch (Exception ex) {
                    error(ex.getClass() + ": " + ex.getLocalizedMessage());
                }
                algPanel.setBackground(Color.LIGHT_GRAY);
            }
        });
        add(canvas, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        add(algPanel, BorderLayout.SOUTH);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == '+') {
                    changeFontSize(4f, SwingGeneralGUI.this);
                }
                if (ke.getKeyChar() == '-') {
                    changeFontSize(-4f, SwingGeneralGUI.this);
                }
            }
        });
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                redrawContent(e);
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                redrawContent(e);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                redrawContent(e);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        setFocusable(true);
        requestFocusInWindow();

        // Display the frame
        setVisible(true);
    }

    private void redrawContent(Object e) {
        System.err.println("Redrawing after " + e.getClass());
        drawGraph(canvas.getGraphics(), canvas.getWidth(), canvas.getHeight());
        if (pathsSS != null) {
            colorNodes(canvas.getGraphics());
        } else {
            nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + " - none - ");
        }
    }

    private void changeFontSize(float by, Container where) {
        for (Component component : where.getComponents()) {
            if (component == null) {
                continue;
            }
            if (component instanceof Container container) {
                changeFontSize(by, container);
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
        } else {
            //System.out.println("Drawing " + graph.getNumNodes() + " nodes.");
        }
        gc.setColor(Color.GRAY);
        gc.setStroke(new BasicStroke(2));
        graphView.recalculateNodeCoordinates(width, height, BASICNODESIZE, leftSep, topSep);

        for (int n = 0; n < graph.getNumNodes(); n++) {
            Set<Edge> edges = graph.getConnectionsList(n);
            for (Edge e : edges) {
                Color c = edgeCM.getColorForValue(e.getWeight());
                //System.out.println(e.getNodeA() + "--" + e.getNodeB() + " : " + e.getWeight() + "->" + c);
                gc.setColor(c);
                Point vA = graphView.getPosition(e.getNodeA());
                Point vB = graphView.getPosition(e.getNodeB());
                gc.drawLine(vA.x, vA.y, vB.x, vB.y);
            }
        }

        if (division != null) {
            for (Edge e : division) {
                gc.setColor(Color.BLACK);
                Point vA = graphView.getPosition(e.getNodeA());
                Point vB = graphView.getPosition(e.getNodeB());
                gc.drawLine(vA.x, vA.y, vB.x, vB.y);
            }
        }

        gc.setColor(Color.DARK_GRAY);
        int nodeSize = graphView.getNodeSize();
        for (int p = 0; p < graph.getNumNodes(); p++) {
            Point v = graphView.getPosition(p);
            //System.out.println(p + "@(" + v + ") " + nodeSize);
            gc.fillOval(v.x - nodeSize / 2, v.y - nodeSize / 2, nodeSize, nodeSize);
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
        gc.setStroke(new BasicStroke(2));
        graphView.recalculateNodeCoordinates(width, height, BASICNODESIZE, leftSep, topSep);

        for (int n = 0; n < graph.getNumNodes(); n++) {
            Set<Edge> edges = mst.getConnectionsList(n);
            for (Edge e : edges) {
                Color c = edgeCM.getColorForValue(e.getWeight());
                //System.out.println(e.getNodeA() + "--" + e.getNodeB() + " : " + e.getWeight() + "->" + c);
                gc.setColor(c);
                Point vA = graphView.getPosition(e.getNodeA());
                Point vB = graphView.getPosition(e.getNodeB());
                gc.drawLine(vA.x, vA.y, vB.x, vB.y);
            }
        }
        int nodeSize = graphView.getNodeSize();
        for (int p = 0; p < graph.getNumNodes(); p++) {
            Point v = graphView.getPosition(p);
            gc.fillOval(v.x - nodeSize / 2, v.y - nodeSize / 2, nodeSize, nodeSize);
        }
    }

    private void colorNodes(Graphics g) {
        Graphics2D gc = (Graphics2D) g;
        if (graph == null || graph.getNumNodes() < 1) {
            return;
        }
        double[] colors = pathsSS.d;
        nodeCM = new ColorMap(pathsSS.dMin, pathsSS.dMax);
        nodeColorMapLabel.setIcon(new ImageIcon(nodeCM.createColorScaleImage(300, 20, SwingConstants.HORIZONTAL)));
        nodeScaleViewLabel.setText(nodeScaleViewLabelTxt + String.format("%.3g", pathsSS.dMin) + " -- " + String.format("%.3g", pathsSS.dMax));
        int nodeSize = graphView.getNodeSize();
        for (int p = 0; p < graph.getNumNodes(); p++) {
            Point v = graphView.getPosition(p);
            gc.setColor(nodeCM.getColorForValue(colors[p]));
            gc.fillOval(v.x - nodeSize / 2, v.y - nodeSize / 2, nodeSize, nodeSize);
        }
    }

    private void drawPath(Graphics g, ArrayList<Integer> path) {
        Graphics2D gc = (Graphics2D) g;
        if (graph == null || graph.getNumNodes() < 1) {
            return;
        }
        gc.setColor(Color.WHITE);
        gc.setStroke(new BasicStroke(4));
        Point vA = graphView.getPosition(path.get(0));
        Point vB;
        for (int i = 1; i < path.size(); i++) {
            vB = graphView.getPosition(path.get(i));
            gc.drawLine(vA.x, vA.y, vB.x, vB.y);
            vA = vB;
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

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Something went wrong!", JOptionPane.QUESTION_MESSAGE);
    }

    public static void main(String[] args) {
        // Run the application
        SwingUtilities.invokeLater(() -> new SwingGeneralGUI());
    }
}
