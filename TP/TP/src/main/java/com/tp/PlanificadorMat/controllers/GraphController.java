package com.tp.PlanificadorMat.controllers;

import com.tp.PlanificadorMat.servicio.GraphService;
import com.tp.PlanificadorMat.servicio.DijkstraService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/** Grafos: DFS/BFS, topo, ciclos, Dijkstra, MST */
@RestController
@RequestMapping("/graph")
public class GraphController {

    private final GraphService svc;          // DFS/BFS/Topo/Cycles/MST
    private final DijkstraService dijkstra;  // NUEVO servicio para shortest path

    public GraphController(GraphService svc, DijkstraService dijkstra) {
        this.svc = svc;
        this.dijkstra = dijkstra;
    }

    /** DFS: O(V+E) */
    @GetMapping("/dfs")
    public Mono<List<String>> dfs(@RequestParam String from) {
        return svc.dfs(from).collectList();
    }

    /** BFS layers: O(V+E) */
    @GetMapping("/bfs-layers")
    public Flux<List<String>> bfs(@RequestParam String from) {
        return svc.bfsLayers(from);
    }

    /** Toposort (Kahn) con aprobadas: O(V+E) */
    @GetMapping("/toposort")
    public Mono<List<String>> topo(@RequestParam(required = false) List<String> approved) {
        return svc.topoOrder(approved);
    }

    /** Ciclos: true/false (O(V+E)) */
    @GetMapping("/cycles")
    public Mono<Map<String, Boolean>> cycles() {
        return svc.hasCycle().map(b -> Map.of("hasCycle", b));
    }

    /**
     * Dijkstra O((V+E) log V): shortest path según métrica y dirección
     * direction = prereqs (default) | dependents
     */
    @GetMapping("/shortest")
    public Mono<List<String>> shortest(@RequestParam String from,
                                       @RequestParam String to,
                                       @RequestParam(required = false) String metric,
                                       @RequestParam(required = false, defaultValue = "prereqs") String direction) {
        return dijkstra.shortestPath(from, to, metric, direction);
    }

    /** MST (Prim/Kruskal) sobre RELATED: O(E log V) */
    @GetMapping("/mst")
    public Mono<List<MstEdgeDTO>> mst(@RequestParam(defaultValue = "prim") String algo) {
        return svc.mst(algo).map(list ->
                list.stream()
                        .map(e -> new MstEdgeDTO(e.u(), e.v(), e.w()))
                        .toList()
        );
    }

    /** DTO simple para exponer aristas del MST */
    public record MstEdgeDTO(String from, String to, double weight) { }
}
