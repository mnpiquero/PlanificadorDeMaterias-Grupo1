package com.tp.PlanificadorMat.modelo;

import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Nodo de materia.
 * code = ID lógico (único)
 * Métricas: credits, hours (semanales), difficulty (1..5)
 *
 * Relaciones:
 *  - (:Course {A})-[:REQUIRES]->(:Course {B})   A requiere B
 *  - (:Course)-[:RELATED {sim:double}]-(:Course)  (solo para MST no dirigido)
 */
@Node("Course")
public class Course {

    @Id
    private String code;

    private String name;
    private Integer credits;
    private Integer hours;
    private Integer difficulty;

    @Relationship(type = "REQUIRES", direction = Relationship.Direction.OUTGOING)
    private Set<Course> prereqs = new HashSet<>();

    public Course() {}

    public Course(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // --- getters/setters ---
    public String getCode() { return code; }
    public Course setCode(String code) { this.code = code; return this; }
    public String getName() { return name; }
    public Course setName(String name) { this.name = name; return this; }
    public Integer getCredits() { return credits; }
    public Course setCredits(Integer credits) { this.credits = credits; return this; }
    public Integer getHours() { return hours; }
    public Course setHours(Integer hours) { this.hours = hours; return this; }
    public Integer getDifficulty() { return difficulty; }
    public Course setDifficulty(Integer difficulty) { this.difficulty = difficulty; return this; }
    public Set<Course> getPrereqs() { return prereqs; }
    public Course setPrereqs(Set<Course> prereqs) { this.prereqs = prereqs; return this; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course that)) return false;
        return Objects.equals(code, that.code);
    }
    @Override public int hashCode() { return Objects.hash(code); }
}
