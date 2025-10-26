package com.tp.PlanificadorMat.repositorio;

import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CourseRepository extends ReactiveNeo4jRepository<Course, String> {

    Mono<Course> findOneByCode(String code);

    @Query("""
      MATCH (c:Course)
      RETURN c
    """)
    Flux<Course> allCourses();

    @Query("""
      MATCH (c:Course {code:$code})-[:REQUIRES]->(p:Course) RETURN p
    """)
    Flux<Course> prereqsOf(String code);

    @Query("""
      // Materias cursables cuando TODOS sus prereqs están en $approved
      MATCH (c:Course)
      WHERE ALL (p IN [(c)-[:REQUIRES]->(x) | x.code] WHERE p IN $approved)
        AND NOT c.code IN $approved
      RETURN c
    """)
    Flux<Course> availableWith(List<String> approved);

    // Para detectar ciclos dirigidos (existe un ciclo en cualquier lado)
    @Query("""
      MATCH p=(a:Course)-[:REQUIRES*]->(a)
      RETURN p LIMIT 1
    """)
    Flux<Object> anyCycle();

    // Subgrafo no-dirigido RELATED para MST
    @Query("""
      MATCH (a:Course)-[r:RELATED]-(b:Course)
      RETURN a.code AS from, b.code AS to, r.sim AS sim
    """)
    Flux<RelatedEdge> relatedEdges();

    interface RelatedEdge {
        String getFrom();
        String getTo();
        Double getSim();
    }

    // Búsqueda por nombre (case insensitive)
    @Query("""
      MATCH (c:Course)
      WHERE toLower(c.name) CONTAINS toLower($namePattern)
      RETURN c
    """)
    Flux<Course> findByNameContaining(String namePattern);

    // Búsqueda avanzada con filtros
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

    // Eliminar curso por código
    @Query("""
      MATCH (c:Course {code:$code})
      DETACH DELETE c
    """)
    Mono<Void> deleteByCode(String code);

    // Verificar si existe un curso
    @Query("""
      MATCH (c:Course {code:$code})
      RETURN count(c) > 0
    """)
    Mono<Boolean> existsByCode(String code);

    // Crear relación RELATED entre dos cursos
    @Query("""
      MATCH (a:Course {code:$fromCode}), (b:Course {code:$toCode})
      MERGE (a)-[r:RELATED]-(b)
      SET r.sim = $similarity
      RETURN a
    """)
    Mono<Course> createRelatedRelationship(String fromCode, String toCode, Double similarity);

    // Eliminar relación RELATED entre dos cursos
    @Query("""
      MATCH (a:Course {code:$fromCode})-[r:RELATED]-(b:Course {code:$toCode})
      DELETE r
      RETURN count(r)
    """)
    Mono<Long> deleteRelatedRelationship(String fromCode, String toCode);

    // Obtener todas las relaciones RELATED de un curso
    @Query("""
      MATCH (a:Course {code:$code})-[r:RELATED]-(b:Course)
      RETURN b.code AS from, a.code AS to, r.sim AS sim
    """)
    Flux<RelatedEdge> getRelatedCourses(String code);

    // Actualizar similaridad de una relación existente
    @Query("""
      MATCH (a:Course {code:$fromCode})-[r:RELATED]-(b:Course {code:$toCode})
      SET r.sim = $similarity
      RETURN count(r)
    """)
    Mono<Long> updateRelatedSimilarity(String fromCode, String toCode, Double similarity);
}
