package com.tp.PlanificadorMat.controllers;

import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.servicio.ScheduleService;

import java.util.List;

/** Greedy, DP, Backtracking, Branch&Bound */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService svc;
    public ScheduleController(ScheduleService svc){ this.svc = svc; }

    /** Disponibles ahora (Cypher ALL) O(V+E) */
    @GetMapping("/available")
    Flux<Course> available(@RequestParam(required=false) List<String> approved) {
        return svc.availableNow(approved);
    }

    /** Greedy por valor (credits|difficulty|hours) con maxHours */
    @GetMapping("/greedy")
    Flux<Course> greedy(@RequestParam(required=false) List<String> approved,
                        @RequestParam(defaultValue="credits") String value,
                        @RequestParam(defaultValue="24") int maxHours){
        return svc.greedySchedule(approved, value, maxHours);
    }

    /** DP Knapsack (O(n*cap)) */
    @GetMapping("/dp")
    Mono<List<Course>> dp(@RequestParam(required=false) List<String> approved,
                          @RequestParam(defaultValue="credits") String value,
                          @RequestParam(defaultValue="24") int maxHours){
        return svc.dpKnapsack(approved, value, maxHours);
    }

    /** Backtracking: rutas de from a to con límite de profundidad */
    @GetMapping("/backtracking")
    Mono<List<List<String>>> bt(@RequestParam String from,
                                @RequestParam String to,
                                @RequestParam(defaultValue="10") int maxDepth){
        return svc.backtrackingPaths(from, to, maxDepth);
    }

    /** Branch & Bound: mejor plan (semesters x maxHours) */
    @GetMapping("/bnb")
    Mono<List<List<String>>> bnb(@RequestParam(required=false) List<String> approved,
                                 @RequestParam(defaultValue="4") int semesters,
                                 @RequestParam(defaultValue="24") int maxHours){
        return svc.branchAndBoundPlan(approved, semesters, maxHours);
    }
}
