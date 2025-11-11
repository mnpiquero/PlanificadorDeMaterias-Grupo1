package com.tp.PlanificadorMat.controllers;

/**
 * DTO para crear/actualizar relaciones RELATED entre materias
 * @param fromCode código de la materia origen
 * @param toCode código de la materia destino
 * @param similarity valor de similaridad (0.0 a 1.0)
 */
public record RelatedRelationshipDTO(String fromCode, String toCode, Double similarity) {
    
    public RelatedRelationshipDTO {
        if (similarity != null && (similarity < 0.0 || similarity > 1.0)) {
            throw new IllegalArgumentException("Similarity debe estar entre 0.0 y 1.0");
        }
    }
}

