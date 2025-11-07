package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.repositorio.CourseRepository;

/**
 * DFS/BFS, Toposort (Kahn), ciclos, Dijkstra, MST (Prim/Kruskal)
 * Complejidad general: O(V+E) (Dijkstra/MST con logV según estructura)
 * Implementado con arreglos primitivos y bucles (sin Collections)
 */
@Service
public class GraphService {
    private final CourseRepository repo;
    public GraphService(CourseRepository repo){ this.repo = repo; }

    //--- Util: construir grafo en memoria como arreglos ---
    private Mono<GraphAdjacency> buildAdj() {
        return repo.allCourses()
                .collectList()
                .flatMap(allCourses -> {
                    int n = allCourses.size();
                    String[] codes = new String[n];
                    for (int i = 0; i < n; i++) {
                        codes[i] = allCourses.get(i).getCode();
                    }
                    
                    return Flux.fromArray(codes)
                            .flatMap(code -> 
                                repo.prereqsOf(code)
                                    .collectList()
                                    .map(prereqs -> {
                                        String[] prereqCodes = new String[prereqs.size()];
                                        for (int i = 0; i < prereqs.size(); i++) {
                                            prereqCodes[i] = prereqs.get(i).getCode();
                                        }
                                        return new AdjEntry(code, prereqCodes);
                                    })
                            )
                            .collectList()
                            .map(entries -> {
                                GraphAdjacency adj = new GraphAdjacency(codes);
                                for (AdjEntry entry : entries) {
                                    adj.setAdj(entry.code, entry.prereqs);
                                }
                                // Asegurar nodos sin salientes
                                for (int i = 0; i < codes.length; i++) {
                                    if (adj.getAdj(codes[i]) == null) {
                                        adj.setAdj(codes[i], new String[0]);
                                    }
                                }
                                return adj;
                            });
                });
    }

    // Clase auxiliar para entrada de adyacencia
    private static class AdjEntry {
        String code;
        String[] prereqs;
        AdjEntry(String code, String[] prereqs) {
            this.code = code;
            this.prereqs = prereqs;
        }
    }

    // Clase auxiliar para grafo de adyacencia usando arreglos
    private static class GraphAdjacency {
        private String[] nodes;
        private String[][] adjacency;
        
        GraphAdjacency(String[] nodes) {
            this.nodes = nodes;
            this.adjacency = new String[nodes.length][];
        }
        
        int getIndex(String node) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null && nodes[i].equals(node)) {
                    return i;
                }
            }
            return -1;
        }
        
        boolean containsKey(String node) {
            return getIndex(node) >= 0;
        }
        
        String[] getAdj(String node) {
            int idx = getIndex(node);
            if (idx < 0) return null;
            return adjacency[idx];
        }
        
        void setAdj(String node, String[] adj) {
            int idx = getIndex(node);
            if (idx >= 0) {
                adjacency[idx] = adj;
            }
        }
        
        String[] getNodes() {
            return nodes;
        }
    }

    // --- DFS desde un nodo (sobre edges REQUIRES salientes) ---
    public Flux<String> dfs(String from) {
        return buildAdj().flatMapMany(adj -> {
            String[] visited = new String[adj.getNodes().length];
            int[] visitedSizeRef = new int[]{0};
            String[] out = new String[adj.getNodes().length];
            int[] outSizeRef = new int[]{0};
            dfsRec(from, adj, visited, out, outSizeRef, visitedSizeRef);
            
            // Crear arreglo del tamaño correcto
            int actualSize = outSizeRef[0];
            String[] finalResult = new String[actualSize];
            for (int i = 0; i < actualSize; i++) {
                finalResult[i] = out[i];
            }
            return Flux.fromArray(finalResult);
        });
    }

    private void dfsRec(String u, GraphAdjacency adj, String[] visited, 
                       String[] out, int[] outSizeRef, int[] visitedSizeRef) {
        if (u == null || contains(visited, visitedSizeRef[0], u) || !adj.containsKey(u)) {
            return;
        }
        visited[visitedSizeRef[0]++] = u;
        out[outSizeRef[0]++] = u;
        
        String[] neighbors = adj.getAdj(u);
        if (neighbors != null) {
            for (int i = 0; i < neighbors.length; i++) {
                dfsRec(neighbors[i], adj, visited, out, outSizeRef, visitedSizeRef);
            }
        }
    }

    // --- BFS por capas ---
    public Flux<java.util.List<String>> bfsLayers(String from) {
        return buildAdj().flatMapMany(adj -> {
            if (!adj.containsKey(from)) {
                return Flux.just(new java.util.ArrayList<String>());
            }
            
            String[] visited = new String[adj.getNodes().length];
            int visitedSize = 0;
            String[] queue = new String[adj.getNodes().length];
            int queueStart = 0;
            int queueEnd = 0;
            String[][] layers = new String[adj.getNodes().length][];
            int layersSize = 0;

            visited[visitedSize++] = from;
            queue[queueEnd++] = from;

            while (queueStart < queueEnd) {
                int layerSize = queueEnd - queueStart;
                String[] layer = new String[layerSize];
                int layerIdx = 0;

                for (int i = 0; i < layerSize; i++) {
                    String u = queue[queueStart++];
                    layer[layerIdx++] = u;
                    
                    String[] neighbors = adj.getAdj(u);
                    if (neighbors != null) {
                        for (int j = 0; j < neighbors.length; j++) {
                            String v = neighbors[j];
                            if (!contains(visited, visitedSize, v)) {
                                visited[visitedSize++] = v;
                                queue[queueEnd++] = v;
                            }
                        }
                    }
                }
                layers[layersSize++] = layer;
            }

            // Convertir a List de List
            java.util.List<java.util.List<String>> finalLayers = new java.util.ArrayList<java.util.List<String>>();
            for (int i = 0; i < layersSize; i++) {
                java.util.List<String> layerList = new java.util.ArrayList<String>();
                for (int j = 0; j < layers[i].length; j++) {
                    layerList.add(layers[i][j]);
                }
                finalLayers.add(layerList);
            }
            return Flux.fromIterable(finalLayers);
        });
    }

    // --- Toposort (Kahn) respetando aprobadas (indegree efectivo) ---
    public Mono<java.util.List<String>> topoOrder(java.util.List<String> approved) {
        // Convertir List a array para procesamiento interno
        String[] approvedArr;
        if (approved == null || approved.isEmpty()) {
            approvedArr = new String[0];
        } else {
            approvedArr = new String[approved.size()];
            for (int i = 0; i < approved.size(); i++) {
                approvedArr[i] = approved.get(i);
            }
        }
        return buildAdj().map(adj -> {
            // indegree: # prereqs no aprobados
            int[] indeg = new int[adj.getNodes().length];
            for (int i = 0; i < adj.getNodes().length; i++) {
                String node = adj.getNodes()[i];
                String[] prereqs = adj.getAdj(node);
                int count = 0;
                if (prereqs != null) {
                    for (int j = 0; j < prereqs.length; j++) {
                        if (!contains(approvedArr, approvedArr.length, prereqs[j])) {
                            count++;
                        }
                    }
                }
                indeg[i] = count;
            }
            
            String[] queue = new String[adj.getNodes().length];
            int queueStart = 0;
            int queueEnd = 0;
            
            for (int i = 0; i < adj.getNodes().length; i++) {
                if (indeg[i] == 0 && !contains(approvedArr, approvedArr.length, adj.getNodes()[i])) {
                    queue[queueEnd++] = adj.getNodes()[i];
                }
            }

            String[] order = new String[adj.getNodes().length];
            int orderSize = 0;
            
            while (queueStart < queueEnd) {
                String u = queue[queueStart++];
                order[orderSize++] = u;
                
                // Buscar todos los nodos que tienen a u como prereq
                for (int i = 0; i < adj.getNodes().length; i++) {
                    String[] prereqs = adj.getAdj(adj.getNodes()[i]);
                    if (prereqs != null && contains(prereqs, prereqs.length, u)) {
                        indeg[i]--;
                        if (indeg[i] == 0 && !contains(approvedArr, approvedArr.length, adj.getNodes()[i])) {
                            queue[queueEnd++] = adj.getNodes()[i];
                        }
                    }
                }
            }

            String[] finalOrder = new String[orderSize];
            for (int i = 0; i < orderSize; i++) {
                finalOrder[i] = order[i];
            }
            // Convertir array a List para compatibilidad con firma
            java.util.List<String> result = new java.util.ArrayList<String>();
            for (int i = 0; i < finalOrder.length; i++) {
                result.add(finalOrder[i]);
            }
            return result;
        });
    }

    // --- Ciclos: si Kahn no visita todos los nodos sin aprobadas, o por Cypher anyCycle ---
    public Mono<Boolean> hasCycle() {
        return repo.anyCycle().hasElements();
    }

    // --- Dijkstra: pesos por métrica del destino ---
    // metric: difficulty | hours | credits (usar inverso si querés minimizar carga)
    public Mono<java.util.List<String>> shortestPath(String from, String to, String metric) {
        String m = (metric == null) ? "difficulty" : metric;

        return Mono.zip(
                buildAdj(),
                repo.allCourses().collectList()
        ).flatMap(tuple -> {
            GraphAdjacency adj = tuple.getT1();
            java.util.List<Course> allCourses = tuple.getT2();
            
            if (!adj.containsKey(from) || !adj.containsKey(to)) {
                return Mono.just(new java.util.ArrayList<String>());
            }

            // Crear mapa de cursos por código usando arreglos
            Course[] courses = new Course[allCourses.size()];
            String[] courseCodes = new String[allCourses.size()];
            for (int i = 0; i < allCourses.size(); i++) {
                courses[i] = allCourses.get(i);
                courseCodes[i] = allCourses.get(i).getCode();
            }

            // Dijkstra sobre course -> prereq, pesando por la métrica del nodo destino (el prereq)
            double[] dist = new double[adj.getNodes().length];
            String[] prev = new String[adj.getNodes().length];
            int fromIdx = adj.getIndex(from);
            int toIdx = adj.getIndex(to);
            
            for (int i = 0; i < dist.length; i++) {
                dist[i] = Double.POSITIVE_INFINITY;
                prev[i] = null;
            }
            dist[fromIdx] = 0.0;

            // Priority queue simple usando arreglo y ordenamiento
            int[] pq = new int[adj.getNodes().length];
            int pqSize = 0;
            pq[pqSize++] = fromIdx;

            while (pqSize > 0) {
                // Encontrar mínimo en pq
                int minIdx = 0;
                for (int i = 1; i < pqSize; i++) {
                    if (dist[pq[i]] < dist[pq[minIdx]]) {
                        minIdx = i;
                    }
                }
                int uIdx = pq[minIdx];
                // Remover de pq
                for (int i = minIdx; i < pqSize - 1; i++) {
                    pq[i] = pq[i + 1];
                }
                pqSize--;

                if (uIdx == toIdx) break;

                String u = adj.getNodes()[uIdx];
                String[] neighbors = adj.getAdj(u);
                if (neighbors != null) {
                    for (int i = 0; i < neighbors.length; i++) {
                        String v = neighbors[i];
                        int vIdx = adj.getIndex(v);
                        if (vIdx < 0) continue;
                        
                        Course vCourse = getCourseByCode(courses, courseCodes, v);
                        double w = weightOf(vCourse, m);
                        double nd = dist[uIdx] + w;
                        
                        if (nd < dist[vIdx]) {
                            dist[vIdx] = nd;
                            prev[vIdx] = u;
                            // Agregar a pq si no está
                            if (!containsInt(pq, pqSize, vIdx)) {
                                pq[pqSize++] = vIdx;
                            }
                        }
                    }
                }
            }

            // Reconstrucción
            if (!from.equals(to) && prev[toIdx] == null) {
                return Mono.just(new java.util.ArrayList<String>());
            }
            
            String[] pathRev = new String[adj.getNodes().length];
            int pathSize = 0;
            String cur = to;
            pathRev[pathSize++] = cur;
            
            while (!cur.equals(from)) {
                int curIdx = adj.getIndex(cur);
                if (curIdx < 0 || prev[curIdx] == null) {
                    return Mono.just(new java.util.ArrayList<String>());
                }
                cur = prev[curIdx];
                pathRev[pathSize++] = cur;
            }

            // Revertir y convertir a List
            java.util.List<String> path = new java.util.ArrayList<String>();
            for (int i = pathSize - 1; i >= 0; i--) {
                path.add(pathRev[i]);
            }
            return Mono.just(path);
        });
    }

    private Course getCourseByCode(Course[] courses, String[] codes, String code) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i] != null && codes[i].equals(code)) {
                return courses[i];
            }
        }
        return null;
    }

    private double weightOf(Course c, String metric) {
        if (c == null) return 1.0;
        if ("hours".equals(metric)) {
            return c.getHours() == null ? 1.0 : c.getHours();
        } else if ("credits".equals(metric)) {
            return c.getCredits() == null ? 1.0 : (double)(6 - c.getCredits());
        } else {
            return c.getDifficulty() == null ? 1.0 : c.getDifficulty();
        }
    }

    // --- MST (Prim y Kruskal) sobre subgrafo no dirigido RELATED (peso = 1/sim) ---
    public Mono<java.util.List<Edge>> mst(String algo) {
        return repo.relatedEdges().collectList().map(edges -> {
            Edge[] list = new Edge[edges.size()];
            int listSize = 0;
            for (int i = 0; i < edges.size(); i++) {
                com.tp.PlanificadorMat.repositorio.CourseRepository.RelatedEdge e = edges.get(i);
                double sim = e.getSim() == null ? 0.0 : e.getSim();
                double w = sim <= 0 ? Double.POSITIVE_INFINITY : 1.0 / sim;
                list[listSize++] = new Edge(e.getFrom(), e.getTo(), w);
            }

            // Recolectar nodos únicos
            String[] nodes = new String[listSize * 2];
            int nodesSize = 0;
            for (int i = 0; i < listSize; i++) {
                if (!contains(nodes, nodesSize, list[i].getU())) {
                    nodes[nodesSize++] = list[i].getU();
                }
                if (!contains(nodes, nodesSize, list[i].getV())) {
                    nodes[nodesSize++] = list[i].getV();
                }
            }
            String[] finalNodes = new String[nodesSize];
            for (int i = 0; i < nodesSize; i++) {
                finalNodes[i] = nodes[i];
            }

            Edge[] finalList = new Edge[listSize];
            for (int i = 0; i < listSize; i++) {
                finalList[i] = list[i];
            }

            Edge[] result;
            if ("kruskal".equalsIgnoreCase(algo)) {
                result = mstKruskal(finalNodes, finalList);
            } else {
                result = mstPrim(finalNodes, finalList);
            }
            
            // Convertir array a List
            java.util.List<Edge> resultList = new java.util.ArrayList<Edge>();
            for (int i = 0; i < result.length; i++) {
                resultList.add(result[i]);
            }
            return resultList;
        });
    }

    private Edge[] mstPrim(String[] nodes, Edge[] edges) {
        if (nodes.length == 0) return new Edge[0];
        
        String start = nodes[0];
        // Crear lista de adyacencia usando arreglos
        Edge[][] adj = new Edge[nodes.length][];
        int[] adjSizes = new int[nodes.length];
        // Inicializar
        for (int i = 0; i < nodes.length; i++) {
            adj[i] = new Edge[edges.length];
            adjSizes[i] = 0;
        }

        for (int i = 0; i < edges.length; i++) {
            Edge e = edges[i];
            int uIdx = getIndex(nodes, e.getU());
            int vIdx = getIndex(nodes, e.getV());
            if (uIdx >= 0) {
                adj[uIdx][adjSizes[uIdx]++] = e;
            }
            if (vIdx >= 0) {
                adj[vIdx][adjSizes[vIdx]++] = new Edge(e.getV(), e.getU(), e.getW());
            }
        }

        String[] vis = new String[nodes.length];
        int visSize = 0;
        Edge[] mst = new Edge[nodes.length - 1];
        int mstSize = 0;
        
        // Priority queue simple para edges
        Edge[] pq = new Edge[edges.length];
        int pqSize = 0;
        
        vis[visSize++] = start;
        int startIdx = getIndex(nodes, start);
        if (startIdx >= 0) {
            for (int i = 0; i < adjSizes[startIdx]; i++) {
                pq[pqSize++] = adj[startIdx][i];
            }
        }

        while (pqSize > 0 && visSize < nodes.length) {
            // Encontrar edge con menor peso
            int minIdx = 0;
            for (int i = 1; i < pqSize; i++) {
                if (pq[i].getW() < pq[minIdx].getW()) {
                    minIdx = i;
                }
            }
            Edge e = pq[minIdx];
            // Remover de pq
            for (int i = minIdx; i < pqSize - 1; i++) {
                pq[i] = pq[i + 1];
            }
            pqSize--;

            if (contains(vis, visSize, e.getV())) continue;
            
            vis[visSize++] = e.getV();
            mst[mstSize++] = e;
            
            int vIdx = getIndex(nodes, e.getV());
            if (vIdx >= 0) {
                for (int i = 0; i < adjSizes[vIdx]; i++) {
                    Edge nx = adj[vIdx][i];
                    if (!contains(vis, visSize, nx.getV())) {
                        pq[pqSize++] = nx;
                    }
                }
            }
        }

        Edge[] finalMst = new Edge[mstSize];
        for (int i = 0; i < mstSize; i++) {
            finalMst[i] = mst[i];
        }
        return finalMst;
    }

    private Edge[] mstKruskal(String[] nodes, Edge[] edges) {
        Edge[] mst = new Edge[nodes.length - 1];
        int mstSize = 0;
        String[] parent = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            parent[i] = nodes[i];
        }

        // Ordenar edges por peso (bubble sort simple)
        for (int i = 0; i < edges.length - 1; i++) {
            for (int j = 0; j < edges.length - 1 - i; j++) {
                if (edges[j].getW() > edges[j + 1].getW()) {
                    Edge temp = edges[j];
                    edges[j] = edges[j + 1];
                    edges[j + 1] = temp;
                }
            }
        }

        for (int i = 0; i < edges.length; i++) {
            Edge e = edges[i];
            String ru = find(parent, nodes, e.getU());
            String rv = find(parent, nodes, e.getV());
            if (!ru.equals(rv)) {
                int ruIdx = getIndex(nodes, ru);
                int rvIdx = getIndex(nodes, rv);
                if (ruIdx >= 0 && rvIdx >= 0) {
                    parent[ruIdx] = rv;
                }
                mst[mstSize++] = e;
            }
        }

        Edge[] finalMst = new Edge[mstSize];
        for (int i = 0; i < mstSize; i++) {
            finalMst[i] = mst[i];
        }
        return finalMst;
    }

    private String find(String[] parent, String[] nodes, String x) {
        int xIdx = getIndex(nodes, x);
        if (xIdx < 0) return x;
        if (parent[xIdx].equals(x)) {
            return x;
        }
        String r = find(parent, nodes, parent[xIdx]);
        parent[xIdx] = r;
        return r;
    }

    // Utilidades para trabajar con arreglos
    private boolean contains(String[] arr, int size, String val) {
        if (val == null) return false;
        for (int i = 0; i < size; i++) {
            if (arr[i] != null && arr[i].equals(val)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsInt(int[] arr, int size, int val) {
        for (int i = 0; i < size; i++) {
            if (arr[i] == val) {
                return true;
            }
        }
        return false;
    }

    private int getIndex(String[] arr, String val) {
        if (val == null) return -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null && arr[i].equals(val)) {
                return i;
            }
        }
        return -1;
    }
}
