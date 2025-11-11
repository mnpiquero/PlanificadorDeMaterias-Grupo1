package com.tp.PlanificadorMat.repositorio;

import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface CourseRepository extends ReactiveNeo4jRepository<Course, String> {

    Mono<Course> findOneByCode(String code);

    /** Todos los cursos (útil para construir el grafo en memoria) */
    @Query("""
      MATCH (c:Course)
      RETURN c
    """)
    Flux<Course> allCourses();

    /** Hidrata la colección 'prereqs' del entity Course */
    @Query("""
      MATCH (c:Course)
      OPTIONAL MATCH (c)-[:REQUIRES]->(p:Course)
      RETURN c, collect(p) AS prereqs
    """)
    Flux<Course> allCoursesWithPrereqs();

    /** Prerrequisitos (REQUIRES) de un curso dado */
    @Query("""
      MATCH (c:Course {code:$code})-[:REQUIRES]->(p:Course)
      RETURN p
    """)
    Flux<Course> prereqsOf(String code);

    /** Materias cursables cuando TODOS sus prereqs ∈ $approved (y aún no están aprobadas) */
    @Query("""
      MATCH (c:Course)
      WHERE ALL (p IN [(c)-[:REQUIRES]->(x) | x.code] WHERE p IN $approved)
        AND NOT c.code IN $approved
      RETURN c
    """)
    Flux<Course> availableWith(List<String> approved);

    /** ¿Existe algún ciclo en el grafo dirigido de correlativas? */
    @Query("""
      MATCH p=(a:Course)-[:REQUIRES*]->(a)
      RETURN p LIMIT 1
    """)
    Flux<Object> anyCycle();

    /** Subgrafo no dirigido RELATED para MST (Prim/Kruskal) -> DTO/record explícito */
    @Query("""
      MATCH (a:Course)-[r:RELATED]-(b:Course)
      WHERE a <> b
      RETURN a.code AS from, b.code AS to, COALESCE(r.sim, 0.0) AS sim
    """)
    Flux<RelatedEdgeRow> relatedEdges();

    /** Obtener RELATED de un curso (mismo DTO) */
    @Query("""
      MATCH (a:Course {code:$code})-[r:RELATED]-(b:Course)
      RETURN a.code AS from, b.code AS to, COALESCE(r.sim, 0.0) AS sim
    """)
    Flux<RelatedEdgeRow> getRelatedCourses(String code);

    /** Búsqueda por nombre (case insensitive) */
    @Query("""
      MATCH (c:Course)
      WHERE toLower(c.name) CONTAINS toLower($namePattern)
      RETURN c
    """)
    Flux<Course> findByNameContaining(String namePattern);

    /** Búsqueda avanzada con filtros opcionales */
    @Query("""
      MATCH (c:Course)
      WHERE ($minCredits IS NULL OR c.credits >= $minCredits)
        AND ($maxCredits IS NULL OR c.credits <= $maxCredits)
        AND ($minDifficulty IS NULL OR c.difficulty >= $minDifficulty)
        AND ($maxDifficulty IS NULL OR c.difficulty <= $maxDifficulty)
        AND ($minHours IS NULL OR c.hours >= $minHours)
        AND ($maxHours IS NULL OR c.hours <= $maxHours)
        AND ($namePattern IS NULL OR toLower(c.name) CONTAINS toLower($namePattern))
      RETURN c
    """)
    Flux<Course> searchCourses(
            String namePattern,
            Integer minCredits, Integer maxCredits,
            Integer minDifficulty, Integer maxDifficulty,
            Integer minHours, Integer maxHours
    );

    /** Eliminar curso por código */
    @Query("""
      MATCH (c:Course {code:$code})
      DETACH DELETE c
    """)
    Mono<Void> deleteByCode(String code);

    /** Verificar existencia por código */
    @Query("""
      MATCH (c:Course {code:$code})
      RETURN count(c) > 0
    """)
    Mono<Boolean> existsByCode(String code);

    /** Crear/actualizar relación RELATED con similitud */
    @Query("""
      MATCH (a:Course {code:$fromCode}), (b:Course {code:$toCode})
      MERGE (a)-[r:RELATED]-(b)
      SET r.sim = $similarity
      RETURN a
    """)
    Mono<Course> createRelatedRelationship(String fromCode, String toCode, Double similarity);

    /** Eliminar relación RELATED entre dos cursos */
    @Query("""
      MATCH (a:Course {code:$fromCode})-[r:RELATED]-(b:Course {code:$toCode})
      DELETE r
      RETURN count(r)
    """)
    Mono<Long> deleteRelatedRelationship(String fromCode, String toCode);

    /** Actualizar similitud de una relación RELATED existente */
    @Query("""
      MATCH (a:Course {code:$fromCode})-[r:RELATED]-(b:Course {code:$toCode})
      SET r.sim = $similarity
      RETURN count(r)
    """)
    Mono<Long> updateRelatedSimilarity(String fromCode, String toCode, Double similarity);

    /** Traer cursos por lista de códigos (usado en endpoints de schedule/approved, etc.) */
    Flux<Course> findAllByCodeIn(Collection<String> codes);

    /** ---- DTO/Record para mapear los resultados escalar/columnas ---- */
    record RelatedEdgeRow(String from, String to, Double sim) {}
}
