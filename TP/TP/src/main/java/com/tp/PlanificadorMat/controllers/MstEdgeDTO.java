package com.tp.PlanificadorMat.controllers;

public class MstEdgeDTO {
    private String from;
    private String to;
    private double weight;

    public MstEdgeDTO() {}

    public MstEdgeDTO(String from, String to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
