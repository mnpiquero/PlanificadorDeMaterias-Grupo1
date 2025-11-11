package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.repositorio.CourseRepository;
import com.tp.PlanificadorMat.repositorio.CourseRepository.RelatedEdgeRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Prim (array-based):
 * - Grafo NO dirigido a partir de RELATED (similitud).
 * - Peso entero positivo: weight = round((1/sim) * SCALE). Si sim <= 0, se descarta la arista.
 * Complejidad: O(V^2 + E) ~ O(V^2) por minKey lineal.
 */
@Service
public class PrimService {

    private static final Logger log = LoggerFactory.getLogger(PrimService.class);

    public record EdgeDTO(String from, String to, int weight) {}

    private static final int INF = Integer.MAX_VALUE;
    private static final double SCALE = 100.0;

    private final CourseRepository repo;

    public PrimService(CourseRepository repo) {
        this.repo = repo;
    }

    public Mono<List<EdgeDTO>> primMST() {
        return repo.relatedEdges().collectList().flatMap(rs -> {
            log.debug("RELATED edges leídas desde Neo4j: {}", rs.size());

            // 1) nodos
            Set<String> nodes = new HashSet<>();
            for (RelatedEdgeRow e : rs) {
                if (e.from() != null) nodes.add(e.from());
                if (e.to()   != null) nodes.add(e.to());
            }
            if (nodes.isEmpty()) {
                log.warn("No hay aristas RELATED o el repositorio no devolvió datos. MST vacío.");
                return Mono.just(List.of());
            }

            // 2) indexado code <-> idx
            List<String> codes = new ArrayList<>(nodes);
            codes.sort(Comparator.naturalOrder());
            Map<String,Integer> idx = new HashMap<>();
            for (int i=0;i<codes.size();i++) idx.put(codes.get(i), i);
            int n = codes.size();

            // 3) lista de adyacencia no dirigida: List<List<int[]{vecino, peso}>>
            List<List<int[]>> graph = new ArrayList<>(n);
            for (int i=0;i<n;i++) graph.add(new ArrayList<>());

            int added = 0;
            for (RelatedEdgeRow e : rs) {
                double sim = (e.sim() == null) ? 0.0 : e.sim();
                if (sim <= 0) continue;

                Integer u = idx.get(e.from());
                Integer v = idx.get(e.to());
                if (u == null || v == null) continue;

                int w = (int)Math.max(1, Math.round((1.0 / sim) * SCALE));
                graph.get(u).add(new int[]{v, w});
                graph.get(v).add(new int[]{u, w});
                added++;
            }
            log.debug("Aristas válidas para Prim (sim>0): {}", added);
            if (added == 0) {
                log.warn("Hay nodos pero ninguna arista RELATED con sim>0. MST vacío.");
                return Mono.just(List.of());
            }

            // 4) Prim clásico (minKey lineal)
            int[] key = new int[n];
            int[] parent = new int[n];
            boolean[] inMST = new boolean[n];
            Arrays.fill(key, INF);
            Arrays.fill(parent, -1);
            key[0] = 0;

            for (int c = 0; c < n - 1; c++) {
                int u = minKey(n, key, inMST);
                if (u == -1) break; // componente no conexa
                inMST[u] = true;

                for (int[] nb : graph.get(u)) {
                    int v = nb[0], w = nb[1];
                    if (!inMST[v] && w < key[v]) {
                        key[v] = w;
                        parent[v] = u;
                    }
                }
            }

            // 5) reconstrucción
            List<EdgeDTO> mst = new ArrayList<>();
            for (int v = 1; v < n; v++) {
                int p = parent[v];
                if (p != -1) {
                    mst.add(new EdgeDTO(codes.get(p), codes.get(v), key[v]));
                }
            }
            log.debug("Aristas en MST: {}", mst.size());
            return Mono.just(mst);
        });
    }

    private int minKey(int n, int[] key, boolean[] inMST) {
        int min = INF, minIdx = -1;
        for (int i = 0; i < n; i++) {
            if (!inMST[i] && key[i] < min) { min = key[i]; minIdx = i; }
        }
        return minIdx;
    }
}


