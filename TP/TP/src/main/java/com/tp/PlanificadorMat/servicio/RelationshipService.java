package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.controllers.RelatedRelationshipDTO;
import com.tp.PlanificadorMat.repositorio.CourseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import com.tp.PlanificadorMat.modelo.Course;

/**
 * Servicio para gestionar relaciones RELATED entre materias
 */
@Service
public class RelationshipService {
    private final CourseRepository repo;

    public RelationshipService(CourseRepository repo) {
        this.repo = repo;
    }

    /**
     * Crear una relación RELATED entre dos materias
     */
    public Mono<Map<String, String>> createRelationship(RelatedRelationshipDTO dto) {
        // Validar que ambas materias existan
        return validateBothExist(dto.getFromCode(), dto.getToCode())
            .flatMap(valid -> {
                if (!valid) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Una o ambas materias no existen"
                    ));
                }
                // Validar que no sea la misma materia
                if (dto.getFromCode().equals(dto.getToCode())) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Una materia no puede tener relación RELATED consigo misma"
                    ));
                }
                
                double similarity = dto.getSimilarity() != null ? dto.getSimilarity() : 0.5;
                return repo.createRelatedRelationship(dto.getFromCode(), dto.getToCode(), similarity)
                    .then(Mono.just(createMap(
                        "message", "Relación creada exitosamente",
                        "from", dto.getFromCode(),
                        "to", dto.getToCode(),
                        "similarity", String.valueOf(similarity)
                    )));
            });
    }

    /**
     * Eliminar una relación RELATED entre dos materias
     */
    public Mono<Map<String, String>> deleteRelationship(String fromCode, String toCode) {
        return repo.deleteRelatedRelationship(fromCode, toCode)
            .flatMap(count -> {
                if (count == 0) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe relación RELATED entre " + fromCode + " y " + toCode
                    ));
                }
                return Mono.just(createMap(
                    "message", "Relación eliminada exitosamente",
                    "from", fromCode,
                    "to", toCode
                ));
            });
    }

    /**
     * Actualizar la similaridad de una relación existente
     */
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
                return Mono.just(createMap(
                    "message", "Similaridad actualizada exitosamente",
                    "from", fromCode,
                    "to", toCode,
                    "similarity", String.valueOf(similarity)
                ));
            });
    }

    /**
     * Obtener todas las relaciones RELATED de una materia
     */
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
                    .map(edge -> createObjectMap(
                        "relatedCourse", edge.getFrom(),
                        "similarity", edge.getSim()
                    ));
            });
    }

    /**
     * Obtener todas las relaciones RELATED del sistema
     */
    public Flux<Map<String, Object>> getAllRelationships() {
        return repo.relatedEdges()
            .map(edge -> createObjectMap(
                "from", edge.getFrom(),
                "to", edge.getTo(),
                "similarity", edge.getSim()
            ));
    }

    /**
     * Calcular similaridad automática entre dos materias basándose en atributos
     */
    public Mono<Map<String, String>> calculateAndCreateSimilarity(String fromCode, String toCode) {
        return Mono.zip(
            repo.findOneByCode(fromCode),
            repo.findOneByCode(toCode)
        ).flatMap(tuple -> {
            Course course1 = tuple.getT1();
            Course course2 = tuple.getT2();
            
            // Algoritmo simple de similaridad basado en créditos, horas y dificultad
            double similarity = calculateSimilarity(
                course1.getCredits(), course2.getCredits(),
                course1.getHours(), course2.getHours(),
                course1.getDifficulty(), course2.getDifficulty()
            );
            
            return repo.createRelatedRelationship(fromCode, toCode, similarity)
                .then(Mono.just(createMap(
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

    /**
     * Algoritmo de cálculo de similaridad
     * Compara créditos, horas y dificultad normalizados
     */
    private double calculateSimilarity(Integer c1, Integer c2, Integer h1, Integer h2, Integer d1, Integer d2) {
        // Valores por defecto
        int credits1 = c1 != null ? c1 : 4;
        int credits2 = c2 != null ? c2 : 4;
        int hours1 = h1 != null ? h1 : 6;
        int hours2 = h2 != null ? h2 : 6;
        int diff1 = d1 != null ? d1 : 3;
        int diff2 = d2 != null ? d2 : 3;
        
        // Distancias normalizadas
        double creditsDist = Math.abs(credits1 - credits2) / 12.0; // max 12 créditos
        double hoursDist = Math.abs(hours1 - hours2) / 40.0;       // max 40 horas
        double diffDist = Math.abs(diff1 - diff2) / 5.0;           // max 5 dificultad
        
        // Promedio de distancias
        double avgDist = (creditsDist + hoursDist + diffDist) / 3.0;
        
        // Convertir distancia a similaridad (1 = idénticos, 0 = muy diferentes)
        return Math.max(0.0, Math.min(1.0, 1.0 - avgDist));
    }

    /**
     * Validar que ambas materias existan
     */
    private Mono<Boolean> validateBothExist(String code1, String code2) {
        return Mono.zip(
            repo.existsByCode(code1),
            repo.existsByCode(code2)
        ).map(tuple -> tuple.getT1() && tuple.getT2());
    }

    // Utilidades para crear Maps sin usar Map.of()
    private Map<String, String> createMap(String... keyValuePairs) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                map.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }
        return map;
    }

    private Map<String, Object> createObjectMap(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private Map<String, Object> createObjectMap(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }
}

