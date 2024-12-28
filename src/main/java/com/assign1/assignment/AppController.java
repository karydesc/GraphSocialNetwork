package com.assign1.assignment;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;

import java.util.regex.*;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.File;  // Import the File class

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class AppController {
    int userCount = 0;
    int edgeCount = 0;
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
    MenuBar menuBar;
    @FXML
    ScrollPane scrollPane;
    @FXML
    private void loadFileHandler(){ //handler for the menu button
        clearCanvas();//clear canvas
        AtomicReference<String> file = new AtomicReference<>(); //initialize atomic reference to store filename
        TextInputDialog popup = new TextInputDialog();
        popup.setContentText("Enter name of file");
        popup.setHeaderText("Loading sequence engaged.");
        popup.setTitle("Race for e-unity");
        popup.showAndWait().ifPresent(file::set); // get input and call set method of Atomic Reference
        encoderDecoder.readFromFile(file.get()); //call readfromfile meethod of encoder object
    }
    private final Scale scale = new Scale(1, 1, 0, 0); // Default scaling factors
    @FXML
    public void initialize() {
        // Add the scale transformation to the nodeGroup
        nodeGroup.getTransforms().add(scale);

        // Add scroll event listener to the ScrollPane
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) { // Only zoom when CTRL is held down
            double zoomFactor = event.getDeltaY() > 0 ? 0.99 : 1.01; // Zoom in or out, getdelta returns the scroll direction (>0) multipliers can be changed to adjust scrolling speed
            scale.setX(scale.getX() * zoomFactor); //applying the scaling
            scale.setY(scale.getY() * zoomFactor);
            event.consume(); //consume event
        }}
    @FXML
    private void saveGraphHandler() throws IOException {
        AtomicReference<String> file = new AtomicReference<>();
        TextInputDialog popup = new TextInputDialog();
        popup.setContentText("Enter name of file");
        popup.setHeaderText("Saving sequence engaged.");
        popup.setTitle("The Future is Euro");
        popup.showAndWait().ifPresent(file::set);//same process to get filename
        encoderDecoder.saveToFile(file.get()); //calling savetofile method
    }
    @FXML
    public void CanvasClickHandler(MouseEvent e) {
        if (addingNode){ //if state boolean addingnode is active
            TextInputDialog popup = new TextInputDialog();
            popup.setHeaderText("User Creation");
            popup.setContentText("Enter new username");
            popup.setTitle("Input");
            popup.showAndWait().ifPresent( input -> { //get input and create node at mouseevent coordinates
                        addNode((int) e.getX(), (int) e.getY(),input);
                    });
        } else if (removingNode) { //else if the remove node state is enabled we simply handle this exception since the event handler for removing a node is on the node itself. (clicking the canvas does nothing)
            Alert warning = new Alert(Alert.AlertType.WARNING, "Click on a node, not the background silly!");
            warning.show();
            removingNode=false;
        }
    }
    @FXML
    public void handleNodeClick(MouseEvent e){
        if(removingNode){
            removeNode((NodeFX) e.getTarget());//when node is clicked in removing mode, call the removeNode method on the target node
        }
    }
    @FXML
    public void menuAddNodeHandler(){
        this.addingNode = true; //simple flag toggling
        this.removingNode=false;

    }
    @FXML
    public void menuRemoveNodeHandler(){
        this.removingNode = true; //same here
        this.addingNode=false;

    }
    @FXML
    public void clearCanvas(){ //clear all data structures
        this.circles.clear();
        this.adjList.clear();
        this.edges.clear();
        this.nodeGroup.getChildren().clear();
    }
    @FXML
    public void addConnectionMenuHandler(){ //create new thread to fetch inputs from the user in the terminal, then call the addedge method in the main thread
        Thread addNodeThread = new Thread(() -> {
            System.out.println("Enter user 1: ");
            String user1 = scanner.nextLine();
            System.out.println("\nEnter user 2: ");
            String user2 = scanner.nextLine();
            NodeFX node1 = circles.get(user1);
            NodeFX node2 = circles.get(user2);
            System.out.println("\nEnter Weight: ");
            float weight = scanner.nextFloat();
            Platform.runLater(() ->addEdge(node1, node2, weight));
        });
        addNodeThread.setDaemon(true);
        addNodeThread.start();


    }
    @FXML
    public void testGraphMenuHandler() {
        clearCanvas();
        AtomicReference<String> input = new AtomicReference<>(); //used to capture user input
        TextInputDialog popup = new TextInputDialog(); //dialog to prompt user input
        popup.setHeaderText("How many users to create?"); //set dialog header text
        popup.setTitle("Despite everything, it's still you"); //set dialog title
        popup.showAndWait().ifPresent(input::set); //wait for user input
        int intInput = Integer.parseInt(input.get()); //parse the user input to an integer

        int spacing = 100; //distance between nodes in both x and y directions
        int rows = (int) Math.sqrt(intInput); //determine the number of rows based on the total number of nodes
        int cols = (int) Math.ceil((double) intInput / rows); //determine the number of columns based on the number of rows

        int currentNode = 0; //track how many nodes have been created
        for (int i = 5; i < rows+5; i++) { //loop through rows with an offset of 10
            for (int j = 5; j < cols+5; j++) { //loop through columns also with an offset
                if (currentNode < intInput) { //only add nodes until the specified total is reached
                    int x = j * spacing; //x-coordinate is based on column index and spacing
                    int y = i * spacing; //y-coordinate is based on row index and spacing
                    addNode(x, y, String.valueOf(currentNode)); //add node at calculated position with unique name
                    currentNode++; //increment node count
                }
            }
        }
    }

    private void addEdge(NodeFX node1, NodeFX node2, float weight) { //
        if (weight < 0.01 || weight > 1 || nodeDoesNotExist(node1) || nodeDoesNotExist(node2)) {
            System.out.println("\nno");
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

    @FXML
    public void dijkstraHandler(){ //creates a new thread to handle the algorithm, looks up the node instance of the given source and passes it to the runDijkstra method
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
        for (String x : circles.keySet()){ //prepare shortest distance labels for the graph
            circles.get(x).dstToSource.setText("INF");
            circles.get(x).dstToSource.setLayoutX(circles.get(x).getCenterX()+10);
            circles.get(x).dstToSource.setLayoutY(circles.get(x).getCenterY()-35);
            circles.get(x).dstToSource.setTextFill(Paint.valueOf("gray"));
        }
        Thread dijkstraThread = new Thread(() -> runDijkstra(source.get()));
        dijkstraThread.setDaemon(true);
        dijkstraThread.start();
    }
    @FXML
    public void sampleHandler(){
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

    void removeNode(NodeFX node){
        this.nodeGroup.getChildren().remove(node);  //removing the node, the label of the node from the actual group
        this.nodeGroup.getChildren().remove(node.nodeLabel); //removing label
        this.nodeGroup.getChildren().remove(node.dstToSource); //removing dijkstra dst label
        this.circles.remove(node.getName()); //removing node from circles
        for (NodeFX user : adjList.get(node).keySet()){ //get ALL users that are associated with remove target iterate through them
            nodeGroup.getChildren().remove(adjList.get(node).get(user)); //relete the edge and weight label
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

    public void addNode(Integer x, Integer y, String name){
        if (!nodeExists(name)){ //if node doesnt already exist
            NodeFX temp = new NodeFX(x, y, 15, name);
            adjList.put(temp, new HashMap<>()); //new entry in adjlist
            circles.put(name, temp);
            nodeGroup.getChildren().addAll(temp, temp.nodeLabel, temp.dstToSource); // Add both Circle and Label to the Group
        }else{
            Alert denied = new Alert(Alert.AlertType.ERROR);
            denied.setHeaderText("User already exists!");
            denied.setTitle("Ya silly goose...");
            denied.show();
        }
        userCount++;
        addingNode=false;
    }

    boolean nodeExists(String username) {
        return circles.containsKey(username);
    }
    boolean nodeDoesNotExist(NodeFX user){
        return !circles.containsValue(user);
    }

    void runDijkstra(NodeFX source){
        for (String x : circles.keySet()){ //set intial values on all nodes
            NodeFX node = circles.get(x);
            node.isActive=false;
            node.isVisited=false;
            node.minDistance=Float.MAX_VALUE;
            node.previous=null;
            Platform.runLater(node::reset);
        }
        Platform.runLater(() -> source.setMinDistance(0F));
        source.minDistance=0F;

        //priority queue with custom comparator
        PriorityQueue<NodeFX> priorityQueue=new PriorityQueue<>(10, (node1, node2) -> {
            if (node1.minDistance > node2.minDistance) return 1;
            if (node1.minDistance < node2.minDistance) return -1;
            return 0;
        });
        priorityQueue.add(source);



        while (!priorityQueue.isEmpty()) { //dijkstra implementation
            NodeFX current = priorityQueue.poll(); //get current node
            Platform.runLater(() -> current.setActive(true)); //set it as active

            for (NodeFX node : current.getAdjacent()){ //iterate throught its neighbors
                Float newDistance = current.minDistance + adjList.get(current).get(node).getWeight(); //current distance to source
                if (newDistance<node.minDistance){//if the new distance is better than the old
                    priorityQueue.remove(node); //remove the node from priority list
                    node.minDistance=newDistance; //adjust distance property
                    node.previous=current; //set pointer to previous node
                    Platform.runLater(() -> node.setMinDistance(newDistance)); //on main thread, change its label to the new distance
                    priorityQueue.add(node); //readd the node with updated values
                }
            }
            try {
                Thread.sleep(1000); //sleep for once second for visualization purposes
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> current.setVisited(true)); //set node as visited


        }
        for (NodeFX user : circles.values()) { //this part is where we retrace our steps to the source for every node (for loop iterates through every node)
            if (user.equals(source)) continue; //we dont need the source

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

    class NodeFX extends Circle{
        protected  String name;
        protected  Label nodeLabel = new Label();
        protected  ArrayList<NodeFX> adjacent = new ArrayList<>();
        protected  Float minDistance = Float.MAX_VALUE;
        protected  Label dstToSource = new Label();
        private boolean isActive;
        private boolean isVisited;

         private NodeFX previous = null;

        public boolean equals (Object obj){
            if (obj instanceof NodeFX u){
                return Objects.equals(u.getId(), this.getId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getId());
        }

        NodeFX(double x, double y, double rad, String name){
            super(x,y,rad,Paint.valueOf("black"));
            this.setStroke(Paint.valueOf("black"));
            this.name = name;
            this.nodeLabel.setText(name);
            this.nodeLabel.setTextFill(Paint.valueOf("black"));
            nodeLabel.setLayoutX(x-3-name.length()*1.1);
            nodeLabel.setLayoutY(y+16);
            this.setId(name);
            this.setOnMouseClicked(event -> {
                handleNodeClick(event);
                event.consume();
            });

            this.setOnDragDetected((_) -> {
                this.startFullDrag();
            });

            this.setOnMouseDragReleased(mouseEvent -> {
                AtomicReference<String> weight = new AtomicReference<>();
                TextInputDialog popup = new TextInputDialog("Weight");
                popup.setHeaderText("Please enter Connection Weight");
                popup.showAndWait().ifPresent(weight::set);
                addEdge((NodeFX) mouseEvent.getGestureSource(), (NodeFX) mouseEvent.getSource(), Float.parseFloat(weight.get())); //create the edge


            });
            this.setOnContextMenuRequested(event -> {
            System.out.println("\n\nYou right-clicked: "+this.name + "\nThey are adjacent to: ");
            for (NodeFX temp : this.adjacent){
                System.out.println(temp.getName()+" "+adjList.get(this).get(temp).getWeight()+" ");//get edge weight
            }

            event.consume();
        });
        }

        String getName(){
            return name;
        }
        ArrayList<NodeFX> getAdjacent(){
            return adjacent;
        }
        void setActive(Boolean b) {
            if(b){
                this.setFill(Paint.valueOf("green"));
            }else{
                this.setFill(Paint.valueOf("black"));
            }
            this.isActive=b;
            this.isVisited=!b;
        }
        void setVisited(Boolean b) {
            if(b){
                this.setFill(Paint.valueOf("gray"));
            }else{
                this.setFill(Paint.valueOf("black"));
            }
            this.isActive=!b;
            this.isVisited=b;
        }
        void reset(){
            this.dstToSource.setText("INF");
            this.setFill(Paint.valueOf("black"));
        }

        void setMinDistance(Float x){
            this.dstToSource.setText(String.valueOf(x));
        }

    }
    class Edge extends Line {
        private boolean isActive;
        protected NodeFX source;
        protected NodeFX destination;
        protected Float weight;
        protected Label weightLabel = new Label();

        private Boolean isVisited;
        Float getWeight(){
            return weight;
        }

        Edge(NodeFX source, NodeFX destination, Float weight){
            super(source.getCenterX(), source.getCenterY(), destination.getCenterX(), destination.getCenterY());
            this.source=source;
            this.destination=destination;
            this.weight=weight;

            weightLabel.setText(weight.toString());
            weightLabel.setLayoutX((destination.getCenterX()+source.getCenterX())/2);
            weightLabel.setLayoutY((destination.getCenterY()+source.getCenterY())/2+10);
            weightLabel.setTextFill(Paint.valueOf("Black"));

            this.setId(String.format("%s-%s",source.getId(),destination.getId()));

            this.weightLabel.setOnMouseClicked(_ -> { //event handler to change edge weight when clicking on the weight text
                TextInputDialog popup = new TextInputDialog("Weight");
                popup.setHeaderText("Please enter Connection Weight");
                popup.showAndWait().ifPresent(input -> {
                    if (Float.parseFloat(input)>=0.01F && Float.parseFloat(input)<=1){
                        this.weight=Float.parseFloat(input);
                        this.weightLabel.setText(input);
                    }else{
                        Alert warning = new Alert(Alert.AlertType.ERROR);
                        warning.setTitle("Cannot Comply.");
                        warning.setHeaderText("Use value in range 0.01 - 1.0");
                        warning.show();
                    }
                });
            });
        }
    }
    class EncoderDecoder {
        String encode(NodeFX node){
            return String.format("n[%s,%s,%s]\n",node.name,node.getCenterX(), node.getCenterY()); //example output n[A,100.0,150.0]
        }
        String encode(Edge edge){
            return String.format("e[%s,%s,%s]\n",edge.source.name,edge.destination.name, edge.getWeight()); //example output e[D,F,0.5]
        }
        void readFromFile(String filename) {
            File myFile; //file object to represent the input file
            Scanner myReader; //scanner to read the file line by line
            try {
                myFile = new File(filename); //initialize file object with the given filename
                myReader = new Scanner(myFile); //initialize scanner to read the file
            } catch (IOException e) {
                System.out.println("An error occurred."); //error handling for file not found or reading issues
                e.printStackTrace(); //print stack trace for debugging
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
                        String name = matcher.group(1); //capture the name of the node
                        Integer value1 = (int) Float.parseFloat(matcher.group(2)); //convert first value to an integer
                        Integer value2 = (int) Float.parseFloat(matcher.group(3)); //convert second value to an integer
                        addNode(value1, value2, name); //add the parsed node to the graph
                    }

                } else if (line.charAt(0) == 'e') { //check if the line starts with 'e' (edge representation)
                    regex = "e\\[(.*?),(.*?),(.*?)]"; //regex to match edge format: e[source,destination,weight]
                    Pattern pattern = Pattern.compile(regex); //compile the regex pattern
                    Matcher matcher = pattern.matcher(line); //create a matcher for the current line

                    if (matcher.matches()) { //check if the line matches the regex pattern
                        // Parse the edge details
                        String source = matcher.group(1); //capture the source node name
                        String destination = matcher.group(2); //capture the destination node name
                        float weight = Float.parseFloat(matcher.group(3)); //convert weight to a float
                        addEdge(circles.get(source), circles.get(destination), weight); //add the edge to the graph
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
                e.printStackTrace(); //print stack trace for debugging
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
    }}

