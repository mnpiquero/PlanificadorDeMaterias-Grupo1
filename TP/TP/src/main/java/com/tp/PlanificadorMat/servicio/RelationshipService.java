package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.controllers.RelatedRelationshipDTO;
import com.tp.PlanificadorMat.repositorio.CourseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Servicio para gestionar relaciones RELATED entre materias
 */
@Service
public class RelationshipService {

    private final CourseRepository repo;

    public RelationshipService(CourseRepository repo) {
        this.repo = repo;
    }

    /** Crear una relación RELATED entre dos materias */
    public Mono<Map<String, String>> createRelationship(RelatedRelationshipDTO dto) {
        return validateBothExist(dto.fromCode(), dto.toCode())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Una o ambas materias no existen"
                        ));
                    }
                    if (dto.fromCode().equals(dto.toCode())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Una materia no puede tener relación RELATED consigo misma"
                        ));
                    }

                    double similarity = dto.similarity() != null ? dto.similarity() : 0.5;
                    return repo.createRelatedRelationship(dto.fromCode(), dto.toCode(), similarity)
                            .then(Mono.just(Map.of(
                                    "message", "Relación creada exitosamente",
                                    "from", dto.fromCode(),
                                    "to", dto.toCode(),
                                    "similarity", String.valueOf(similarity)
                            )));
                });
    }

    /** Eliminar una relación RELATED entre dos materias */
    public Mono<Map<String, String>> deleteRelationship(String fromCode, String toCode) {
        return repo.deleteRelatedRelationship(fromCode, toCode)
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe relación RELATED entre " + fromCode + " y " + toCode
                        ));
                    }
                    return Mono.just(Map.of(
                            "message", "Relación eliminada exitosamente",
                            "from", fromCode,
                            "to", toCode
                    ));
                });
    }

    /** Actualizar la similaridad de una relación existente */
    public Mono<Map<String, String>> updateSimilarity(String fromCode, String toCode, Double similarity) {
        if (similarity == null || similarity < 0.0 || similarity > 1.0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La similaridad debe estar entre 0.0 y 1.0"
            ));
        }

        return repo.updateRelatedSimilarity(fromCode, toCode, similarity)
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe relación RELATED entre " + fromCode + " y " + toCode
                        ));
                    }
                    return Mono.just(Map.of(
                            "message", "Similaridad actualizada exitosamente",
                            "from", fromCode,
                            "to", toCode,
                            "similarity", String.valueOf(similarity)
                    ));
                });
    }

    /** Obtener todas las relaciones RELATED de una materia */
    public Flux<Map<String, Object>> getRelatedCourses(String code) {
        return repo.existsByCode(code)
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Materia no encontrada: " + code
                        ));
                    }
                    return repo.getRelatedCourses(code)
                            .map(edge -> Map.<String, Object>of(
                                    "relatedCourse", edge.from(),   // << accessors de record
                                    "similarity", edge.sim()        // << accessors de record
                            ));
                });
    }

    /** Obtener todas las relaciones RELATED del sistema */
    public Flux<Map<String, Object>> getAllRelationships() {
        return repo.relatedEdges()
                .map(edge -> Map.<String, Object>of(
                        "from", edge.from(),           // << accessors de record
                        "to", edge.to(),               // << accessors de record
                        "similarity", edge.sim()       // << accessors de record
                ));
    }

    /** Calcular similaridad automática entre dos materias basándose en atributos */
    public Mono<Map<String, String>> calculateAndCreateSimilarity(String fromCode, String toCode) {
        return Mono.zip(
                repo.findOneByCode(fromCode),
                repo.findOneByCode(toCode)
        ).flatMap(tuple -> {
            var course1 = tuple.getT1();
            var course2 = tuple.getT2();

            double similarity = calculateSimilarity(
                    course1.getCredits(), course2.getCredits(),
                    course1.getHours(), course2.getHours(),
                    course1.getDifficulty(), course2.getDifficulty()
            );

            return repo.createRelatedRelationship(fromCode, toCode, similarity)
                    .then(Mono.just(Map.of(
                            "message", "Relación creada con similaridad calculada automáticamente",
                            "from", fromCode,
                            "to", toCode,
                            "similarity", String.format("%.2f", similarity)
                    )));
        }).switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Una o ambas materias no existen"
        )));
    }

    /** Algoritmo simple de cálculo de similaridad */
    private double calculateSimilarity(Integer c1, Integer c2, Integer h1, Integer h2, Integer d1, Integer d2) {
        int credits1 = c1 != null ? c1 : 4;
        int credits2 = c2 != null ? c2 : 4;
        int hours1   = h1 != null ? h1 : 6;
        int hours2   = h2 != null ? h2 : 6;
        int diff1    = d1 != null ? d1 : 3;
        int diff2    = d2 != null ? d2 : 3;

        double creditsDist = Math.abs(credits1 - credits2) / 12.0;
        double hoursDist   = Math.abs(hours1 - hours2)     / 40.0;
        double diffDist    = Math.abs(diff1 - diff2)       / 5.0;

        double avgDist = (creditsDist + hoursDist + diffDist) / 3.0;
        return Math.max(0.0, Math.min(1.0, 1.0 - avgDist));
    }

    /** Validar que ambas materias existan */
    private Mono<Boolean> validateBothExist(String code1, String code2) {
        return Mono.zip(
                repo.existsByCode(code1),
                repo.existsByCode(code2)
        ).map(tuple -> tuple.getT1() && tuple.getT2());
    }
}
