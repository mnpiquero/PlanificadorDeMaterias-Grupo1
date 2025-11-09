package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.modelo.Course;
import com.tp.PlanificadorMat.repositorio.CourseRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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

        // Cargar todos los cursos y luego sus prereqs (más confiable que allCoursesWithPrereqs)
        return repo.allCourses()
                .collectMap(Course::getCode, c -> c)
                .flatMap(byCode -> {
                    if (byCode.isEmpty()) return Mono.just(List.of());
                    if (!byCode.containsKey(from) || !byCode.containsKey(to)) return Mono.just(List.of());

                    // Cargar prereqs para cada curso
                    return Flux.fromIterable(byCode.keySet())
                            .flatMap(code -> repo.prereqsOf(code)
                                    .map(Course::getCode)
                                    .collectList()
                                    .map(prereqCodes -> new AbstractMap.SimpleEntry<>(code, prereqCodes))
                            )
                            .collectMap(
                                    AbstractMap.SimpleEntry::getKey,
                                    AbstractMap.SimpleEntry::getValue
                            )
                            .flatMap(prereqsMap -> {
                                // Asegurar que todos los prereqs estén en byCode
                                // (cargar cursos faltantes si es necesario)
                                Set<String> missingCodes = new HashSet<>();
                                for (List<String> prereqCodes : prereqsMap.values()) {
                                    for (String prereqCode : prereqCodes) {
                                        if (!byCode.containsKey(prereqCode)) {
                                            missingCodes.add(prereqCode);
                                        }
                                    }
                                }
                                
                                // Si faltan cursos, cargarlos
                                if (!missingCodes.isEmpty()) {
                                    return repo.findAllByCodeIn(missingCodes)
                                            .collectMap(Course::getCode, c -> c)
                                            .map(missingCourses -> {
                                                byCode.putAll(missingCourses);
                                                return buildGraph(byCode, prereqsMap, from, to, m, dir);
                                            });
                                } else {
                                    return Mono.just(buildGraph(byCode, prereqsMap, from, to, m, dir));
                                }
                            });
                });
    }
    
    private List<String> buildGraph(Map<String, Course> byCode,
                                    Map<String, List<String>> prereqsMap,
                                    String from, String to,
                                    String metric, String direction) {
        // requiresOut: course -> {prereqs}
        Map<String, Set<String>> requiresOut = new HashMap<>();
        // requiresIn:  prereq  -> {courses que lo requieren}
        Map<String, Set<String>> requiresIn = new HashMap<>();

        // Inicializar todos los cursos
        for (String code : byCode.keySet()) {
            requiresOut.putIfAbsent(code, new HashSet<>());
            requiresIn.putIfAbsent(code, new HashSet<>());
        }

        // Construir mapas de relaciones (solo incluir prereqs que existen en byCode)
        for (var entry : prereqsMap.entrySet()) {
            String courseCode = entry.getKey();
            if (!byCode.containsKey(courseCode)) continue;
            
            List<String> prereqCodes = entry.getValue();
            for (String prereqCode : prereqCodes) {
                if (byCode.containsKey(prereqCode)) {
                    requiresOut.get(courseCode).add(prereqCode);
                    requiresIn.get(prereqCode).add(courseCode);
                }
            }
        }
        
        return buildPath(byCode, requiresOut, requiresIn, from, to, metric, direction);
    }
    
    private List<String> buildPath(Map<String, Course> byCode,
                                   Map<String, Set<String>> requiresOut,
                                   Map<String, Set<String>> requiresIn,
                                   String from, String to,
                                   String metric, String direction) {
        // --- 1) code <-> idx
        List<String> codes = new ArrayList<>(byCode.keySet());
        codes.sort(Comparator.naturalOrder());
        Map<String, Integer> idxOf = new HashMap<>();
        for (int i = 0; i < codes.size(); i++) idxOf.put(codes.get(i), i);

        if (!idxOf.containsKey(from) || !idxOf.containsKey(to)) {
            return List.of();
        }

        int n   = codes.size();
        int src = idxOf.get(from);
        int dst = idxOf.get(to);

        // --- 2) construir lista de adyacencia según la dirección
        List<List<Edge>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        if ("dependents".equals(direction)) {
            // prereq (p) -> course (c)
            for (var entry : requiresIn.entrySet()) {
                String pCode = entry.getKey();
                if (!idxOf.containsKey(pCode)) continue;
                int p = idxOf.get(pCode);
                for (String cCode : entry.getValue()) {
                    if (!idxOf.containsKey(cCode)) continue;
                    int c = idxOf.get(cCode);
                    int w = weightOf(byCode.get(cCode), metric); // costo "ir a c"
                    adj.get(p).add(new Edge(c, w));
                }
            }
        } else {
            // course (c) -> prereq (p)
            for (var entry : requiresOut.entrySet()) {
                String cCode = entry.getKey();
                if (!idxOf.containsKey(cCode)) continue;
                int c = idxOf.get(cCode);
                for (String pCode : entry.getValue()) {
                    if (!idxOf.containsKey(pCode)) continue;
                    int p = idxOf.get(pCode);
                    int w = weightOf(byCode.get(pCode), metric); // costo "ir a p"
                    adj.get(c).add(new Edge(p, w));
                }
            }
        }

        // --- 3) Dijkstra (arrays + PriorityQueue)
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
                if (dist[u] == Integer.MAX_VALUE) continue; // evitar overflow
                int nd = dist[u] + e.w;
                if (nd < 0) continue; // evitar overflow (aunque no debería pasar)
                if (!vis[v] && nd < dist[v]) {
                    dist[v] = nd;
                    prev[v] = u;
                    pq.add(new int[]{v, nd});
                }
            }
        }

        if (dist[dst] == Integer.MAX_VALUE) return List.of(); // sin camino

        // --- 4) reconstrucción del camino
        List<String> path = new ArrayList<>();
        int cur = dst;
        while (cur != -1) {
            path.add(0, codes.get(cur)); // insertar al inicio
            if (cur == src) break;
            cur = prev[cur];
        }
        return path;
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
