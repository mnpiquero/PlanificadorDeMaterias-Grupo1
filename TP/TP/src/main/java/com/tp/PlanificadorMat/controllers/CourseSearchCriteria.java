package com.tp.PlanificadorMat.controllers;

/**
 * Criterios de búsqueda para materias
 */
public class CourseSearchCriteria {
    private String nameContains;
    private Integer minCredits;
    private Integer maxCredits;
    private Integer minDifficulty;
    private Integer maxDifficulty;
    private Integer minHours;
    private Integer maxHours;

    // Getters y Setters
    public String getNameContains() { return nameContains; }
    public void setNameContains(String nameContains) { this.nameContains = nameContains; }
    
    public Integer getMinCredits() { return minCredits; }
    public void setMinCredits(Integer minCredits) { this.minCredits = minCredits; }
    
    public Integer getMaxCredits() { return maxCredits; }
    public void setMaxCredits(Integer maxCredits) { this.maxCredits = maxCredits; }
    
    public Integer getMinDifficulty() { return minDifficulty; }
    public void setMinDifficulty(Integer minDifficulty) { this.minDifficulty = minDifficulty; }
    
    public Integer getMaxDifficulty() { return maxDifficulty; }
    public void setMaxDifficulty(Integer maxDifficulty) { this.maxDifficulty = maxDifficulty; }
    
    public Integer getMinHours() { return minHours; }
    public void setMinHours(Integer minHours) { this.minHours = minHours; }
    
    public Integer getMaxHours() { return maxHours; }
    public void setMaxHours(Integer maxHours) { this.maxHours = maxHours; }
}

