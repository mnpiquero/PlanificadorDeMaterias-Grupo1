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
}
