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
    
    public record ComponentMST(String componentId, List<String> nodes, List<EdgeDTO> edges, int totalWeight) {}
    
    public record ConnectivityStats(
        int totalNodes,
        int connectedNodes,
        int disconnectedNodes,
        List<String> disconnectedNodesList,
        int totalComponents,
        double coveragePercentage
    ) {}

    private static final int INF = Integer.MAX_VALUE;
    private static final double SCALE = 100.0;

    private final CourseRepository repo;

    public PrimService(CourseRepository repo) {
        this.repo = repo;
    }

    /**
     * MST con Prim desde el primer nodo alfabético (comportamiento por defecto)
     */
    public Mono<List<EdgeDTO>> primMST() {
        return primMST(null);
    }
    
    /**
     * MST con Prim desde un nodo específico
     * @param startNode Código del nodo inicial (null = primer nodo alfabético)
     */
    public Mono<List<EdgeDTO>> primMST(String startNode) {
        return repo.relatedEdges().collectList().flatMap(relatedEdges -> {
            log.debug("RELATED edges leídas desde Neo4j: {}", relatedEdges.size());

            // 1) Extraer todos los nodos únicos del grafo
            Set<String> allNodes = new HashSet<>();
            for (RelatedEdgeRow edgeRow : relatedEdges) {
                if (edgeRow.from() != null) allNodes.add(edgeRow.from());
                if (edgeRow.to()   != null) allNodes.add(edgeRow.to());
            }
            if (allNodes.isEmpty()) {
                log.warn("No hay aristas RELATED o el repositorio no devolvió datos. MST vacío.");
                return Mono.just(List.of());
            }

            // 2) Crear índice bidireccional: código de curso <-> índice numérico
            List<String> sortedNodeCodes = new ArrayList<>(allNodes);
            sortedNodeCodes.sort(Comparator.naturalOrder());
            
            // Si se especifica startNode, ponerlo al inicio
            if (startNode != null && sortedNodeCodes.contains(startNode)) {
                sortedNodeCodes.remove(startNode);
                sortedNodeCodes.add(0, startNode);
                log.debug("Prim iniciando desde nodo especificado: {}", startNode);
            } else if (startNode != null) {
                log.warn("Nodo inicial '{}' no encontrado en el grafo. Usando primer nodo alfabético.", startNode);
            }
            
            Map<String,Integer> codeToIndex = new HashMap<>();
            for (int i=0;i<sortedNodeCodes.size();i++) codeToIndex.put(sortedNodeCodes.get(i), i);
            int numNodes = sortedNodeCodes.size();

            // 3) Construir lista de adyacencia no dirigida (grafo bidireccional)
            List<List<int[]>> adjacencyList = new ArrayList<>(numNodes);
            for (int i=0;i<numNodes;i++) adjacencyList.add(new ArrayList<>());

            int validEdgesCount = 0;
            for (RelatedEdgeRow edgeRow : relatedEdges) {
                double similarity = (edgeRow.sim() == null) ? 0.0 : edgeRow.sim();
                if (similarity <= 0) continue; // Descartar aristas con similitud inválida

                Integer fromIndex = codeToIndex.get(edgeRow.from());
                Integer toIndex = codeToIndex.get(edgeRow.to());
                if (fromIndex == null || toIndex == null) continue;

                // Convertir similitud a peso: mayor similitud = menor peso
                int weight = (int)Math.max(1, Math.round((1.0 / similarity) * SCALE));
                adjacencyList.get(fromIndex).add(new int[]{toIndex, weight});
                adjacencyList.get(toIndex).add(new int[]{fromIndex, weight}); // Grafo no dirigido
                validEdgesCount++;
            }
            log.debug("Aristas válidas para Prim (sim>0): {}", validEdgesCount);
            if (validEdgesCount == 0) {
                log.warn("Hay nodos pero ninguna arista RELATED con sim>0. MST vacío.");
                return Mono.just(List.of());
            }

            // 4) Algoritmo de Prim: construir MST desde nodo inicial
            int[] minWeight = new int[numNodes];      // Peso mínimo conocido para llegar a cada nodo
            int[] parentIndex = new int[numNodes];   // Padre de cada nodo en el MST
            boolean[] inMST = new boolean[numNodes]; // Nodos ya incluidos en el MST
            Arrays.fill(minWeight, INF);
            Arrays.fill(parentIndex, -1);
            minWeight[0] = 0; // Nodo inicial con peso 0

            // Iterar V-1 veces (MST tiene V-1 aristas)
            for (int iteration = 0; iteration < numNodes - 1; iteration++) {
                // Seleccionar nodo con menor key que aún no está en MST
                int currentNodeIndex = minKey(numNodes, minWeight, inMST);
                if (currentNodeIndex == -1) break; // Grafo desconectado: no hay más nodos alcanzables
                inMST[currentNodeIndex] = true; // Agregar nodo al MST

                // Actualizar keys de vecinos no incluidos en MST
                for (int[] neighborEdge : adjacencyList.get(currentNodeIndex)) {
                    int neighborIndex = neighborEdge[0];
                    int edgeWeight = neighborEdge[1];
                    if (!inMST[neighborIndex] && edgeWeight < minWeight[neighborIndex]) {
                        minWeight[neighborIndex] = edgeWeight;        // Actualizar peso mínimo
                        parentIndex[neighborIndex] = currentNodeIndex;     // Registrar padre
                    }
                }
            }

            // 5) Reconstruir MST: convertir estructura de padres a lista de aristas
            List<EdgeDTO> mst = new ArrayList<>();
            for (int nodeIndex = 1; nodeIndex < numNodes; nodeIndex++) {
                int parentIdx = parentIndex[nodeIndex];
                if (parentIdx != -1) {
                    mst.add(new EdgeDTO(sortedNodeCodes.get(parentIdx), sortedNodeCodes.get(nodeIndex), minWeight[nodeIndex]));
                }
            }
            log.debug("Aristas en MST: {}", mst.size());
            return Mono.just(mst);
        });
    }

    /**
     * Encuentra el nodo con menor key que aún no está en el MST.
     * Complejidad: O(V) - búsqueda lineal.
     */
    private int minKey(int numNodes, int[] minWeight, boolean[] inMST) {
        int minWeightValue = INF, minIndex = -1;
        for (int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
            if (!inMST[nodeIndex] && minWeight[nodeIndex] < minWeightValue) { 
                minWeightValue = minWeight[nodeIndex]; 
                minIndex = nodeIndex; 
            }
        }
        return minIndex; // -1 si no hay nodos disponibles (grafo desconectado)
    }
    
    /**
     * Prim Forest: Encuentra MST para cada componente conexa del grafo
     * Retorna múltiples árboles (uno por área temática/componente)
     */
    public Mono<List<ComponentMST>> primForest() {
        return repo.relatedEdges().collectList().flatMap(relatedEdges -> {
            log.debug("RELATED edges para Forest: {}", relatedEdges.size());
            
            // 1) Construir grafo
            Set<String> allNodes = new HashSet<>();
            for (RelatedEdgeRow edgeRow : relatedEdges) {
                if (edgeRow.from() != null) allNodes.add(edgeRow.from());
                if (edgeRow.to() != null) allNodes.add(edgeRow.to());
            }
            
            if (allNodes.isEmpty()) {
                return Mono.just(List.of());
            }
            
            List<String> sortedNodeCodes = new ArrayList<>(allNodes);
            sortedNodeCodes.sort(Comparator.naturalOrder());
            Map<String, Integer> codeToIndex = new HashMap<>();
            for (int i = 0; i < sortedNodeCodes.size(); i++) codeToIndex.put(sortedNodeCodes.get(i), i);
            int numNodes = sortedNodeCodes.size();
            
            // Lista de adyacencia
            List<List<int[]>> adjacencyList = new ArrayList<>(numNodes);
            for (int i = 0; i < numNodes; i++) adjacencyList.add(new ArrayList<>());
            
            for (RelatedEdgeRow edgeRow : relatedEdges) {
                double similarity = (edgeRow.sim() == null) ? 0.0 : edgeRow.sim();
                if (similarity <= 0) continue;
                
                Integer fromIndex = codeToIndex.get(edgeRow.from());
                Integer toIndex = codeToIndex.get(edgeRow.to());
                if (fromIndex == null || toIndex == null) continue;
                
                int weight = (int) Math.max(1, Math.round((1.0 / similarity) * SCALE));
                adjacencyList.get(fromIndex).add(new int[]{toIndex, weight});
                adjacencyList.get(toIndex).add(new int[]{fromIndex, weight});
            }
            
            // 2) Encontrar componentes conexas: ejecutar Prim en cada componente desconectada
            boolean[] visited = new boolean[numNodes];
            List<ComponentMST> forest = new ArrayList<>();
            int componentId = 1;
            
            // Iterar sobre todos los nodos para encontrar componentes
            for (int startNodeIndex = 0; startNodeIndex < numNodes; startNodeIndex++) {
                if (visited[startNodeIndex]) continue; // Ya procesado en otra componente
                
                // Ejecutar Prim desde este nodo no visitado (nueva componente)
                int[] minWeight = new int[numNodes];
                int[] parentIndex = new int[numNodes];
                boolean[] inMST = new boolean[numNodes];
                Arrays.fill(minWeight, INF);
                Arrays.fill(parentIndex, -1);
                minWeight[startNodeIndex] = 0; // Nodo inicial de esta componente
                
                List<EdgeDTO> componentEdges = new ArrayList<>();
                Set<String> componentNodes = new HashSet<>();
                int totalWeight = 0;
                
                // Prim: expandir MST hasta cubrir toda la componente
                for (int iteration = 0; iteration < numNodes; iteration++) {
                    int currentNodeIndex = minKey(numNodes, minWeight, inMST);
                    if (currentNodeIndex == -1) break; // No hay más nodos en esta componente
                    inMST[currentNodeIndex] = true;
                    visited[currentNodeIndex] = true; // Marcar como visitado globalmente
                    componentNodes.add(sortedNodeCodes.get(currentNodeIndex));
                    
                    // Actualizar keys de vecinos
                    for (int[] neighborEdge : adjacencyList.get(currentNodeIndex)) {
                        int neighborIndex = neighborEdge[0];
                        int edgeWeight = neighborEdge[1];
                        if (!inMST[neighborIndex] && edgeWeight < minWeight[neighborIndex]) {
                            minWeight[neighborIndex] = edgeWeight;
                            parentIndex[neighborIndex] = currentNodeIndex;
                        }
                    }
                }
                
                // Reconstruir aristas del MST de esta componente
                for (int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
                    int parentIdx = parentIndex[nodeIndex];
                    if (parentIdx != -1 && inMST[nodeIndex]) {
                        componentEdges.add(new EdgeDTO(sortedNodeCodes.get(parentIdx), sortedNodeCodes.get(nodeIndex), minWeight[nodeIndex]));
                        totalWeight += minWeight[nodeIndex];
                    }
                }
                
                // Agregar componente al bosque (incluso si es un nodo aislado)
                if (!componentEdges.isEmpty() || componentNodes.size() == 1) {
                    String componentIdStr = "component_" + componentId;
                    forest.add(new ComponentMST(
                        componentIdStr,
                        new ArrayList<>(componentNodes),
                        componentEdges,
                        totalWeight
                    ));
                    componentId++;
                }
            }
            
            log.debug("Componentes encontradas: {}", forest.size());
            return Mono.just(forest);
        });
    }
    
    /**
     * Estadísticas de conectividad del grafo RELATED
     */
    public Mono<ConnectivityStats> getConnectivityStats() {
        return repo.allCourses().collectList().flatMap(allCourses -> 
            repo.relatedEdges().collectList().map(edges -> {
                Set<String> allNodes = allCourses.stream()
                    .map(c -> c.getCode())
                    .collect(java.util.stream.Collectors.toSet());
                
                Set<String> connectedNodes = new HashSet<>();
                for (var e : edges) {
                    if (e.from() != null) connectedNodes.add(e.from());
                    if (e.to() != null) connectedNodes.add(e.to());
                }
                
                List<String> disconnected = new ArrayList<>(allNodes);
                disconnected.removeAll(connectedNodes);
                disconnected.sort(Comparator.naturalOrder());
                
                int total = allNodes.size();
                int connected = connectedNodes.size();
                int disconnectedCount = disconnected.size();
                double coverage = total > 0 ? (connected * 100.0 / total) : 0.0;
                
                // Contar componentes (simplificado: usamos el tamaño del forest)
                int components = disconnectedCount; // Cada nodo desconectado es una componente
                // + componentes conexas (las contaremos desde el forest)
                
                return new ConnectivityStats(
                    total,
                    connected,
                    disconnectedCount,
                    disconnected,
                    components + 1, // +1 para la componente principal
                    Math.round(coverage * 100.0) / 100.0
                );
            })
        );
    }
}


