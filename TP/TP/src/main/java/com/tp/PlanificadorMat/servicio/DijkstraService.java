package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.modelo.Course;
import com.tp.PlanificadorMat.repositorio.CourseRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Dijkstra con lista de adyacencia por índices  y dirección seleccionable:
 * - direction = "prereqs" (default): edges course -> prereq (camino "hacia atrás")
 * - direction = "dependents": edges prereq -> course (camino "hacia adelante")
 *
 * Peso entero según la métrica del nodo destino: difficulty | hours | credits
 * Complejidad: O((V + E) · log V) temporal (con lista de adyacencia + PriorityQueue) y O(V + E) espacial.
 */
@Service
public class DijkstraService {

    private final CourseRepository repo;

    public DijkstraService(CourseRepository repo) {
        this.repo = repo;
    }

    // Arista compacta por índice
    private static final class Edge {
        final int to;
        final int w;
        Edge(int to, int w) { this.to = to; this.w = w; }
    }

    /**
     * @param from      código de materia origen (ej: "PROG3")
     * @param to        código de materia destino (ej: "AYP")
     * @param metric    "difficulty" (default) | "hours" | "credits"
     * @param direction "prereqs" (default) | "dependents"
     */
    public Mono<List<String>> shortestPath(String from, String to, String metric, String direction) {
        final String m = (metric == null || metric.isBlank()) ? "difficulty" : metric;
        final String dir = (direction == null || direction.isBlank()) ? "prereqs" : direction.toLowerCase();

        return repo.allCoursesWithPrereqs().collectList().flatMap(all -> {
            if (all.isEmpty()) return Mono.just(List.of());

            // --- 1) code -> Course y armado de mapas auxiliares
            Map<String, Course> byCode = new HashMap<>();
            for (Course c : all) byCode.put(c.getCode(), c);

            if (!byCode.containsKey(from) || !byCode.containsKey(to)) return Mono.just(List.of());

            // requiresOut: course -> {prereqs}
            Map<String, Set<String>> requiresOut = new HashMap<>();
            // requiresIn:  prereq  -> {courses que lo requieren}
            Map<String, Set<String>> requiresIn = new HashMap<>();

            for (Course c : all) {
                requiresOut.putIfAbsent(c.getCode(), new HashSet<>());
                if (c.getPrereqs() != null) {
                    for (Course p : c.getPrereqs()) {
                        requiresOut.get(c.getCode()).add(p.getCode());
                        requiresIn.computeIfAbsent(p.getCode(), k -> new HashSet<>()).add(c.getCode());
                    }
                }
            }
            // asegurar llaves
            for (Course c : all) {
                requiresIn.putIfAbsent(c.getCode(), requiresIn.getOrDefault(c.getCode(), new HashSet<>()));
                requiresOut.putIfAbsent(c.getCode(), requiresOut.getOrDefault(c.getCode(), new HashSet<>()));
            }

            // --- 2) code <-> idx
            List<String> codes = new ArrayList<>(byCode.keySet());
            codes.sort(Comparator.naturalOrder());
            Map<String, Integer> idxOf = new HashMap<>();
            for (int i = 0; i < codes.size(); i++) idxOf.put(codes.get(i), i);

            int n   = codes.size();
            int src = idxOf.get(from);
            int dst = idxOf.get(to);

            // --- 3) construir lista de adyacencia según la dirección
            List<List<Edge>> adj = new ArrayList<>(n);
            for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

            if ("dependents".equals(dir)) {
                // prereq (p) -> course (c)
                for (var entry : requiresIn.entrySet()) {
                    int p = idxOf.get(entry.getKey());
                    for (String cCode : entry.getValue()) {
                        int c = idxOf.get(cCode);
                        int w = weightOf(byCode.get(cCode), m); // costo “ir a c”
                        adj.get(p).add(new Edge(c, w));
                    }
                }
            } else {
                // course (c) -> prereq (p)
                for (var entry : requiresOut.entrySet()) {
                    int c = idxOf.get(entry.getKey());
                    for (String pCode : entry.getValue()) {
                        int p = idxOf.get(pCode);
                        int w = weightOf(byCode.get(pCode), m); // costo “ir a p”
                        adj.get(c).add(new Edge(p, w));
                    }
                }
            }

            // --- 4) Dijkstra (arrays + PriorityQueue como tu ejemplo)
            int[] dist = new int[n];
            Arrays.fill(dist, Integer.MAX_VALUE);
            dist[src] = 0;

            int[] prev = new int[n];
            Arrays.fill(prev, -1);

            boolean[] vis = new boolean[n];

            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
            pq.add(new int[]{src, 0});

            while (!pq.isEmpty()) {
                int[] cur = pq.poll();
                int u = cur[0];
                if (vis[u]) continue;
                vis[u] = true;
                if (u == dst) break;

                for (Edge e : adj.get(u)) {
                    int v  = e.to;
                    int nd = (dist[u] == Integer.MAX_VALUE) ? Integer.MAX_VALUE : dist[u] + e.w;
                    if (!vis[v] && nd < dist[v]) {
                        dist[v] = nd;
                        prev[v] = u;
                        pq.add(new int[]{v, nd});
                    }
                }
            }

            if (dist[dst] == Integer.MAX_VALUE) return Mono.just(List.of()); // sin camino

            // --- 5) reconstrucción del camino
            LinkedList<String> path = new LinkedList<>();
            for (int cur = dst; cur != -1; cur = prev[cur]) {
                path.addFirst(codes.get(cur));
                if (cur == src) break;
            }
            return Mono.just(path);
        });
    }

    // Peso entero según la métrica del nodo destino
    private int weightOf(Course c, String metric) {
        if (c == null) return 1;
        return switch (metric) {
            case "hours"   -> c.getHours()     == null ? 1 : c.getHours();
            case "credits" -> c.getCredits()   == null ? 1 : (6 - c.getCredits()); // menor = mejor “carga”
            default        -> c.getDifficulty()== null ? 1 : c.getDifficulty();     // difficulty por defecto
        };
    }
}
