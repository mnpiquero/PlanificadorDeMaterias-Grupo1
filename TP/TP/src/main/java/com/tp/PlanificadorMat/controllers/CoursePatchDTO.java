package com.tp.PlanificadorMat.controllers;

/**
 * DTO para actualizaciones parciales de Course
 * Todos los campos son opcionales
 */
public class CoursePatchDTO {
    private String name;
    private Integer credits;
    private Integer hours;
    private Integer difficulty;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    
    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
    
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
}

