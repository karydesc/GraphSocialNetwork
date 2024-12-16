package com.assign1.assignment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AppController {
    Boolean addingNode = false;
    Boolean removingNode = false;
    boolean addingEdge = false;
    Set<NodeFX> circles = new HashSet<>();
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
        }
        System.out.println("I am also clicked");

    }
    @FXML
    public void handleNodeClick(MouseEvent e){
        if(removingNode){
            
        }
    }


    public void menuAddNodeHandler(ActionEvent e){
        this.addingNode = true;
    }


    public void addNode(MouseEvent e){
        TextInputDialog popup = new TextInputDialog();
        popup.setHeaderText("Technology is so cool.");
        popup.setContentText("Enter new username");
        popup.setTitle("New user");
        popup.showAndWait().ifPresent(input -> {
            NodeFX temp = new NodeFX(e.getX(), e.getY(), 35, input); // Use input from popup
            temp.point = new Point((int)e.getX(), (int)e.getY());
            circles.add(temp);
            nodeGroup.getChildren().addAll(temp, temp.nodeLabel); // Add both Circle and Label to the Group
        });
        addingNode=false;
    }


    class NodeFX extends Circle{
        Point point;
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
            super(x,y,rad,Paint.valueOf("White"));
            this.setStroke(Paint.valueOf("black"));
            this.name = name;
            this.nodeLabel.setText(name);
            this.nodeLabel.setTextFill(Paint.valueOf("black"));
            nodeLabel.setLayoutX(x-9);
            nodeLabel.setLayoutY(y-10);
            this.setId(name);
            this.setOnMouseClicked(event -> {
                handleNodeClick(event);
                event.consume();
            });

        }

    }

}