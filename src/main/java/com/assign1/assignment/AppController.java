package com.assign1.assignment;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.regex.*;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.File;  // Import the File class

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class AppController {
    private int userCount = 0;
    private int edgeCount = 0;
    Scanner scanner = new Scanner(System.in);
    protected Boolean addingNode = false;
    protected Boolean removingNode = false;
    Set<Edge> edges = new HashSet<>(); //keep track of edges in graph, used only when loading and saving from files to avoid iterating through adj nested hashmaps
    EncoderDecoder encoderDecoder = new EncoderDecoder(); //used to encode and decode objects into and from files
    Map<String, NodeFX> circles = new HashMap<>();  //to lookup nodeFX instances through their string name/id
    Map<NodeFX, Map<NodeFX, Edge>> adjList = new HashMap<>(); //nested hashmap adjacency list, ex. to get chris and joes edge object -> hashmap.get(chris).get(joe).getWeight. simple and straightforward
    @FXML
    AnchorPane graphArea; //ui element initializations
    @FXML
    Group nodeGroup;
    @FXML
    ScrollPane scrollPane;
    private final Scale scale = new Scale(1, 1, 0, 0); // Default scaling factors

    @FXML
    private void initialize() {
        // Add the scale transformation to the nodeGroup
        nodeGroup.getTransforms().add(scale);
        // Add scroll event listener to the ScrollPane
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
        scrollPane.addEventFilter(ZoomEvent.ZOOM, this::handleZoom);
        scrollPane.addEventHandler(MouseEvent.MOUSE_MOVED,this::setScrollPane);

    }
    private void setScrollPane(MouseEvent e){
        scrollPane.setPannable(!e.isAltDown());
    }
    //menu item handlers
    @FXML
    private void resetNodes() {
        for (String x : circles.keySet()) {
            NodeFX node = circles.get(x);
            node.minDistance = Float.MAX_VALUE;
            node.previous = null;
            node.reset();
            nodeGroup.getChildren().remove(node.dstToSource);
        }
    }

    @FXML
    private void menuAddNodeHandler() {
        this.addingNode = true; //simple flag toggling
        this.removingNode = false;

    }

    @FXML
    private void menuRemoveNodeHandler() {
        this.removingNode = true; //same here
        this.addingNode = false;

    }

    @FXML
    private void menuInfoHandler() {
        String stringToShow = "\nThere are " + userCount + " users in total, with " + edgeCount + "  edges connecting them";
        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setHeaderText("Info about this graph: ");
        popup.setContentText(stringToShow);
        popup.show();
    }

    @FXML
    private void clearCanvas() { //clear all data structures
        this.userCount = this.edgeCount = 0;
        this.circles.clear();
        this.adjList.clear();
        this.edges.clear();
        this.nodeGroup.getChildren().clear();
    }

    @FXML
    private void sampleHandler() {
        // Fixed node positions for visualization
        NodeFX nodeA = new NodeFX(100, 150, 15, "A");
        NodeFX nodeB = new NodeFX(250, 200, 15, "B");
        NodeFX nodeC = new NodeFX(250, 100, 15, "C");
        NodeFX nodeD = new NodeFX(400, 150, 15, "D");
        NodeFX nodeE = new NodeFX(550, 200, 15, "E");
        NodeFX nodeF = new NodeFX(550, 100, 15, "F");

        adjList.put(nodeA, new HashMap<>());
        adjList.put(nodeB, new HashMap<>());
        adjList.put(nodeC, new HashMap<>());
        adjList.put(nodeD, new HashMap<>());
        adjList.put(nodeE, new HashMap<>());
        adjList.put(nodeF, new HashMap<>());

// Add nodes to circles map
        circles.put("A", nodeA);
        circles.put("B", nodeB);
        circles.put("C", nodeC);
        circles.put("D", nodeD);
        circles.put("E", nodeE);
        circles.put("F", nodeF);

// Initialize adjacency list entries

        nodeA.adjacent.add(nodeB);
        nodeA.adjacent.add(nodeC);

        nodeB.adjacent.add(nodeA);
        nodeB.adjacent.add(nodeD);
        nodeB.adjacent.add(nodeE);

        nodeC.adjacent.add(nodeA);
        nodeC.adjacent.add(nodeD);

        nodeD.adjacent.add(nodeB);
        nodeD.adjacent.add(nodeC);
        nodeD.adjacent.add(nodeE);
        nodeD.adjacent.add(nodeF);

        nodeE.adjacent.add(nodeB);
        nodeE.adjacent.add(nodeD);
        nodeE.adjacent.add(nodeF);

        nodeF.adjacent.add(nodeD);
        nodeF.adjacent.add(nodeE);


// Add nodes to the UI (directly without Platform.runLater)
        nodeGroup.getChildren().addAll(nodeA, nodeA.nodeLabel, nodeA.dstToSource);
        nodeGroup.getChildren().addAll(nodeB, nodeB.nodeLabel, nodeB.dstToSource);
        nodeGroup.getChildren().addAll(nodeC, nodeC.nodeLabel, nodeC.dstToSource);
        nodeGroup.getChildren().addAll(nodeD, nodeD.nodeLabel, nodeD.dstToSource);
        nodeGroup.getChildren().addAll(nodeE, nodeE.nodeLabel, nodeE.dstToSource);
        nodeGroup.getChildren().addAll(nodeF, nodeF.nodeLabel, nodeF.dstToSource);

// Create edges with weights
        Edge edgeAB = new Edge(nodeA, nodeB, 1.0f);
        Edge edgeAC = new Edge(nodeA, nodeC, 0.3f);
        Edge edgeBD = new Edge(nodeB, nodeD, 1f);
        Edge edgeBE = new Edge(nodeB, nodeE, 0.2f);
        Edge edgeCD = new Edge(nodeC, nodeD, 0.8f);
        Edge edgeDE = new Edge(nodeD, nodeE, 1.0f);
        Edge edgeDF = new Edge(nodeD, nodeF, 0.5f);
        Edge edgeEF = new Edge(nodeE, nodeF, 0.6f);

        edges.add(edgeAB);
        edges.add(edgeAC);
        edges.add(edgeBD);
        edges.add(edgeBE);
        edges.add(edgeCD);
        edges.add(edgeDE);
        edges.add(edgeDF);
        edges.add(edgeEF);

// Update adjacency list
        adjList.get(nodeA).put(nodeB, edgeAB);
        adjList.get(nodeA).put(nodeC, edgeAC);
        adjList.get(nodeB).put(nodeA, edgeAB);
        adjList.get(nodeB).put(nodeD, edgeBD);
        adjList.get(nodeB).put(nodeE, edgeBE);
        adjList.get(nodeC).put(nodeA, edgeAC);
        adjList.get(nodeC).put(nodeD, edgeCD);
        adjList.get(nodeD).put(nodeB, edgeBD);
        adjList.get(nodeD).put(nodeC, edgeCD);
        adjList.get(nodeD).put(nodeE, edgeDE);
        adjList.get(nodeD).put(nodeF, edgeDF);
        adjList.get(nodeE).put(nodeB, edgeBE);
        adjList.get(nodeE).put(nodeD, edgeDE);
        adjList.get(nodeE).put(nodeF, edgeEF);
        adjList.get(nodeF).put(nodeD, edgeDF);
        adjList.get(nodeF).put(nodeE, edgeEF);

// Add edges to the UI
        nodeGroup.getChildren().addAll(edgeAB, edgeAB.weightLabel);
        nodeGroup.getChildren().addAll(edgeAC, edgeAC.weightLabel);
        nodeGroup.getChildren().addAll(edgeBD, edgeBD.weightLabel);
        nodeGroup.getChildren().addAll(edgeBE, edgeBE.weightLabel);
        nodeGroup.getChildren().addAll(edgeCD, edgeCD.weightLabel);
        nodeGroup.getChildren().addAll(edgeDE, edgeDE.weightLabel);
        nodeGroup.getChildren().addAll(edgeDF, edgeDF.weightLabel);
        nodeGroup.getChildren().addAll(edgeEF, edgeEF.weightLabel);


    } //creates a sample graph

    @FXML
    private void addConnectionMenuHandler() { //create new thread to fetch inputs from the user in the terminal, then call the addEdge method in the main thread
        Thread addNodeThread = new Thread(() -> {
            System.out.println("Enter user 1: ");
            String user1 = scanner.nextLine();
            System.out.println("\nEnter user 2: ");
            String user2 = scanner.nextLine();
            NodeFX node1 = circles.get(user1);
            NodeFX node2 = circles.get(user2);
            System.out.println("\nEnter Weight: ");
            float weight = scanner.nextFloat();
            Platform.runLater(() -> addEdge(node1, node2, weight));
        });
        addNodeThread.setDaemon(true);
        addNodeThread.start();


    }

    @FXML
    private void testGraphMenuHandler() {
        clearCanvas();
        AtomicReference<String> input = new AtomicReference<>(); //used to capture user input
        TextInputDialog popup = new TextInputDialog(); //dialog to prompt user input
        popup.setHeaderText("How many users to create?"); //set dialog header text
        popup.setTitle("Random graph creation"); //set dialog title
        popup.showAndWait().ifPresent(input::set); //wait for user input
        int intInput;
        try {
            intInput = Integer.parseInt(input.get());
        } catch (Error e) {
            System.out.println("Something went wrong when parsing the amount of nodes as integer");
            return;
        }
        if (intInput > 5000) return;
        int spacing = (int) (100 + 100 * Math.log(intInput)); //distance between nodes in both x and y directions
        int rows = (int) Math.sqrt(intInput); //determine the number of rows based on the total number of nodes
        int cols = (int) Math.ceil((double) intInput / rows); //determine the number of columns based on the number of rows
        RandomGenerator randomGenerator = new Random();
        int currentNode = 0; //track how many nodes have been created
        for (int i = 1; i < rows + 1; i++) { //loop through rows with an offset of 5
            for (int j = 1; j < cols + 1; j++) { //loop through columns also with an offset
                if (currentNode < intInput) { //only add nodes until the specified total is reached
                    int x = j * randomGenerator.nextInt(spacing - 10, spacing + 10); //x-coordinate is based on column index and spacing
                    int y = i * randomGenerator.nextInt(spacing - 10, spacing + 10);
                    //y-coordinate is based on row index and spacing
                    addNode(x, y, String.valueOf(currentNode)); //add node at calculated position with unique name
                    currentNode++; //increment node count
                }
            }
        }
        for (int i = 0; i < intInput * 2; i++) {
            NodeFX user1 = (NodeFX) circles.values().toArray()[randomGenerator.nextInt(0, circles.size() - 1)];
            NodeFX user2 = (NodeFX) circles.values().toArray()[randomGenerator.nextInt(0, circles.size() - 1)];

            if (adjList.get(user1).containsKey(user2) || adjList.get(user2).containsKey(user1) || user1.equals(user2)) {
                i--;
                continue;
            }
            float[] values = {0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F, 0.9F, 1.0F};
            addEdge(user1, user2, values[randomGenerator.nextInt(0, values.length - 1)]);

        }
    }

    @FXML
    private void loadFileHandler() { //handler for the menu button
        clearCanvas();//clear canvas
        AtomicReference<String> file = new AtomicReference<>(); //initialize atomic reference to store filename
        TextInputDialog popup = new TextInputDialog();
        popup.setContentText("Enter name of file");
        popup.setHeaderText("Loading sequence engaged.");
        popup.setTitle("Race for e-unity");
        popup.showAndWait().ifPresent(file::set);// get input and call set method of Atomic Reference
        if (popup.getResult() == null || popup.getResult().isEmpty()) return;
        encoderDecoder.readFromFile(file.get()); //call readFromFile method of encoder object
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) { // Only zoom when CTRL is held down
            double zoomFactor = event.getDeltaY() > 0 ? 0.90 : 1.1; // Zoom in or out, getDelta returns the scroll direction (>0) multipliers can be changed to adjust scrolling speed
            scale.setX(scale.getX() * zoomFactor); //applying the scaling
            scale.setY(scale.getY() * zoomFactor);
            event.consume(); //consume event
        }
    }

    private void handleZoom(ZoomEvent event) {
        scale.setX(scale.getX() * event.getZoomFactor()); //applying the scaling
        scale.setY(scale.getY() * event.getZoomFactor());
        event.consume(); //consume event
    }

    @FXML
    private void saveGraphHandler() throws IOException {
        AtomicReference<String> file = new AtomicReference<>();
        TextInputDialog popup = new TextInputDialog();
        popup.setContentText("Enter name of file");
        popup.setHeaderText("Saving sequence engaged.");
        popup.setTitle("The Future is Euro");
        popup.showAndWait().ifPresent(file::set);//same process to get filename
        if (popup.getResult() == null || popup.getResult().isEmpty()) return;
        encoderDecoder.saveToFile(file.get()); //calling saveToFile method
    }

    @FXML
    private void canvasClickHandler(MouseEvent e) {
        if (addingNode) { //if state boolean addingNode is active
            TextInputDialog popup = new TextInputDialog();
            popup.setHeaderText("User Creation");
            popup.setContentText("Enter new username");
            popup.setTitle("Input");
            popup.showAndWait().ifPresent(input -> { //get input and create node at mouseevent coordinates
                addNode((int) e.getX(), (int) e.getY(), input);
            });
        } else if (removingNode) { //else if the remove node state is enabled we simply handle this exception since the event handler for removing a node is on the node itself. (clicking the canvas does nothing)
            Alert warning = new Alert(Alert.AlertType.WARNING, "Click on a node, not the background!");
            warning.show();
            removingNode = false;
        }
    }

    @FXML
    private void handleNodeClick(MouseEvent e) {
        if (removingNode) {
            removeNode((NodeFX) e.getTarget());//when node is clicked in removing mode, call the removeNode method on the target node
        }
    }

    @FXML
    private void communityDetectionHandler() {
        AtomicReference<String> weight = new AtomicReference<>();
        TextInputDialog popup = new TextInputDialog();
        popup.setHeaderText("Minimum connection weight");
        popup.setContentText("Input float in range 0.01 - 1.0");
        popup.setTitle("Input");
        popup.showAndWait().ifPresent(weight::set);
        for (String x : circles.keySet()) {
            NodeFX node = circles.get(x);
            node.setFill(Paint.valueOf("black"));
        }
        if (Objects.equals(weight.get(), "")) {
            weight.set("0.01");
        }
        try {
            Thread task = new Thread(() -> runCommunityDetection(Float.parseFloat(weight.get())));
            task.setDaemon(true);
            task.start();
        } catch (Exception e) {
            System.err.println("Error parsing threshold input.");
        }


    }

    @FXML
    private void dijkstraHandler() { //creates a new thread to handle the algorithm, looks up the node instance of the given source and passes it to the runDijkstra method
        AtomicReference<NodeFX> source = new AtomicReference<>();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Dijkstra Algorithm");
        dialog.setContentText("Enter start node:");
        dialog.showAndWait().ifPresent(start -> {
            source.set(circles.get(start));
            if (source.get() == null) {//handle invalid source name
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid start node!");
                alert.show();
            }
        });
        for (String x : circles.keySet()) { //prepare the shortest distance labels for the graph
            NodeFX node = circles.get(x);
            node.minDistance = Float.MAX_VALUE;
            node.previous = null;
            node.reset();
            node.dstToSource.setText("INF");
            node.dstToSource.setLayoutX(circles.get(x).getCenterX() + 10);
            node.dstToSource.setLayoutY(circles.get(x).getCenterY() - 35);
            node.dstToSource.setTextFill(Paint.valueOf("gray"));
        }
        Thread dijkstraThread = new Thread(() -> runDijkstra(source.get()));
        dijkstraThread.setDaemon(true);
        dijkstraThread.start();
    }

    @FXML
    private void friendSuggestionHandler() {
        Thread friendSuggestion = new Thread(this::friendSuggestion);
        friendSuggestion.setDaemon(true);
        friendSuggestion.start();
    }

    //Graph operations
    private void addEdge(NodeFX node1, NodeFX node2, float weight) { //
        if (weight < 0.01 || weight > 1 || nodeDoesNotExist(node1) || nodeDoesNotExist(node2)) {
            System.out.println("\nInvalid input for edge creation");
            return;
        }
        Edge temp = new Edge(node1, node2, weight); //temp variable

        adjList.get(node1).put(node2, temp); //update the two users' adj list entries
        adjList.get(node2).put(node1, temp);

        node1.adjacent.add(node2); //update inner neighbor node list for both users
        node2.adjacent.add(node1);
        edges.add(temp); //update this set too

        nodeGroup.getChildren().add(temp); //add to actual UI
        nodeGroup.getChildren().add(temp.weightLabel);

        edgeCount++;
    }

    private void removeNode(NodeFX node) {
        this.nodeGroup.getChildren().remove(node);  //removing the node, the label of the node from the actual group
        this.nodeGroup.getChildren().remove(node.nodeLabel); //removing label
        this.nodeGroup.getChildren().remove(node.dstToSource); //removing dijkstra dst label
        this.circles.remove(node.getName()); //removing node from circles
        for (NodeFX user : adjList.get(node).keySet()) { //get ALL users that are associated with remove target iterate through them
            nodeGroup.getChildren().remove(adjList.get(node).get(user)); //relate the edge and weight label
            nodeGroup.getChildren().remove(adjList.get(node).get(user).weightLabel);
            adjList.get(user).remove(node); //removing the adjlist entry of the removal target node from the other associated user
            user.adjacent.remove(node); //update local adjacency list of the associated user
            this.edges.remove(adjList.get(node).get(user)); //remove the edge object from the edges set
            edgeCount--;
        }
        adjList.remove(node); // finally drop the removal target's entry in the adjlist
        this.removingNode = false;
        userCount--;
    }

    private void addNode(Integer x, Integer y, String name) {
        if (!nodeExists(name)) { //if node doesn't already exist
            NodeFX temp = new NodeFX(x, y, 15, name);
            adjList.put(temp, new HashMap<>()); //new entry in adjlist
            circles.put(name, temp);
            nodeGroup.getChildren().addAll(temp, temp.nodeLabel, temp.dstToSource); // Add both Circle and Label to the Group
        } else {
            Alert denied = new Alert(Alert.AlertType.ERROR);
            denied.setHeaderText("User already exists!");
            denied.setTitle("Cannot comply");
            denied.show();
        }
        userCount++;
        addingNode = false;
    }

    private boolean nodeExists(String username) {
        return circles.containsKey(username);
    }

    private boolean nodeDoesNotExist(NodeFX user) {
        return !circles.containsValue(user);
    }

    private class NodeFX extends Circle {
        protected String name;

        protected Label nodeLabel = new Label();
        protected ArrayList<NodeFX> adjacent = new ArrayList<>();
        protected Float minDistance = Float.MAX_VALUE;
        protected Label dstToSource = new Label();
        private NodeFX previous = null;

        public boolean equals(Object obj) {
            if (obj instanceof NodeFX u) {
                return Objects.equals(u.getId(), this.getId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getId());
        }

        NodeFX(double x, double y, double rad, String name) {
            super(x, y, rad, Paint.valueOf("black"));
            this.setStroke(Paint.valueOf("black"));
            this.name = name;
            this.nodeLabel.setText(name);
            this.nodeLabel.setTextFill(Paint.valueOf("black"));
            nodeLabel.setLayoutX(x - 3 - name.length() * 1.1);
            nodeLabel.setLayoutY(y + 16);
            this.setId(name);
            this.setOnMouseClicked(event -> {
                handleNodeClick(event);
                event.consume();
            });

            this.setOnDragDetected((_) -> this.startFullDrag());

            this.setOnMouseDragReleased(mouseEvent -> {
                if (mouseEvent.getSource().equals(mouseEvent.getGestureSource()))
                    return; //edge case where shaking the mouse while holding a node could trigger an edge creation with the node itself
                AtomicReference<String> weight = new AtomicReference<>();
                TextInputDialog popup = new TextInputDialog("Weight");
                popup.setHeaderText("Please enter Connection Weight");
                popup.showAndWait().ifPresent(weight::set);
                addEdge((NodeFX) mouseEvent.getGestureSource(), (NodeFX) mouseEvent.getSource(), Float.parseFloat(weight.get())); //create the edge
                mouseEvent.consume();

            });
            this.setOnContextMenuRequested(event -> {
                StringBuilder stringToShow = new StringBuilder("You right-clicked node " + this.name + ":\nThey are adjacent to: ");
                for (NodeFX temp : this.adjacent) {
                    stringToShow.append("\nNode ").append(temp.getName()).append(": weight=").append(adjList.get(this).get(temp).getWeight()).append(" ");//get edge weight
                }
                Alert popup = new Alert(Alert.AlertType.INFORMATION);
                popup.setHeaderText("Info about this node: ");
                popup.setContentText(stringToShow.toString());
                popup.show();


                event.consume();
            });

            this.setOnMouseDragged(e -> {
                if (e.isAltDown()) return;
                double parentOffsetX = e.getX();
                double parentOffsetY = e.getY();

                // Adjust position of the node itself
                this.setCenterX(parentOffsetX);
                this.setCenterY(parentOffsetY);

                // Adjust position of the label
                this.nodeLabel.setLayoutX(parentOffsetX - 3 - name.length() * 1.1);
                this.nodeLabel.setLayoutY(parentOffsetY + 16);

                // Update adjacent edges
                for (NodeFX adjacentNode : this.getAdjacent()) {
                    Edge edge = adjList.get(this).get(adjacentNode);
                    if (edge.source.equals(this)) { //we need to adjust the correct end of the line as the edge source and destination properties are not guaranteed to be in the right order
                        edge.setStartX(this.getCenterX());
                        edge.setStartY(this.getCenterY());

                    } else {
                        edge.setEndX(this.getCenterX());
                        edge.setEndY(this.getCenterY());
                    }
                    edge.weightLabel.setLayoutX((edge.destination.getCenterX() + edge.source.getCenterX()) / 2);
                    edge.weightLabel.setLayoutY((edge.destination.getCenterY() + edge.source.getCenterY()) / 2 + 10);
                }
                if (!this.dstToSource.isNeedsLayout()) {
                    this.dstToSource.setLayoutX(this.getCenterX() + 10);
                    this.dstToSource.setLayoutY(this.getCenterY() - 35);
                }

                e.consume();
            });
        }

        String getName() {
            return name;
        }

        ArrayList<NodeFX> getAdjacent() {
            return adjacent;
        }

        void setActive() {
            this.setFill(Paint.valueOf("green"));

        }

        void setVisited() {
            this.setFill(Paint.valueOf("gray"));

        }

        void reset() {
            this.setFill(Paint.valueOf("black"));
        }

        void setMinDistance(Float x) {
            this.dstToSource.setText(String.valueOf(x).substring(0,3));
        }


    }

    private class Edge extends Line {

        protected NodeFX source;
        protected NodeFX destination;
        protected Float weight;
        protected Label weightLabel = new Label();

        Float getWeight() {
            return weight;
        }

        Edge(NodeFX source, NodeFX destination, Float weight) {
            super(source.getCenterX(), source.getCenterY(), destination.getCenterX(), destination.getCenterY());
            this.source = source;
            this.destination = destination;
            this.weight = weight;

            weightLabel.setText(weight.toString());
            weightLabel.setLayoutX((destination.getCenterX() + source.getCenterX()) / 2);
            weightLabel.setLayoutY((destination.getCenterY() + source.getCenterY()) / 2 + 10);
            weightLabel.setTextFill(Paint.valueOf("Black"));

            this.setId(String.format("%s-%s", source.getId(), destination.getId()));

            this.weightLabel.setOnMouseClicked(_ -> { //event handler to change edge weight when clicking on the weight text
                TextInputDialog popup = new TextInputDialog("Weight");
                popup.setHeaderText("Please enter Connection Weight");
                popup.showAndWait().ifPresent(input -> {
                    if (Float.parseFloat(input) >= 0.01F && Float.parseFloat(input) <= 1) {
                        this.weight = Float.parseFloat(input);
                        this.weightLabel.setText(input);
                    } else {
                        Alert warning = new Alert(Alert.AlertType.ERROR);
                        warning.setTitle("Cannot Comply.");
                        warning.setHeaderText("Use value in range 0.01 - 1.0");
                        warning.show();
                    }
                });
            });
        }

    }

    private class EncoderDecoder {

        String encode(NodeFX node) {
            return String.format("n[%s,%s,%s]\n", node.name, node.getCenterX(), node.getCenterY()); //example output n[A,100.0,150.0]
        }

        String encode(Edge edge) {
            return String.format("e[%s,%s,%s]\n", edge.source.name, edge.destination.name, edge.getWeight()); //example output e[D,F,0.5]
        }

        void readFromFile(String filename) {
            File myFile; //file object to represent the input file
            Scanner myReader; //scanner to read the file line by line
            try {
                myFile = new File(filename); //initialize file object with the given filename
                myReader = new Scanner(myFile); //initialize scanner to read the file
            } catch (IOException e) {
                System.out.println("An error occurred opening the file"); //error handling for file not found or reading issues
                return; //exit the function on error
            }
            while (myReader.hasNext()) { //iterate through each line in the file
                String line = myReader.nextLine(); //read the current line
                String regex;
                if (line.charAt(0) == 'n') { //check if the line starts with 'n' (node representation)
                    regex = "n\\[(.*?),(.*?),(.*?)]"; //regex to match node format: n[name,value1,value2]
                    Pattern pattern = Pattern.compile(regex); //compile the regex pattern
                    Matcher matcher = pattern.matcher(line); //create a matcher for the current line
                    if (matcher.matches()) { //check if the line matches the regex pattern
                        // Parse the node details
                        String name;
                        int value1;
                        int value2;
                        try {
                            name = matcher.group(1); //capture the name of the node
                            value1 = (int) Float.parseFloat(matcher.group(2)); //convert first value to an integer
                            value2 = (int) Float.parseFloat(matcher.group(3)); //convert second value to an integer
                        } catch (Exception e) {
                            System.out.println("Error when parsing the coordinates in line: " + line);
                            continue;
                        }
                        addNode(value1, value2, name); //add the parsed node to the graph
                        userCount++;
                    }

                } else if (line.charAt(0) == 'e') { //check if the line starts with 'e' (edge representation)
                    regex = "e\\[(.*?),(.*?),(.*?)]"; //regex to match edge format: e[source,destination,weight]
                    Pattern pattern = Pattern.compile(regex); //compile the regex pattern
                    Matcher matcher = pattern.matcher(line); //create a matcher for the current line

                    if (matcher.matches()) { //check if the line matches the regex pattern
                        // Parse the edge details
                        String source = matcher.group(1); //capture the source node name
                        String destination = matcher.group(2);//capture the destination node name
                        float weight;
                        try {
                            weight = Float.parseFloat(matcher.group(3));
                        } catch (Exception e) {
                            System.out.println("Error converting from float on line: " + line);
                            continue;
                        } //convert weight to a float
                        addEdge(circles.get(source), circles.get(destination), weight); //add the edge to the graph
                        edgeCount++;
                    }
                }

            }
        }

        void saveToFile(String filename) throws IOException {
            FileWriter myFile; //file writer object to write to the file
            try {
                myFile = new FileWriter(filename); //initialize the file writer with the given filename
            } catch (IOException e) {
                System.out.println("An error occurred."); //error handling for issues creating the file
                return; //exit the function on error
            }
            for (NodeFX node : circles.values()) { //iterate through all nodes in the graph
                myFile.write(encode(node)); //write the encoded node representation to the file
            }
            for (Edge edge : edges) { //iterate through all edges in the graph
                myFile.write(encode(edge)); //write the encoded edge representation to the file
            }
            myFile.close(); //close the file writer to save changes
        }

    }

    //Algorithms
    private void runDijkstra(NodeFX source) {
        for (String x : circles.keySet()) { //set initial values on all nodes
            NodeFX node = circles.get(x);
            node.minDistance = Float.MAX_VALUE;
            node.previous = null;
            Platform.runLater(node::reset);
        }
        Platform.runLater(() -> source.setMinDistance(0F));
        source.minDistance = 0F;

        //priority queue with custom comparator
        PriorityQueue<NodeFX> priorityQueue = new PriorityQueue<>(10, Comparator.comparing(node -> node.minDistance));
        priorityQueue.add(source);


        while (!priorityQueue.isEmpty()) { //dijkstra implementation
            NodeFX current = priorityQueue.poll(); //get current node
            Platform.runLater(current::setActive); //set it as active

            for (NodeFX node : current.getAdjacent()) { //iterate through its neighbors
                Float newDistance = current.minDistance + adjList.get(current).get(node).getWeight(); //current distance to source
                if (newDistance < node.minDistance) {//if the new distance is better than the old
                    priorityQueue.remove(node); //remove the node from priority list
                    node.minDistance = newDistance; //adjust distance property
                    node.previous = current; //set pointer to previous node
                    Platform.runLater(() -> node.setMinDistance(newDistance)); //on main thread, change its label to the new distance
                    priorityQueue.add(node); //read the node with updated values
                }
            }
            try {
                Thread.sleep(250); //sleep for once second for visualization purposes
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(current::setVisited); //set node as visited


        }
        for (NodeFX user : circles.values()) { //this part is where we retrace our steps to the source for every node (for loop iterates through every node)
            if (user.equals(source)) continue; //we don't need the source

            System.out.print("\nShortest path from source to " + user.getName() + ": ");

            if (user.minDistance == Float.MAX_VALUE) { //if the minDistance value has not been touched at all, it means that the node has no available path to the source
                System.out.println("No path found.");
                continue;
            }

            // Use a stack to reconstruct the path
            Stack<String> pathStack = new Stack<>();
            NodeFX currIter = user;

            while (currIter != null) {//currIter being null means we reached the source, before we reach it we iterate through the pointers and push the names to the stack
                pathStack.push(currIter.getName());
                currIter = currIter.previous;
            }

            // Print the path in the correct order
            while (!pathStack.isEmpty()) {
                System.out.print(pathStack.pop());
                if (!pathStack.isEmpty()) {
                    System.out.print(" -> ");
                }
            }
        }


    }

    private void runCommunityDetection(float threshold) {
        Supplier<Color> supplier = () -> Color.color(Math.random(), Math.random(), Math.random());
        Set<Set<NodeFX>> communities = new HashSet<>();
        Set<NodeFX> visited = new HashSet<>();

        for (String user : circles.keySet()) {
            NodeFX node = circles.get(user);
            Color currentColor = supplier.get();
            if (!visited.contains(node)) {
                Queue<NodeFX> fxQueue = new LinkedList<>();
                Set<NodeFX> community = new HashSet<>();
                fxQueue.add(node);

                while (!fxQueue.isEmpty()) {
                    NodeFX current = fxQueue.poll();
                    community.add(current);
                    visited.add(current);
                    for (NodeFX x : current.getAdjacent()) {
                        if (adjList.get(current).get(x).getWeight() >= threshold && !visited.contains(x)) {
                            community.add(x);
                            Platform.runLater(() -> x.setFill(currentColor));
                            fxQueue.add(x);
                        }
                    }
                }
                // Normalize the community by converting to a sorted list, then back to a set
                if (community.size() > 1) {
                    communities.add(community);
                    Platform.runLater(() -> {
                        for (NodeFX x : community){
                            x.setFill(currentColor);
                        }
                    });

                }
            }
        }
        for (Set<NodeFX> x : communities) {
            System.out.println("\n\nSeparator: ");
            for (NodeFX c : x) {
                System.out.print("  " + c.name + "  ");
            }
        }
    }


    private void friendSuggestion() {
        Map<NodeFX, Set<NodeFX>> friendSuggestions = new HashMap<>(); //keep track of friend suggestions
        for (String x : circles.keySet()) {
            NodeFX current = circles.get(x);
            friendSuggestions.put(current, new HashSet<>());
            for (NodeFX adjNode1 : current.getAdjacent()) {
                for (NodeFX adjNode2 : adjNode1.getAdjacent()) {
                    if (!current.getAdjacent().contains(adjNode2) && !current.equals(adjNode2)) {
                        friendSuggestions.get(current).add(adjNode2);
                    }
                }
            }
        }

        for (Map.Entry<NodeFX, Set<NodeFX>> entry : friendSuggestions.entrySet()) {
            NodeFX user = entry.getKey();
            Set<NodeFX> suggestions = entry.getValue();
            System.out.println("\nUser: " + user.getName());
            System.out.println("Suggested Friends: ");
            for (NodeFX suggestedFriend : suggestions) {
                System.out.println(suggestedFriend.getName());
            }
        }
    }
}


