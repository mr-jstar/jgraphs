package fxgui;

import graphs.AllToAllGraphPaths;
import graphs.Graph;
import graphs.GridGraph;
import graphs.Edge;
import graphs.SingleSourceGraphPaths;
import graphs.GraphAlgorithms;
import graphs.GraphIO;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

/**
 *
 * @author jstar
 */
public class FXGUI extends Application {

    final static String[] algorithms = {"BFS", "DFS Recursive", "DFS Iterative", "Dijkstra", "Bellman-Ford", "Floyd-Warshall", "Kruskal", "Prim", "Prim_CLRS"};

    final static int DEFAULTWIDTH = 1600;
    final static int DEFAULTHEIGHT = DEFAULTWIDTH - 600;

    final static int MINNODESIZE = 10;
    final static int BASICNODESEP = 80;
    final static int BASICNODESIZE = 20;
    
    final static String FONTSTYLE = "-fx-font-size: 14pt";

    private final int leftSep = 10;
    private final int topSep = 10;
    private int nodeSize = BASICNODESIZE;
    private int nodeSep = BASICNODESEP;

    private GraphicsContext gc;
    private GridGraph graph;
    private Canvas canvas;
    private double minWght = 0;
    private double maxWght = 20;
    private final ColorMap edgeCM = new ColorMap(minWght, maxWght);

    private double edgesPerNode = 4;

    private ColorMap nodeCM;
    private Label nodeViewMinLabel;
    private Label nodeViewMaxLabel;
    private Label edgeViewMinLabel;
    private Label edgeViewMaxLabel;

    private final int plotWidth = DEFAULTWIDTH;
    private final int plotHeight = DEFAULTHEIGHT;

    private SingleSourceGraphPaths pathsSS = null;
    private AllToAllGraphPaths pathsAll = null;
    private Graph mst = null;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private final ToggleGroup algGroup = new ToggleGroup();

    //private final Random random = new Random();
    @Override
    public void start(Stage primaryStage) {

        Label slabel = new Label("Grid size: ");
        TextField sTextField = new TextField();
        sTextField.setMaxWidth(100);
        sTextField.setAlignment(Pos.CENTER);
        sTextField.setText("10 x 10");
        HBox shbox = new HBox(sTextField);

        Label rlabel = new Label("Edge weight range: ");
        TextField rTextField = new TextField();
        rTextField.setMaxWidth(100);
        rTextField.setAlignment(Pos.CENTER);
        rTextField.setText(minWght + " : " + maxWght);
        HBox rhbox = new HBox(rTextField);

        Label elabel = new Label("Edges per node: ");
        TextField eTextField = new TextField();
        eTextField.setMaxWidth(40);
        eTextField.setAlignment(Pos.CENTER);
        eTextField.setText("" + edgesPerNode);
        HBox ehbox = new HBox(eTextField);

        HBox phbox = new HBox(5, slabel, shbox, rlabel, rhbox, elabel, ehbox);

        Button abtn = new Button();
        abtn.setText("Generate");
        abtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    String[] cr = sTextField.getText().split("\\s*x\\s*");
                    String[] mx = rTextField.getText().split("\\s*:\\s*");
                    minWght = Double.parseDouble(mx[0]);
                    maxWght = Double.parseDouble(mx[1]);
                    edgeViewMinLabel.setText("" + minWght);
                    edgeViewMaxLabel.setText("" + maxWght);
                    edgeCM.setMin(minWght);
                    edgeCM.setMax(maxWght);
                    edgesPerNode = Double.parseDouble(eTextField.getText());
                    long start = System.nanoTime();
                    graph = new GridGraph(Integer.parseInt(cr[0]), Integer.parseInt(cr[1]), minWght, maxWght, edgesPerNode);
                    long finish = System.nanoTime();
                    System.out.println((finish - start) / 1000 + " microseconds");
                    pathsSS = null;
                    System.out.println("Draw graph " + graph.getNumColumns() + "x" + graph.getNumRows());
                    nodeSep = BASICNODESEP;
                    drawGraph(gc, canvas.getWidth(), canvas.getHeight());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });

        Button rbtn = new Button();
        rbtn.setText("Redraw");
        rbtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Redraw graph");
                drawGraph(gc, canvas.getWidth(), canvas.getHeight());
                if (pathsSS != null) {
                    colorNodes(gc, canvas.getWidth(), canvas.getHeight());
                }
            }
        });

        Button dbtn = new Button();
        dbtn.setText("Delete");
        dbtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Delete graph");
                graph = null;
                pathsSS = null;
                drawGraph(gc, canvas.getWidth(), canvas.getHeight());
            }
        });

        Button sbtn = new Button();
        sbtn.setText("Save");
        sbtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (graph != null) {
                    FileChooser fileChooser = new FileChooser();
                    //FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                    //fileChooser.getExtensionFilters().add(extFilter);
                    File file = fileChooser.showSaveDialog(primaryStage);
                    if (file != null) {
                        System.out.println("Save graph");
                        try {
                            GraphIO.saveGridGraph(graph, new PrintWriter(file));
                        } catch (IOException e) {
                            System.out.println("NOT SAVED: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        });

        Button readbtn = new Button();
        readbtn.setText("Load");
        readbtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (graph == null) {
                    graph = new GridGraph();
                }
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    System.out.println("Load graph");
                    try {
                        Reader r = new FileReader(file);
                        graph = GraphIO.readGridGraph(r);
                        r.close();
                        sTextField.setText(graph.getNumColumns() + " x " + graph.getNumRows());
                        minWght = graph.getMinEdgeWeight();
                        maxWght = graph.getMaxEdgeWeight();
                        rTextField.setText(minWght + " : " + maxWght);
                        edgeViewMinLabel.setText("" + minWght);
                        edgeViewMaxLabel.setText("" + maxWght);
                        edgeCM.setMin(minWght);
                        edgeCM.setMax(maxWght);
                        drawGraph(gc, canvas.getWidth(), canvas.getHeight());
                    } catch (IOException e) {
                        System.out.println("NOT LOADED: " + e.getLocalizedMessage());
                    }
                }
            }
        }
        );

        Button ebtn = new Button();

        ebtn.setText(
                "Exit");
        ebtn.setOnAction(
                new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event
            ) {
                System.exit(0);
            }
        }
        );
        final Pane spacer0 = new Pane();

        HBox.setHgrow(spacer0, Priority.ALWAYS);

        spacer0.setMinSize(10, 1);

        HBox btnBox = new HBox(30, phbox, abtn, rbtn, readbtn, sbtn, dbtn, spacer0, ebtn);

        btnBox.setPadding(new Insets(5, 5, 5, 5));
        btnBox.setPrefWidth(plotWidth);

        FlowPane root = new FlowPane();

        root.getChildren().add(btnBox);

        HBox buttBox = new HBox();
        buttBox.setSpacing(10);

        for (String s : algorithms) {
            RadioButton aB = new RadioButton(s);
            aB.setToggleGroup(algGroup);
            aB.setSelected(true);
            buttBox.getChildren().add(aB);
        }
        root.getChildren().add(buttBox);

        HBox obox = new HBox(30, new Label("Operation:"), buttBox);
        obox.setPadding(new Insets(5, 5, 5, 5));
        root.getChildren().add(obox);

        canvas = new Canvas(plotWidth, plotHeight);

        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {

                int c = (int) ((e.getX() - leftSep) / nodeSep);
                int r = (int) ((e.getY() - topSep) / nodeSep);

                System.out.println("(" + e.getX() + "," + e.getY() + ") -> " + "(" + c + "," + r + ")");

                //String selectedtAlgorithm = algorithms.getSelectionModel().getSelectedItem();
                String selectedtAlgorithm = ((RadioButton) algGroup.getSelectedToggle()).getText();
                if (selectedtAlgorithm == null) {
                    selectedtAlgorithm = "";
                }
                try {
                    if (graph != null && c >= 0 && c < graph.getNumColumns() && r >= 0 && r < graph.getNumRows()) {
                        System.out.println("Node # " + graph.nodeNum(r, c));
                        if (e.getButton() == MouseButton.PRIMARY) {
                            if (selectedtAlgorithm.equals("Dijkstra")) {
                                System.out.println("Dijkstra");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.dijkstra(graph, graph.nodeNum(r, c));
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Bellman-Ford")) {
                                System.out.println("Bellman-Ford");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.bellmanFord(graph, graph.nodeNum(r, c));
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Floyd-Warshall")) {
                                System.out.println("Floyd-Warshall");
                                long start = System.nanoTime();
                                pathsAll = GraphAlgorithms.floydWarshall(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                pathsSS = pathsAll.getSSPaths(graph.nodeNum(r, c));
                                mst = null;
                            } else if (selectedtAlgorithm.equals("BFS")) {
                                System.out.println("BFS");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.bfs(graph, graph.nodeNum(r, c));
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("DFS Recursive")) {
                                System.out.println("DFS Recursive");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.dfs(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("DFS Iterative")) {
                                System.out.println("Iterative DFS");
                                long start = System.nanoTime();
                                pathsSS = GraphAlgorithms.dfs_iterative(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                mst = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Kruskal")) {
                                System.out.println("MST by Kruskal");
                                long start = System.nanoTime();
                                mst = GraphAlgorithms.kruskal(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                GraphIO.saveGridGraph( new GridGraph(graph.getNumColumns(), graph.getNumRows(), mst),new PrintWriter(new File("LastMST")));
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Prim")) {
                                System.out.println("MST by Prim");
                                long start = System.nanoTime();
                                mst = GraphAlgorithms.prim(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                GraphIO.saveGridGraph(new GridGraph(graph.getNumColumns(), graph.getNumRows(), mst),new PrintWriter(new File("LastMST")));
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            } else if (selectedtAlgorithm.equals("Prim_CLRS")) {
                                System.out.println("MST by Prim");
                                long start = System.nanoTime();
                                mst = GraphAlgorithms.classical_prim(graph);
                                long finish = System.nanoTime();
                                System.out.println((finish - start) / 1000 + " microseconds");
                                GraphIO.saveGridGraph(new GridGraph(graph.getNumColumns(), graph.getNumRows(), mst),new PrintWriter(new File("LastMST")));
                                System.out.println("MST generated and saved as GridGraph to file \"LastMST\"");
                                pathsSS = null;
                                pathsAll = null;
                            }
                            drawGraph(gc, canvas.getWidth(), canvas.getHeight());
                            if (pathsSS != null) {
                                colorNodes(gc, canvas.getWidth(), canvas.getHeight());
                                ArrayList<Integer> longestPath = decodePathTo(pathsSS.farthest);
                                printPath(longestPath);
                            }
                            if (mst != null) {
                                drawMST(gc, canvas.getWidth(), canvas.getHeight());
                            }
                        }
                        if (e.getButton() == MouseButton.MIDDLE && pathsAll != null) {
                            pathsSS = pathsAll.getSSPaths(graph.nodeNum(r, c));
                            for (int i = 0; i < pathsSS.p.length; i++) {
                                System.out.print(" " + pathsSS.p[i]);
                            }
                            System.out.println();
                            colorNodes(gc, canvas.getWidth(), canvas.getHeight());
                        }
                        if (e.getButton() == MouseButton.SECONDARY) {
                            if (pathsSS != null) {
                                int dn = graph.nodeNum(r, c);
                                System.out.println("Path to node " + dn);
                                ArrayList<Integer> path = decodePathTo(dn);
                                printPath(path);
                                drawPath(gc, canvas.getWidth(), canvas.getHeight(), path);
                            } else {
                                System.out.println("No paths defined!");
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getLocalizedMessage());
                }

            }

        };

        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);

        gc = canvas.getGraphicsContext2D();

        root.getChildren()
                .add(canvas);

        edgeViewMinLabel = new Label("" + minWght);
        edgeViewMaxLabel = new Label("" + maxWght);
        Label esLabel = new Label("Edge color scale");
        final Pane spacer1 = new Pane();

        HBox.setHgrow(spacer1, Priority.ALWAYS);

        spacer1.setMinSize(
                10, 1);
        final Pane spacer2 = new Pane();

        HBox.setHgrow(spacer2, Priority.ALWAYS);

        spacer2.setMinSize(10, 1);
        HBox lbox = new HBox(edgeViewMinLabel, spacer1, esLabel, spacer2, edgeViewMaxLabel);

        lbox.setPrefWidth(plotWidth);

        lbox.setStyle("-fx-background-color:#FAFAFA;");
        root.getChildren().add(lbox);
        root.getChildren().add(new ImageView(edgeCM.createColorScaleImage(plotWidth, 20, Orientation.HORIZONTAL)));
        nodeCM = new ColorMap(0, 1);
        nodeViewMinLabel = new Label("0");
        nodeViewMaxLabel = new Label("1");
        Label nsLabel = new Label("Node color scale");
        final Pane spacer3 = new Pane();

        HBox.setHgrow(spacer3, Priority.ALWAYS);

        spacer1.setMinSize(10, 1);
        final Pane spacer4 = new Pane();

        HBox.setHgrow(spacer4, Priority.ALWAYS);

        spacer2.setMinSize(10, 1);
        HBox kbox = new HBox(nodeViewMinLabel, spacer3, nsLabel, spacer4, nodeViewMaxLabel);

        kbox.setPrefWidth(plotWidth);

        kbox.setStyle("-fx-background-color:#FAFAFA;");
        root.getChildren().add(kbox);
        
        root.setStyle(FONTSTYLE);

        Scene scene = new Scene(root, plotWidth, plotHeight + 120);

        scene.setOnKeyPressed(e -> pressedKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        primaryStage.setTitle("Kratka");
        primaryStage.setScene(scene);

        primaryStage.show();
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

    private void drawGraph(GraphicsContext gc, double width, double height) {

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        if (graph == null || graph.getNumVertices() < 1) {
            return;
        }
        gc.setFill(Color.GREY);
        gc.setLineWidth(2);
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
        int[][] rc = new int[graph.getNumVertices()][2];
        for (int n = 0; n < graph.getNumVertices(); n++) {
            rc[n][0] = n % rows;  // column
            rc[n][1] = n / rows;  // row
            //System.out.println(n + "-> r=" + rc[n][0] + " c=" + rc[n][1]);
        }
        for (int n = 0; n < graph.getNumVertices(); n++) {
            Set<Edge> edges = graph.getConnectionsList(n);
            for (Edge e : edges) {
                Color c = edgeCM.getColorForValue(e.getWeight());
                //System.out.println(e.getVertexA() + "--" + e.getVertexB() + " : " + e.getWeight() + "->" + c);
                gc.setStroke(c);
                int nA = e.getVertexA();
                int nB = e.getVertexB();
                gc.strokeLine(leftSep + nodeSize / 2 + rc[nA][1] * nodeSep, topSep + nodeSize / 2 + rc[nA][0] * nodeSep, leftSep + nodeSize / 2 + rc[nB][1] * nodeSep, topSep + nodeSize / 2 + rc[nB][0] * nodeSep);
            }
        }
        for (int r = 0; r < graph.getNumRows(); r++) {
            for (int c = 0; c < graph.getNumColumns(); c++) {
                gc.fillOval(leftSep + c * nodeSep, topSep + r * nodeSep, nodeSize, nodeSize);
            }
        }
    }

    private void drawMST(GraphicsContext gc, double width, double height) {

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        if (graph == null || mst == null || graph.getNumVertices() < 1) {
            return;
        }
        gc.setFill(Color.GREY);
        gc.setLineWidth(2);
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
        int[][] rc = new int[graph.getNumVertices()][2];
        for (int n = 0; n < graph.getNumVertices(); n++) {
            rc[n][0] = n % rows;  // column
            rc[n][1] = n / rows;  // row
            //System.out.println(n + "-> r=" + rc[n][0] + " c=" + rc[n][1]);
        }
        for (int n = 0; n < graph.getNumVertices(); n++) {
            Set<Edge> edges = mst.getConnectionsList(n);
            for (Edge e : edges) {
                Color c = edgeCM.getColorForValue(e.getWeight());
                //System.out.println(e.getVertexA() + "--" + e.getVertexB() + " : " + e.getWeight() + "->" + c);
                gc.setStroke(c);
                int nA = e.getVertexA();
                int nB = e.getVertexB();
                gc.strokeLine(leftSep + nodeSize / 2 + rc[nA][1] * nodeSep, topSep + nodeSize / 2 + rc[nA][0] * nodeSep, leftSep + nodeSize / 2 + rc[nB][1] * nodeSep, topSep + nodeSize / 2 + rc[nB][0] * nodeSep);
            }
        }
        for (int r = 0; r < graph.getNumRows(); r++) {
            for (int c = 0; c < graph.getNumColumns(); c++) {
                gc.fillOval(leftSep + c * nodeSep, topSep + r * nodeSep, nodeSize, nodeSize);
            }
        }
    }

    private void colorNodes(GraphicsContext gc, double width, double height) {
        if (graph == null || graph.getNumVertices() < 1) {
            return;
        }
        double[] colors = pathsSS.d;
        nodeCM = new ColorMap(pathsSS.dMin, pathsSS.dMax);
        nodeViewMinLabel.setText("" + pathsSS.dMin);
        nodeViewMaxLabel.setText("" + pathsSS.dMax);
        for (int r = 0; r < graph.getNumRows(); r++) {
            for (int c = 0; c < graph.getNumColumns(); c++) {
                gc.setFill(nodeCM.getColorForValue(colors[graph.nodeNum(r, c)]));
                gc.fillOval(leftSep + c * nodeSep, topSep + r * nodeSep, nodeSize, nodeSize);
            }
        }
    }

    private void drawPath(GraphicsContext gc, double width, double height, ArrayList<Integer> path) {
        if (graph == null || graph.getNumVertices() < 1) {
            return;
        }
        gc.setLineWidth(4);
        gc.setStroke(Color.WHITE);
        int nA = path.get(0);
        int nB;
        for (int i = 1; i < path.size(); i++) {
            nB = path.get(i);
            gc.strokeLine(leftSep + nodeSize / 2 + graph.col(nA) * nodeSep, topSep + nodeSize / 2 + graph.row(nA) * nodeSep, leftSep + nodeSize / 2 + graph.col(nB) * nodeSep, topSep + nodeSize / 2 + graph.row(nB) * nodeSep);
            nA = nB;
        }
    }

    /*
    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);
        gc.strokeLine(40, 10, 10, 40);
        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
        gc.fillPolygon(new double[]{10, 40, 10, 40},
                new double[]{210, 210, 240, 240}, 4);
        gc.strokePolygon(new double[]{60, 90, 60, 90},
                new double[]{210, 210, 240, 240}, 4);
        gc.strokePolyline(new double[]{110, 140, 110, 140},
                new double[]{210, 210, 240, 240}, 4);
    }
     */
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
