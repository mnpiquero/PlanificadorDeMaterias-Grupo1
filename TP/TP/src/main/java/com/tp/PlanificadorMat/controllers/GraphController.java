package com.tp.PlanificadorMat.controllers;

import com.tp.PlanificadorMat.servicio.GraphService;
import com.tp.PlanificadorMat.servicio.DijkstraService;
import com.tp.PlanificadorMat.servicio.PrimService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/** Grafos: DFS/BFS, Toposort, Ciclos, Dijkstra, MST (Prim/Kruskal) */
@RestController
@RequestMapping("/graph")
public class GraphController {

    private final GraphService svc;          // DFS/BFS/Topo/Cycles + Kruskal
    private final DijkstraService dijkstra;  // Shortest path
    private final PrimService prim;          // Prim array-based

    public GraphController(GraphService svc, DijkstraService dijkstra, PrimService prim) {
        this.svc = svc;
        this.dijkstra = dijkstra;
        this.prim = prim;
    }

    /** DFS: O(V+E) */
    @GetMapping("/dfs")
    public Mono<List<String>> dfs(@RequestParam String from) {
        return svc.dfs(from).collectList();
    }

    /** BFS por capas: O(V+E) */
    @GetMapping("/bfs-layers")
    public Flux<List<String>> bfs(@RequestParam String from) {
        return svc.bfsLayers(from);
    }

    /** Toposort (Kahn) con aprobadas: O(V+E) */
    @GetMapping("/toposort")
    public Mono<List<String>> topo(@RequestParam(required = false) List<String> approved) {
        return svc.topoOrder(approved);
    }

    /** Detección de ciclos (true/false) */
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

    /**
     * MST sobre RELATED:
     * - algo=prim     -> usa PrimService (array-based)
     * - algo=kruskal  -> usa GraphService.mst("kruskal")
     * - startNode     -> nodo inicial para Prim (opcional)
     */
    @GetMapping("/mst")
    public Mono<List<MstEdgeDTO>> mst(
            @RequestParam(defaultValue = "prim") String algo,
            @RequestParam(required = false) String startNode) {
        if ("kruskal".equalsIgnoreCase(algo)) {
            return svc.mst("kruskal").map(list ->
                    list.stream()
                            .map(e -> new MstEdgeDTO(e.u(), e.v(), e.w()))
                            .toList()
            );
        }
        // default: prim (con nodo inicial opcional)
        return prim.primMST(startNode).map(list ->
                list.stream()
                        .map(e -> new MstEdgeDTO(e.from(), e.to(), (double) e.weight()))
                        .toList()
        );
    }
    
    /**
     * MST Forest: Encuentra MST para cada componente conexa
     * Retorna múltiples árboles (uno por área temática)
     */
    @GetMapping("/mst-forest")
    public Mono<List<PrimService.ComponentMST>> mstForest() {
        return prim.primForest();
    }
    
    /**
     * Estadísticas de conectividad del grafo RELATED
     */
    @GetMapping("/connectivity-stats")
    public Mono<PrimService.ConnectivityStats> connectivityStats() {
        return prim.getConnectivityStats();
    }

    /** DTO unificado para exponer aristas del MST */
    public record MstEdgeDTO(String from, String to, double weight) { }
}
