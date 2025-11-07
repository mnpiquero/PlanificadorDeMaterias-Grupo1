package com.tp.PlanificadorMat.controllers;

/**
 * DTO para crear/actualizar relaciones RELATED entre materias
 */
public class RelatedRelationshipDTO {
    private String fromCode;
    private String toCode;
    private Double similarity;

    public RelatedRelationshipDTO() {}

    public RelatedRelationshipDTO(String fromCode, String toCode, Double similarity) {
        if (similarity != null && (similarity < 0.0 || similarity > 1.0)) {
            throw new IllegalArgumentException("Similarity debe estar entre 0.0 y 1.0");
        }
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.similarity = similarity;
    }

    public String getFromCode() { return fromCode; }
    public void setFromCode(String fromCode) { this.fromCode = fromCode; }
    public String getToCode() { return toCode; }
    public void setToCode(String toCode) { this.toCode = toCode; }
    public Double getSimilarity() { return similarity; }
    public void setSimilarity(Double similarity) { 
        if (similarity != null && (similarity < 0.0 || similarity > 1.0)) {
            throw new IllegalArgumentException("Similarity debe estar entre 0.0 y 1.0");
        }
        this.similarity = similarity; 
    }
}

