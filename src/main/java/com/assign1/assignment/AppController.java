package com.assign1.assignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.*;

public class AppController {
    Boolean addingNode = false;
    Boolean removingNode = false;
    ArrayList<NodeFX> circles = new ArrayList<>();
//    Set<Node> users = new HashSet<>();
    ArrayList<Edge> edges = new ArrayList<>();
    @FXML
    AnchorPane graphArea = new AnchorPane();
    @FXML
    Group nodeGroup = new Group();
    @FXML
    MenuBar menuBar = new MenuBar();
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
        this.edges.clear();
        this.nodeGroup.getChildren().clear();
    }

    public void removeNode(NodeFX node){
        this.nodeGroup.getChildren().remove(node);  //removing the node, the label of the node from the actual group
        this.nodeGroup.getChildren().remove(node.nodeLabel);
        this.circles.remove(node); //removing node from circles
        Iterator<Edge> edgeIterator = edges.iterator();
        while (edgeIterator.hasNext()){ //iterating through every edge, if edge contains the node that were deleting, delete the edge
            Edge edge = edgeIterator.next();
            if (edge.source.equals(node) || edge.destination.equals(node)){
                nodeGroup.getChildren().remove(edge);
                nodeGroup.getChildren().remove(edge.weightLabel);
                edgeIterator.remove();
            }
        }

        Iterator<NodeFX> nodeFXIterator = circles.iterator(); //iterating through all circles/ users

        while(nodeFXIterator.hasNext()){
            NodeFX temp = nodeFXIterator.next(); //iterating through every users adjacency list, passing reference as "temp" to the deleteAdjacencyReference method
            deleteAdjacencyReference(temp, node);
        }

        for (NodeFX x : circles){ //finish cleaning up by updating the appcontroller arraylist as well
            deleteAdjacencyReference(x,node);
        }

        this.removingNode = false;
    }

    public void addNode(MouseEvent e){
        TextInputDialog popup = new TextInputDialog();
        popup.setHeaderText("User Creation");
        popup.setContentText("Enter new username");
        popup.setTitle("Input");
        popup.showAndWait().ifPresent(input -> {
            if (!nodeExists(input)){
                NodeFX temp = new NodeFX(e.getX(), e.getY(), 27, input); // Use input from popup
                circles.add(temp);
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
        for (NodeFX node : circles) {
            if (node.name.equals(username)) {
                return true; // Found a node with the given username
            }
        }
        return false; // No node with the given username
    }

    public void deleteAdjacencyReference(NodeFX targetNode, NodeFX nodeToRemove){
        Iterator<NodeFX> nodeFXIterator = targetNode.adjacent.iterator(); //iterate through adjlist of the targetnode, searching from nodetoremove
        while (nodeFXIterator.hasNext()){//since we passed a reference, deleting the node here actually affects the actual users' objects on top level
            NodeFX adjacentNode = nodeFXIterator.next();
            if (adjacentNode.equals(nodeToRemove)){
                nodeFXIterator.remove();
                return;
            }
        }
    }

    class NodeFX extends Circle{
        String name;
        Label nodeLabel = new Label();
        Set<NodeFX> adjacent = new HashSet<>();

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
            nodeLabel.setLayoutX(x-9);
            nodeLabel.setLayoutY(y+35);
            this.setId(name);

            this.setOnMouseClicked(event -> {
                handleNodeClick(event);
                event.consume();
            });

            this.setOnDragDetected(mouseEvent -> {
                this.startFullDrag();
            });r

            this.setOnMouseDragReleased(mouseEvent -> {
                if (mouseEvent.getGestureSource() instanceof NodeFX && mouseEvent.getSource() instanceof NodeFX) {
                    TextInputDialog popup = new TextInputDialog("Weight");
                    popup.setHeaderText("Please enter Connection Weight");
                    popup.showAndWait().ifPresent(input -> {
                        if (Float.parseFloat(input)>=0.01F && Float.parseFloat(input)<=1){
                            NodeFX node1 = (NodeFX) mouseEvent.getGestureSource();
                            NodeFX node2 = (NodeFX) mouseEvent.getSource();
                            Edge temp = new Edge(node1, node2,  Float.parseFloat(input));
                            edges.add(temp);
                            nodeGroup.getChildren().add(temp);
                            nodeGroup.getChildren().add(temp.weightLabel);
                            ((NodeFX) mouseEvent.getSource()).adjacent.add(node1);
                            ((NodeFX) mouseEvent.getGestureSource()).adjacent.add(node2);
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
                System.out.println(temp.name+" ");
            }
        });
        }

    }

    public class Edge extends Line {
        protected NodeFX source;
        protected NodeFX destination;
        protected Float weight;
        protected Label weightLabel = new Label();
        Edge(AppController.NodeFX source, AppController.NodeFX destination, Float weight){
            this.source=source;
            this.destination=destination;
            this.weight=weight;

            weightLabel.setText(weight.toString());
            weightLabel.setLayoutX((destination.getCenterX()+source.getCenterX())/2);
            weightLabel.setLayoutY((destination.getCenterY()+source.getCenterY())/2+10);
            weightLabel.setTextFill(Paint.valueOf("Black"));

            this.setId(String.format("%s-%s",source.getId(),destination.getId()));
            this.setStartX(source.getCenterX());
            this.setStartY(source.getCenterY());
            this.setEndX(destination.getCenterX());
            this.setEndY(destination.getCenterY());
        }
    }


}