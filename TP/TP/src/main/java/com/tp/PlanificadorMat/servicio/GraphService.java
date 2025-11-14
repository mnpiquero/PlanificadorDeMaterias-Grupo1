package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.modelo.Course;
import com.tp.PlanificadorMat.repositorio.CourseRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.AbstractMap;

/**
 * DFS/BFS, Toposort (Kahn), ciclos, Dijkstra, MST (Prim/Kruskal)
 * Complejidad general: O(V+E) (Dijkstra/MST con logV según estructura)
 */
@Service
public class GraphService {
    private final CourseRepository repo;
    public GraphService(CourseRepository repo){ this.repo = repo; }

    //--- Util: construir grafo en memoria ---
    private Mono<Map<String, Set<String>>> buildAdj() {
        return repo.allCourses()
                .map(Course::getCode) // Flux<String>
                .collectList()        // Mono<List<String>> codes
                .flatMap(codes ->
                        Flux.fromIterable(codes) // Flux<String> code
                                .flatMap(code ->
                                        repo.prereqsOf(code)                 // Flux<Course>
                                                .map(Course::getCode)        // Flux<String>
                                                .collectList()               // Mono<List<String>>
                                                .map(list -> new AbstractMap.SimpleEntry<>(
                                                        code,
                                                        new HashSet<String>(list)
                                                ))
                                )
                                .collectMap(
                                        AbstractMap.SimpleEntry::getKey,
                                        AbstractMap.SimpleEntry::getValue,
                                        () -> new HashMap<String, Set<String>>()
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

    // --- Ciclos: por Cypher anyCycle ---
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
        return repo.relatedEdges().collectList().map(relatedEdges -> {
            List<Edge> edgeList = new ArrayList<>();
            for (var edgeRow : relatedEdges) {
                double similarity = (edgeRow.sim() == null) ? 0.0 : edgeRow.sim();
                double weight = similarity <= 0 ? Double.POSITIVE_INFINITY : 1.0 / similarity;
                edgeList.add(new Edge(edgeRow.from(), edgeRow.to(), weight));
            }
            Set<String> allNodes = new HashSet<>();
            edgeList.forEach(edge -> { allNodes.add(edge.u()); allNodes.add(edge.v()); });

            if ("kruskal".equalsIgnoreCase(algo)) return mstKruskal(allNodes, edgeList);
            return mstPrim(allNodes, edgeList);
        });
    }

    /**
     * Algoritmo de Prim con PriorityQueue (versión eficiente O(E log V)).
     * Construye MST agregando aristas de menor peso desde nodos ya incluidos.
     */
    private List<Edge> mstPrim(Set<String> allNodes, List<Edge> allEdges) {
        if (allNodes.isEmpty()) return List.of();
        String startNode = allNodes.iterator().next(); // Nodo inicial arbitrario
        
        // Construir lista de adyacencia bidireccional
        Map<String, List<Edge>> adjacencyList = new HashMap<>();
        for (Edge edge : allEdges) {
            adjacencyList.computeIfAbsent(edge.u, k->new ArrayList<>()).add(edge);
            adjacencyList.computeIfAbsent(edge.v, k->new ArrayList<>()).add(new Edge(edge.v, edge.u, edge.w)); // Arista inversa
        }
        
        Set<String> visitedNodes = new HashSet<>(); // Nodos ya en el MST
        List<Edge> mst = new ArrayList<>();
        // Cola de prioridad: aristas candidatas ordenadas por peso
        PriorityQueue<Edge> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(edge->edge.w));
        visitedNodes.add(startNode);
        priorityQueue.addAll(adjacencyList.getOrDefault(startNode, List.of())); // Agregar aristas del nodo inicial

        // Expandir MST hasta incluir todos los nodos
        while(!priorityQueue.isEmpty() && visitedNodes.size()<allNodes.size()){
            Edge currentEdge = priorityQueue.poll(); // Arista de menor peso
            if (visitedNodes.contains(currentEdge.v)) continue; // Ya conectado, ignorar
            visitedNodes.add(currentEdge.v); 
            mst.add(currentEdge); // Agregar arista al MST
            // Agregar nuevas aristas candidatas desde el nodo recién agregado
            for (Edge neighborEdge : adjacencyList.getOrDefault(currentEdge.v, List.of())) 
                if (!visitedNodes.contains(neighborEdge.v)) priorityQueue.add(neighborEdge);
        }
        return mst;
    }

    /**
     * Algoritmo de Kruskal con Union-Find.
     * Ordena aristas por peso y agrega las que no forman ciclos.
     * Complejidad: O(E log E) por ordenamiento + O(E α(V)) por Union-Find.
     */
    private List<Edge> mstKruskal(Set<String> allNodes, List<Edge> allEdges) {
        List<Edge> mst = new ArrayList<>();
        // Union-Find: cada nodo es su propio padre inicialmente
        Map<String,String> parentMap = new HashMap<>();
        for (String node : allNodes) parentMap.put(node, node);
        
        // Ordenar aristas por peso (greedy: siempre elegir la más liviana)
        allEdges.sort(Comparator.comparingDouble(edge->edge.w));
        
        // Procesar aristas en orden de peso creciente
        for (Edge edge : allEdges) {
            String rootFrom = find(parentMap, edge.u);
            String rootTo = find(parentMap, edge.v); // Encontrar raíces
            if (!rootFrom.equals(rootTo)) { // Si están en componentes diferentes
                parentMap.put(rootFrom, rootTo); // Unir componentes (union)
                mst.add(edge); // Agregar arista al MST
            }
            // Si rootFrom == rootTo, la arista formaría ciclo → ignorar
        }
        return mst;
    }
    
    /**
     * Find con compresión de camino (path compression).
     * Encuentra la raíz del conjunto y optimiza el árbol para futuras búsquedas.
     */
    private String find(Map<String,String> parentMap, String node) {
        if (parentMap.get(node).equals(node)) return node; // Raíz encontrada
        String root = find(parentMap, parentMap.get(node)); // Buscar recursivamente
        parentMap.put(node, root); // Compresión: conectar directamente a la raíz
        return root;
    }

    public record Edge(String u, String v, double w) {}
}
