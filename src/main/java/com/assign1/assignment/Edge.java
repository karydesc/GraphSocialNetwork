package com.assign1.assignment;

public class Edge{
    protected AppController.NodeFX source;
    protected AppController.NodeFX destination;
    protected Float weight;

        Edge(AppController.NodeFX source, AppController.NodeFX destination, Float weight){
        this.source=source;
        this.destination=destination;
        this.weight=weight;
    }
}
