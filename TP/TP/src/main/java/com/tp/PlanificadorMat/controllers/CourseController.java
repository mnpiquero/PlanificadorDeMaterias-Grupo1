package com.tp.PlanificadorMat.controllers;


import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.repositorio.CourseRepository;

/** CRUD completo para materias */
@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseRepository repo;
    public CourseController(CourseRepository repo){ this.repo = repo; }

    /** Upsert de Course (incluye prereqs si vienen embebidos) */
    @PutMapping
    Mono<Course> upsert(@RequestBody Course c){ 
        validateCourse(c);
        
        // Si tiene prereqs, procesarlos manualmente para evitar sobrescribir nodos existentes
        if (c.getPrereqs() != null && !c.getPrereqs().isEmpty()) {
            // Extraer los códigos de prerequisitos
            java.util.List<String> prereqCodes = new java.util.ArrayList<String>();
            for (Course prereq : c.getPrereqs()) {
                prereqCodes.add(prereq.getCode());
            }
            
            // Limpiar prereqs temporalmente para guardar el curso sin ellos
            c.setPrereqs(new java.util.HashSet<>());
            
            // Guardar el curso sin prereqs
            return repo.save(c)
                .flatMap(savedCourse -> {
                    // Ahora buscar los prerequisitos reales y agregarlos
                    return repo.findAllByCodeIn(prereqCodes)
                        .collectList()
                        .map(prereqs -> {
                            savedCourse.setPrereqs(new java.util.HashSet<>(prereqs));
                            return savedCourse;
                        })
                        .flatMap(repo::save);
                });
        }
        
        // Si no tiene prereqs, guardarlo normalmente
        return repo.save(c); 
    }

    /** Crear nueva materia (falla si ya existe) */
    @PostMapping
    Mono<Course> create(@RequestBody Course c) {
        validateCourse(c);
        return repo.existsByCode(c.getCode())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.CONFLICT, 
                        "Ya existe una materia con código: " + c.getCode()
                    ));
                }
                return repo.save(c);
            });
    }

    /** Listar todas las materias (SSE - streaming) */
    @GetMapping(value={"","/"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Course> all(){ return repo.findAll(); }

    /** Listar todas las materias (JSON - para frontend) */
    @GetMapping(value="/list", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<java.util.List<Course>> allList(){ 
        return repo.findAll().collectList(); 
    }

    /** Obtener materia por código */
    @GetMapping("/{code}")
    Mono<Course> one(@PathVariable String code){ 
        return repo.findOneByCode(code)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Materia no encontrada: " + code
            )));
    }

    /** Eliminar materia por código */
    @DeleteMapping("/{code}")
    Mono<ResponseEntity<Void>> delete(@PathVariable String code) {
        return repo.existsByCode(code)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Materia no encontrada: " + code
                    ));
                }
                return repo.deleteByCode(code)
                    .then(Mono.just(ResponseEntity.noContent().<Void>build()));
            });
    }

    /** Actualización parcial de materia */
    @PatchMapping("/{code}")
    Mono<Course> patch(@PathVariable String code, @RequestBody CoursePatchDTO patch) {
        return repo.findOneByCode(code)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Materia no encontrada: " + code
            )))
            .map(course -> {
                if (patch.getName() != null) course.setName(patch.getName());
                if (patch.getCredits() != null) {
                    validateCredits(patch.getCredits());
                    course.setCredits(patch.getCredits());
                }
                if (patch.getHours() != null) {
                    validateHours(patch.getHours());
                    course.setHours(patch.getHours());
                }
                if (patch.getDifficulty() != null) {
                    validateDifficulty(patch.getDifficulty());
                    course.setDifficulty(patch.getDifficulty());
                }
                return course;
            })
            .flatMap(repo::save);
    }

    /** Búsqueda por nombre */
    @GetMapping("/search/by-name")
    Flux<Course> searchByName(@RequestParam String name) {
        return repo.findByNameContaining(name);
    }

    /** Búsqueda avanzada con múltiples criterios */
    @GetMapping("/search/advanced")
    Flux<Course> advancedSearch(
            @RequestParam(required = false) String nameContains,
            @RequestParam(required = false) Integer minCredits,
            @RequestParam(required = false) Integer maxCredits,
            @RequestParam(required = false) Integer minDifficulty,
            @RequestParam(required = false) Integer maxDifficulty,
            @RequestParam(required = false) Integer minHours,
            @RequestParam(required = false) Integer maxHours
    ) {
        return repo.searchCourses(
            nameContains, 
            minCredits, maxCredits,
            minDifficulty, maxDifficulty,
            minHours, maxHours
        );
    }

    /** Verificar si existe una materia */
    @GetMapping("/{code}/exists")
    Mono<Boolean> exists(@PathVariable String code) {
        return repo.existsByCode(code);
    }

    /** Agregar prerequisitos a una materia sin modificar sus propiedades */
    @PostMapping("/{code}/prereqs")
    Mono<Course> addPrereqs(@PathVariable String code, @RequestBody java.util.List<String> prereqCodes) {
        return repo.findOneByCode(code)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Materia no encontrada: " + code
            )))
            .flatMap(course -> {
                // Buscar todos los prerequisitos por código
                return repo.findAllByCodeIn(prereqCodes)
                    .collectList()
                    .map(prereqs -> {
                        course.setPrereqs(new java.util.HashSet<>(prereqs));
                        return course;
                    });
            })
            .flatMap(repo::save);
    }

    // Validaciones
    private void validateCourse(Course c) {
        if (c.getCode() == null || c.getCode().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código es obligatorio");
        }
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (c.getCredits() != null) validateCredits(c.getCredits());
        if (c.getHours() != null) validateHours(c.getHours());
        if (c.getDifficulty() != null) validateDifficulty(c.getDifficulty());
    }

    private void validateCredits(Integer credits) {
        if (credits < 0 || credits > 12) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Los créditos deben estar entre 0 y 12"
            );
        }
    }

    private void validateHours(Integer hours) {
        if (hours < 0 || hours > 350) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Las horas deben estar entre 0 y 350"
            );
        }
    }

    private void validateDifficulty(Integer difficulty) {
        if (difficulty < 1 || difficulty > 5) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "La dificultad debe estar entre 1 y 5"
            );
        }
    }
}
