package com.tp.PlanificadorMat.servicio;

public class Edge {
    private String u;
    private String v;
    private double w;

    public Edge() {}

    public Edge(String u, String v, double w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public String getU() { return u; }
    public void setU(String u) { this.u = u; }
    public String getV() { return v; }
    public void setV(String v) { this.v = v; }
    public double getW() { return w; }
    public void setW(double w) { this.w = w; }
}

