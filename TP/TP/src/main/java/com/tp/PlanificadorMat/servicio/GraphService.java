package com.tp.PlanificadorMat.servicio;


import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.repositorio.CourseRepository;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import java.util.*;
import java.util.stream.Collectors;

/**
 * DFS/BFS, Toposort (Kahn), ciclos, Dijkstra, MST (Prim/Kruskal)
 * Complejidad general: O(V+E) (Dijkstra/MST con logV según estructura)
 */
@Service
public class GraphService {
    private final CourseRepository repo;
    public GraphService(CourseRepository repo){ this.repo = repo; }

    //--- Util: construir grafo en memoria ---
    // GraphService.java
    private Mono<Map<String, Set<String>>> buildAdj() {
        return repo.allCourses()
                .map(com.tp.PlanificadorMat.modelo.Course::getCode) // Flux<String>
                .collectList()                                      // Mono<List<String>> codes
                .flatMap(codes ->
                        Flux.fromIterable(codes)                        // Flux<String> code
                                .flatMap(code ->
                                        repo.prereqsOf(code)                    // Flux<Course>
                                                .map(com.tp.PlanificadorMat.modelo.Course::getCode) // Flux<String>
                                                .collectList()                      // Mono<List<String>>
                                                .map(list -> new AbstractMap.SimpleEntry<>(
                                                        code,
                                                        new HashSet<String>(list)   // <-- genérico explícito
                                                ))
                                )
                                // Forzamos el tipo del Map resultante
                                .collectMap(
                                        AbstractMap.SimpleEntry::getKey,
                                        AbstractMap.SimpleEntry::getValue,
                                        () -> new HashMap<String, Set<String>>()   // <-- proveedor tipado
                                )
                                .map(adj -> {
                                    // asegurar nodos sin salientes
                                    for (String c : codes) {
                                        adj.putIfAbsent(c, new HashSet<String>());
                                    }
                                    return adj; // Map<String, Set<String>>
                                })
                );
    }



    // --- DFS desde un nodo (sobre edges REQUIRES salientes) ---
    public Flux<String> dfs(String from) {
        return buildAdj().flatMapMany(adj -> {
            Set<String> vis = new HashSet<>();
            List<String> out = new ArrayList<>();
            dfsRec(from, adj, vis, out);
            return Flux.fromIterable(out);
        });
    }
    private void dfsRec(String u, Map<String, Set<String>> adj, Set<String> vis, List<String> out) {
        if (u==null || vis.contains(u) || !adj.containsKey(u)) return;
        vis.add(u);
        out.add(u);
        for (String v : adj.get(u)) dfsRec(v, adj, vis, out);
    }

    // --- BFS por capas ---
    public Flux<List<String>> bfsLayers(String from) {
        return buildAdj().flatMapMany(adj -> {
            if (!adj.containsKey(from)) return Flux.just(List.of());
            Set<String> vis = new HashSet<>();
            Queue<String> q = new ArrayDeque<>();
            List<List<String>> layers = new ArrayList<>();

            vis.add(from); q.add(from);
            while(!q.isEmpty()){
                int sz = q.size();
                List<String> layer = new ArrayList<>();
                for (int i=0;i<sz;i++){
                    String u = q.poll();
                    layer.add(u);
                    for (String v : adj.get(u)) {
                        if (!vis.contains(v)) { vis.add(v); q.add(v); }
                    }
                }
                layers.add(layer);
            }
            return Flux.fromIterable(layers);
        });
    }

    // --- Toposort (Kahn) respetando aprobadas (indegree efectivo) ---
    public Mono<List<String>> topoOrder(List<String> approved) {
        Set<String> approvedSet = new HashSet<>(Optional.ofNullable(approved).orElse(List.of()));
        return buildAdj().map(adj -> {
            // indegree: # prereqs no aprobados
            Map<String,Integer> indeg = new HashMap<>();
            for (var e : adj.entrySet()) {
                long count = e.getValue().stream().filter(p->!approvedSet.contains(p)).count();
                indeg.put(e.getKey(), (int)count);
            }
            Deque<String> q = new ArrayDeque<>();
            for (var e : indeg.entrySet())
                if (e.getValue()==0 && !approvedSet.contains(e.getKey())) q.add(e.getKey());

            List<String> order = new ArrayList<>();
            while(!q.isEmpty()){
                String u = q.poll();
                order.add(u);
                for (var e : adj.entrySet()) {
                    if (e.getValue().contains(u)) {
                        int v = indeg.get(e.getKey())-1;
                        indeg.put(e.getKey(), v);
                        if (v==0 && !approvedSet.contains(e.getKey())) q.add(e.getKey());
                    }
                }
            }
            return order;
        });
    }

    // --- Ciclos: si Kahn no visita todos los nodos sin aprobadas, o por Cypher anyCycle ---
    public Mono<Boolean> hasCycle() {
        return repo.anyCycle().hasElements();
    }

    // --- Dijkstra: pesos por métrica del destino ---
    // metric: difficulty | hours | credits (usar inverso si querés minimizar carga)
    public Mono<List<String>> shortestPath(String from, String to, String metric) {
        String m = (metric == null) ? "difficulty" : metric;

        return Mono.zip(
                buildAdj(),                                      // Map<String, Set<String>>  course -> prereqs
                repo.allCourses().collectMap(Course::getCode, c -> c) // Map<String, Course>
        ).flatMap(tuple -> {
            Map<String, Set<String>> adj = tuple.getT1();
            Map<String, Course> map = tuple.getT2();

            if (!adj.containsKey(from) || !adj.containsKey(to)) return Mono.just(List.of());

            // Dijkstra sobre course -> prereq, pesando por la métrica del nodo destino (el prereq)
            Map<String, Double> dist = new HashMap<>();
            Map<String, String>  prev = new HashMap<>();
            for (String k : adj.keySet()) dist.put(k, Double.POSITIVE_INFINITY);
            dist.put(from, 0.0);

            PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
            pq.add(from);

            while (!pq.isEmpty()) {
                String u = pq.poll();
                if (u.equals(to)) break;

                for (String v : adj.getOrDefault(u, Set.of())) {
                    double w  = weightOf(map.get(v), m);       // costo de cursar v
                    double nd = dist.get(u) + w;
                    if (nd < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                        dist.put(v, nd);
                        prev.put(v, u);
                        pq.remove(v);
                        pq.add(v);
                    }
                }
            }

            // reconstrucción
            if (!from.equals(to) && !prev.containsKey(to)) return Mono.just(List.of());
            List<String> path = new ArrayList<>();
            String cur = to;
            path.add(cur);
            while (!cur.equals(from)) {
                cur = prev.get(cur);
                if (cur == null) return Mono.just(List.of());
                path.add(cur);
            }
            Collections.reverse(path);
            return Mono.just(path);
        });
    }


    private double weightOf(Course c, String metric) {
        if (c==null) return 1.0;
        return switch (metric) {
            case "hours" -> c.getHours()==null? 1.0 : c.getHours();
            case "credits" -> c.getCredits()==null? 1.0 : (double)(6 - c.getCredits()); // menor es mejor si querés “carga”: ajustable
            default -> c.getDifficulty()==null? 1.0 : c.getDifficulty();
        };
    }

    // --- MST (Prim y Kruskal) sobre subgrafo no dirigido RELATED (peso = 1/sim) ---
    public Mono<List<Edge>> mst(String algo) {
        return repo.relatedEdges().collectList().map(edges -> {
            List<Edge> list = new ArrayList<>();
            for (var e : edges) {
                double sim = e.getSim() == null ? 0.0 : e.getSim();
                double w = sim <= 0 ? Double.POSITIVE_INFINITY : 1.0 / sim;
                list.add(new Edge(e.getFrom(), e.getTo(), w));
            }
            Set<String> nodes = new HashSet<>();
            list.forEach(ed -> { nodes.add(ed.u()); nodes.add(ed.v()); });

            if ("kruskal".equalsIgnoreCase(algo)) return mstKruskal(nodes, list);
            return mstPrim(nodes, list);
        });
    }


    private List<Edge> mstPrim(Set<String> nodes, List<Edge> edges) {
        if (nodes.isEmpty()) return List.of();
        String start = nodes.iterator().next();
        Map<String, List<Edge>> adj = new HashMap<>();
        for (Edge e : edges) {
            adj.computeIfAbsent(e.u,k->new ArrayList<>()).add(e);
            adj.computeIfAbsent(e.v,k->new ArrayList<>()).add(new Edge(e.v,e.u,e.w));
        }
        Set<String> vis = new HashSet<>();
        List<Edge> mst = new ArrayList<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(ed->ed.w));
        vis.add(start);
        pq.addAll(adj.getOrDefault(start, List.of()));

        while(!pq.isEmpty() && vis.size()<nodes.size()){
            Edge e = pq.poll();
            if (vis.contains(e.v)) continue;
            vis.add(e.v); mst.add(e);
            for (Edge nx : adj.getOrDefault(e.v, List.of())) if (!vis.contains(nx.v)) pq.add(nx);
        }
        return mst;
    }

    private List<Edge> mstKruskal(Set<String> nodes, List<Edge> edges) {
        List<Edge> mst = new ArrayList<>();
        Map<String,String> parent = new HashMap<>();
        for (String n : nodes) parent.put(n, n);
        edges.sort(Comparator.comparingDouble(e->e.w));
        for (Edge e : edges) {
            String ru = find(parent, e.u), rv = find(parent, e.v);
            if (!ru.equals(rv)) {
                parent.put(ru, rv); mst.add(e);
            }
        }
        return mst;
    }
    private String find(Map<String,String> p, String x) {
        if (p.get(x).equals(x)) return x;
        String r = find(p, p.get(x));
        p.put(x, r);
        return r;
    }

    public record Edge(String u, String v, double w) {}

}
