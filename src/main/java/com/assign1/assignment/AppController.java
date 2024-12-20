package com.assign1.assignment;

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

import java.util.*;

public class AppController {

    Boolean addingNode = false;
    Boolean removingNode = false;
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
            double zoomFactor = event.getDeltaY() > 0 ? 1.01 : 0.99; // Zoom in or out
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
    public void menuAddNodeHandler(ActionEvent e){
        this.addingNode = true;
        this.removingNode=false;

    }
    public void menuRemoveNodeHandler(ActionEvent e){
        this.removingNode = true;
        this.addingNode=false;

    }
    public void menuClearHandler(){
        this.circles.clear();
        this.adjList.clear();
        this.nodeGroup.getChildren().clear();
    }

    public void removeNode(NodeFX node){
        this.nodeGroup.getChildren().remove(node);  //removing the node, the label of the node from the actual group
        this.nodeGroup.getChildren().remove(node.nodeLabel);
        this.circles.remove(node.getName()); //removing node from circles

        for (NodeFX user : adjList.get(node).keySet()){ //removing from UI
            nodeGroup.getChildren().remove(adjList.get(node).get(user));
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
                nodeGroup.getChildren().addAll(temp, temp.nodeLabel); // Add both Circle and Label to the Group
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
         protected  Set<NodeFX> adjacent = new HashSet<>();

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
            nodeLabel.setLayoutX(x-3);
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
                System.out.println(temp.getName()+" ");
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

        Set<NodeFX> getAdjacent(){
            return adjacent;
        }
    }

    public class Edge extends Line {
        protected NodeFX source;
        protected NodeFX destination;
        protected Float weight;
        protected Label weightLabel = new Label();
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

        Boolean contains(NodeFX node){
            return this.destination.equals(node) || this.source.equals(node);
        }
    }




}