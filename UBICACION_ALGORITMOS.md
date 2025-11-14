# Ubicación de Algoritmos

Este documento lista todos los algoritmos implementados en el proyecto y su ubicación en el código.

## Algoritmos de Grafos

### DFS (Depth-First Search)
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `dfs(String from)` (línea 54)

### BFS (Breadth-First Search)
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `bfsLayers(String from)` (línea 70)

### Ordenamiento Topológico (Kahn)
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `topoOrder(List<String> approved)` (línea 95)

### Detección de Ciclos
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `hasCycle()` (línea 125)

### Dijkstra (Camino Más Corto)
**Ubicación 1:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `shortestPath(String from, String to, String metric)` (línea 131)

**Ubicación 2:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/DijkstraService.java`  
**Método:** `shortestPath(String from, String to, String metric, String direction)` (línea 41)

### MST - Prim
**Ubicación 1:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `mstPrim(Set<String> nodes, List<Edge> edges)` (línea 209)

**Ubicación 2:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/PrimService.java`  
**Método:** `primMST()` (línea 34)

### MST - Kruskal
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/GraphService.java`  
**Método:** `mstKruskal(Set<String> nodes, List<Edge> edges)` (línea 232)

## Algoritmos de Planificación

### Greedy (Selección Voraz)
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/ScheduleService.java`  
**Método:** `greedySchedule(List<String> approved, String value, int maxHours)` (línea 29)

### DP Knapsack (Programación Dinámica)
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/ScheduleService.java`  
**Método:** `dpKnapsack(List<String> approved, String value, int maxHours)` (línea 52)

### Backtracking
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/ScheduleService.java`  
**Método:** `backtrackingPaths(String from, String to, int maxDepth)` (línea 83)

### Branch & Bound
**Ubicación:** `TP/TP/src/main/java/com/tp/PlanificadorMat/servicio/ScheduleService.java`  
**Método:** `branchAndBoundPlan(List<String> approved, int semesters, int maxHours)` (línea 107)

## Interfaz de Usuario (Frontend)

### Página de Algoritmos
**Ubicación:** `frontend/src/pages/Algorithms.tsx`  
**Descripción:** Interfaz React que permite ejecutar los algoritmos desde el navegador.

