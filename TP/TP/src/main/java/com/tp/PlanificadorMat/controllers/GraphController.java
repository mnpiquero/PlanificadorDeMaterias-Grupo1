package com.tp.PlanificadorMat.controllers;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.servicio.GraphService;

import java.util.List;
import java.util.Map;

/** Grafos: DFS/BFS, topo, ciclos, Dijkstra, MST */
@RestController
@RequestMapping("/graph")
public class GraphController {

    private final GraphService svc;

    public GraphController(GraphService svc) {
        this.svc = svc;
    }

    /** DFS: O(V+E) */
    @GetMapping("/dfs")
    public reactor.core.publisher.Mono<java.util.List<String>> dfs(@RequestParam String from) {
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

    /** Dijkstra O((V+E) log V): shortest path según métrica */
    @GetMapping("/shortest")
    public Mono<List<String>> shortest(@RequestParam String from,
                                       @RequestParam String to,
                                       @RequestParam(required = false) String metric) {
        return svc.shortestPath(from, to, metric);
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
}
