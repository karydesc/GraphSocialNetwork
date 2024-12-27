package com.assign1.assignment;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.random.RandomGenerator;

public class AppController {
    Scanner scanner = new Scanner(System.in);
    protected Boolean addingNode = false;
    protected Boolean removingNode = false;
    Map<String, NodeFX> circles = new HashMap<>();
    Map<NodeFX, Map<NodeFX, Edge>> adjList = new HashMap<>();
    @FXML
    AnchorPane graphArea;
    @FXML
    Group nodeGroup;
    @FXML
    MenuBar menuBar;
    @FXML
    ScrollPane scrollPane;


    private Scale scale = new Scale(1, 1, 0, 0); // Default scaling factors

    @FXML
    public void initialize() {
        // Add the scale transformation to the nodeGroup
        nodeGroup.getTransforms().add(scale);

        // Add scroll event listener to the ScrollPane
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) { // Only zoom when CTRL is held down
            double zoomFactor = event.getDeltaY() > 0 ? 0.99 : 1.01; // Zoom in or out
            scale.setX(scale.getX() * zoomFactor);
            scale.setY(scale.getY() * zoomFactor);
            event.consume();
        }}


    @FXML
    public void CanvasClickHandler(MouseEvent e) {
        if (addingNode){
            addNode(e);
        } else if (removingNode) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "Click on a node, not the background silly!");
            warning.show();
            removingNode=false;
        }
    }
    @FXML
    public void handleNodeClick(MouseEvent e){
        if(removingNode){
            removeNode((NodeFX) e.getTarget());
        }
    }
    @FXML
    public void menuAddNodeHandler(ActionEvent e){
        this.addingNode = true;
        this.removingNode=false;

    }
    @FXML
    public void menuRemoveNodeHandler(ActionEvent e){
        this.removingNode = true;
        this.addingNode=false;

    }
    @FXML
    public void menuClearHandler(){
        this.circles.clear();
        this.adjList.clear();
        this.nodeGroup.getChildren().clear();
    }
    @FXML
    public void addConnectionMenuHandler(){
        System.out.println("Enter user 1: ");
        String user1 = scanner.nextLine();
        System.out.println("\nEnter user 2: ");
        String user2 = scanner.nextLine();
        NodeFX node1 = circles.get(user1);
        NodeFX node2 = circles.get(user2);
        System.out.println("\nEnter Weights: ");
        Float weight = scanner.nextFloat();
        if (weight<0.01||weight>1||!nodeExists(user1)||!nodeExists(user2)){
            System.out.println("\nno");
            return;
        }
        Edge temp = new Edge(node1, node2,  weight);

        adjList.get(node1).put(node2, temp);
        adjList.get(node2).put(node1, temp);

        node1.adjacent.add(node2);
        node2.adjacent.add(node1);

        nodeGroup.getChildren().add(temp);
        nodeGroup.getChildren().add(temp.weightLabel);


    }
    @FXML
    public void dijkstraHandler() throws IOException {
//        Map<NodeFX, Float> dijkstraTable = new HashMap<>();
        AtomicReference<NodeFX> source = new AtomicReference<>();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Dijkstra Algorithm");
        dialog.setContentText("Enter start node:");
        dialog.showAndWait().ifPresent(start -> {
            source.set(circles.get(start));
            if (source.get() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid start node!");
                alert.show();
                return;
            }
        });
        for (String x : circles.keySet()){
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


    }

    public void removeNode(NodeFX node){
        this.nodeGroup.getChildren().remove(node);  //removing the node, the label of the node from the actual group
        this.nodeGroup.getChildren().remove(node.nodeLabel);
        this.nodeGroup.getChildren().remove(node.dstToSource);
        this.circles.remove(node.getName()); //removing node from circles

        for (NodeFX user : adjList.get(node).keySet()){ //removing from UI
            nodeGroup.getChildren().remove(adjList.get(node).get(user)); //fetching edge objects from adjlist
            nodeGroup.getChildren().remove(adjList.get(node).get(user).weightLabel);
            adjList.get(user).remove(node);
            user.adjacent.remove(node); //update local adjacency list of the actual circles in the GUI

        }
        adjList.remove(node);
        this.removingNode = false;
    }

    public void addNode(MouseEvent e){
        TextInputDialog popup = new TextInputDialog();
        popup.setHeaderText("User Creation");
        popup.setContentText("Enter new username");
        popup.setTitle("Input");
        popup.showAndWait().ifPresent(input -> {
            if (!nodeExists(input)){
                NodeFX temp = new NodeFX(e.getX(), e.getY(), 15, input); // Use input from popup
                adjList.put(temp, new HashMap<>());
                circles.put(input, temp);
                nodeGroup.getChildren().addAll(temp, temp.nodeLabel, temp.dstToSource); // Add both Circle and Label to the Group
            }else{
                Alert denied = new Alert(Alert.AlertType.ERROR);
                denied.setHeaderText("User already exists!");
                denied.setTitle("Ya silly goose...");
                denied.show();
            }

        });
        addingNode=false;
    }

    public boolean nodeExists(String username) {
        return circles.containsKey(username);
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
            if (obj instanceof NodeFX){
                NodeFX u = (NodeFX) obj;
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

            this.setOnDragDetected(mouseEvent -> {
                this.startFullDrag();
            });

            this.setOnMouseDragReleased(mouseEvent -> {
                if (mouseEvent.getGestureSource() instanceof NodeFX && mouseEvent.getSource() instanceof NodeFX) {
                    TextInputDialog popup = new TextInputDialog("Weight");
                    popup.setHeaderText("Please enter Connection Weight");
                    popup.showAndWait().ifPresent(input -> {
                        if (Float.parseFloat(input)>=0.01F && Float.parseFloat(input)<=1){
                            NodeFX node1 = (NodeFX) mouseEvent.getGestureSource();
                            NodeFX node2 = (NodeFX) mouseEvent.getSource();
                            Edge temp = new Edge(node1, node2,  Float.parseFloat(input));

                            adjList.get(node1).put(node2, temp);
                            adjList.get(node2).put(node1, temp);

                            node1.adjacent.add(node2);
                            node2.adjacent.add(node1);

                            nodeGroup.getChildren().add(temp);
                            nodeGroup.getChildren().add(temp.weightLabel);
                        }else{
                            Alert warning = new Alert(Alert.AlertType.ERROR);
                            warning.setTitle("Cannot Comply.");
                            warning.setHeaderText("Use value 0.1 <-> 1.0");
                            warning.show();
                        }
                    });
                }
            });
            this.setOnContextMenuRequested(event -> {
            System.out.println("\n\nYou right-clicked: "+this.name + "\nThey are adjacent to: ");
            for (NodeFX temp : this.adjacent){
                System.out.println(temp.getName()+" "+adjList.get(this).get(temp).getWeight()+" ");//get edge weight
            }

            event.consume();
        });
        }

        Label getLabel(){
            return this.nodeLabel;
        }
        String getName(){
            return name;
        }

        ArrayList<NodeFX> getAdjacent(){
            return adjacent;
        }
        Boolean isActive(){
            return isActive;
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

    public class Edge extends Line {
        private boolean isActive;
        protected NodeFX source;
        protected NodeFX destination;
        protected Float weight;
        protected Label weightLabel = new Label();
        private Boolean isVisited;

        Float getWeight(){
            return weight;
        }
        Edge(AppController.NodeFX source, AppController.NodeFX destination, Float weight){
            super(source.getCenterX(), source.getCenterY(), destination.getCenterX(), destination.getCenterY());
            this.source=source;
            this.destination=destination;
            this.weight=weight;

            weightLabel.setText(weight.toString());
            weightLabel.setLayoutX((destination.getCenterX()+source.getCenterX())/2);
            weightLabel.setLayoutY((destination.getCenterY()+source.getCenterY())/2+10);
            weightLabel.setTextFill(Paint.valueOf("Black"));

            this.setId(String.format("%s-%s",source.getId(),destination.getId()));
            this.weightLabel.setOnMouseClicked(mouseEvent -> {
                TextInputDialog popup = new TextInputDialog("Weight");
                popup.setHeaderText("Please enter Connection Weight");
                popup.showAndWait().ifPresent(input -> {
                    if (Float.parseFloat(input)>=0.01F && Float.parseFloat(input)<=1){
                        this.weight=Float.parseFloat(input);
                        this.weightLabel.setText(input);
                    }else{
                        Alert warning = new Alert(Alert.AlertType.ERROR);
                        warning.setTitle("Cannot Comply.");
                        warning.setHeaderText("Use value 0.1 <-> 1.0");
                        warning.show();
                    }
                });
            });
        }

        void setActive(Boolean b) {
            if(b){
                this.setStroke(Paint.valueOf("green"));
                this.setStrokeWidth(1.5);
            }else{
                this.setStroke(Paint.valueOf("black"));
                this.setStrokeWidth(1);
            }
            this.isActive=b;
            this.isVisited=!b;
        }
        void setVisited(Boolean b) {
            if(b){
                this.setStroke(Paint.valueOf("gray"));
                this.setStrokeWidth(1.5);
            }else{
                this.setStroke(Paint.valueOf("black"));
                this.setStrokeWidth(1);
            }
            this.isActive=!b;
            this.isVisited=b;
        }
        Boolean isActive(){
            return this.isActive;
        }



        Boolean contains(NodeFX node){
            return this.destination.equals(node) || this.source.equals(node);
        }
    }

    void runDijkstra(NodeFX source){
        for (String x : circles.keySet()){
            circles.get(x).isActive=false;
            circles.get(x).isVisited=false;
            circles.get(x).minDistance=Float.MAX_VALUE;
            Platform.runLater(()->circles.get(x).reset());
        }
        Platform.runLater(() -> {
            source.setMinDistance(0F);
        });
        source.minDistance=0F;

        PriorityQueue<NodeFX> priorityQueue=new PriorityQueue<NodeFX>(10, new Comparator<NodeFX>() {
            public int compare(NodeFX node1, NodeFX node2) {
                if (node1.minDistance > node2.minDistance) return 1;
                if (node1.minDistance < node2.minDistance) return -1;
                return 0;
            }
        });
        priorityQueue.add(source);



        while (!priorityQueue.isEmpty()) {
            NodeFX current = priorityQueue.poll();
            Platform.runLater(() -> current.setActive(true));

            for (NodeFX node : current.getAdjacent()){
                Float newWeight = current.minDistance + adjList.get(current).get(node).getWeight();
                if (newWeight<node.minDistance){
                    priorityQueue.remove(node);
                    node.minDistance=newWeight;
                    node.previous=current;
                    Platform.runLater(() -> node.setMinDistance(newWeight));
                    priorityQueue.add(node);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            };

            Platform.runLater(() -> current.setVisited(true));


        }
        for (NodeFX user : circles.values()) {
            if (user.equals(source)) continue;

            System.out.print("\nShortest path to source to " + user.getName() + ": ");

            if (user.minDistance == Float.MAX_VALUE) {
                // If the distance is still infinity, the node is unreachable
                System.out.println("No path found.");
                continue;
            }

            // Use a stack to reconstruct the path
            Stack<String> pathStack = new Stack<>();
            NodeFX currIter = user;

            while (currIter != null) {
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

}