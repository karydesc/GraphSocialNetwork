package com.assign1.assignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Popup;

import java.awt.*;
import java.util.*;

public class AppController {
    Boolean addingNode = false;
    Boolean removingNode = false;
    ArrayList<NodeFX> circles = new ArrayList<>();
//    Set<Node> users = new HashSet<>();
    Set<Edge> edges = new HashSet<>();
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
    public void menuAddEdgeHandler(ActionEvent e){
        this.removingNode = false;
        this.addingNode=false;
    }
    public void menuClearHandler(){
        this.circles.clear();
        this.edges.clear();
        this.nodeGroup.getChildren().clear();
    }

    public void removeNode(NodeFX node){
        this.nodeGroup.getChildren().remove(node);
        this.nodeGroup.getChildren().remove(node.nodeLabel);
        this.circles.remove(node);
        this.removingNode = false;
    }

    public void addNode(MouseEvent e){
        TextInputDialog popup = new TextInputDialog();
        popup.setHeaderText("Technology is so cool.");
        popup.setContentText("Enter new username");
        popup.setTitle("New user");
        popup.showAndWait().ifPresent(input -> {
            NodeFX temp = new NodeFX(e.getX(), e.getY(), 35, input); // Use input from popup
            circles.add(temp);
            nodeGroup.getChildren().addAll(temp, temp.nodeLabel); // Add both Circle and Label to the Group
        });
        addingNode=false;
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
                System.out.println("Starting Drag");
                this.startFullDrag();
            });

            this.setOnMouseDragReleased(mouseEvent -> {
                if (mouseEvent.getGestureSource() instanceof NodeFX && mouseEvent.getSource() instanceof NodeFX) {
                    TextInputDialog popup = new TextInputDialog("Weight");
                    popup.setHeaderText("Please enter Connection Weight");
                    popup.showAndWait().ifPresent(input -> {
                        NodeFX node1 = (NodeFX) mouseEvent.getGestureSource();
                        NodeFX node2 = (NodeFX) mouseEvent.getSource();
                        Edge temp = new Edge(node1, node2,  Float.parseFloat(input));
                        edges.add(temp);
                        nodeGroup.getChildren().add(temp);
                        nodeGroup.getChildren().add(temp.weightLabel);
                        ((NodeFX) mouseEvent.getSource()).adjacent.add(node1);
                        ((NodeFX) mouseEvent.getGestureSource()).adjacent.add(node2);


                    });
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