package com.tp.PlanificadorMat.controllers;

import com.tp.PlanificadorMat.servicio.RelationshipService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller para gestionar relaciones RELATED entre materias
 */
@RestController
@RequestMapping("/relationships")
public class RelationshipController {
    
    private final RelationshipService service;

    public RelationshipController(RelationshipService service) {
        this.service = service;
    }

    /**
     * Crear relación RELATED entre dos materias
     * POST /relationships
     * Body: {"fromCode": "MAT101", "toCode": "FIS101", "similarity": 0.8}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> createRelationship(@RequestBody RelatedRelationshipDTO dto) {
        return service.createRelationship(dto);
    }

    /**
     * Eliminar relación RELATED entre dos materias
     * DELETE /relationships/{fromCode}/{toCode}
     */
    @DeleteMapping("/{fromCode}/{toCode}")
    public Mono<Map<String, String>> deleteRelationship(
            @PathVariable String fromCode,
            @PathVariable String toCode) {
        return service.deleteRelationship(fromCode, toCode);
    }

    /**
     * Actualizar similaridad de una relación existente
     * PATCH /relationships/{fromCode}/{toCode}
     * Body: {"similarity": 0.9}
     */
    @PatchMapping("/{fromCode}/{toCode}")
    public Mono<Map<String, String>> updateSimilarity(
            @PathVariable String fromCode,
            @PathVariable String toCode,
            @RequestBody Map<String, Double> body) {
        Double similarity = body.get("similarity");
        return service.updateSimilarity(fromCode, toCode, similarity);
    }

    /**
     * Obtener todas las materias relacionadas con una materia específica
     * GET /relationships/{code}
     */
    @GetMapping("/{code}")
    public Flux<Map<String, Object>> getRelatedCourses(@PathVariable String code) {
        return service.getRelatedCourses(code);
    }

    /**
     * Listar todas las relaciones RELATED del sistema
     * GET /relationships
     */
    @GetMapping
    public Flux<Map<String, Object>> getAllRelationships() {
        return service.getAllRelationships();
    }

    /**
     * Crear relación con similaridad calculada automáticamente
     * POST /relationships/auto
     * Body: {"fromCode": "MAT101", "toCode": "FIS101"}
     */
    @PostMapping("/auto")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> createWithAutoSimilarity(@RequestBody RelatedRelationshipDTO dto) {
        return service.calculateAndCreateSimilarity(dto.fromCode(), dto.toCode());
    }
}

