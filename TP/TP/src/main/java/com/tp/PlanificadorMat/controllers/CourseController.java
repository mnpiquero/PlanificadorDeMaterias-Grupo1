package com.tp.PlanificadorMat.controllers;


import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.repositorio.CourseRepository;

/** CRUD simple para materias */
@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseRepository repo;
    public CourseController(CourseRepository repo){ this.repo = repo; }

    /** Upsert de Course (incluye prereqs si vienen embebidos) */
    @PutMapping
    Mono<Course> upsert(@RequestBody Course c){ return repo.save(c); }

    @GetMapping(value={"","/"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Course> all(){ return repo.findAll(); }

    @GetMapping("/{code}")
    Mono<Course> one(@PathVariable String code){ return repo.findOneByCode(code); }
}
