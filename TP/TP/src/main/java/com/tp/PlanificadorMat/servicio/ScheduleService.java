package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.repositorio.CourseRepository;

/**
 * Greedy (selección de materias por cuatri),
 * DP (knapsack),
 * Backtracking (todas las rutas factibles a target),
 * Branch & Bound (mejor plan a N cuatris)
 * Implementado con arreglos primitivos y bucles (sin Collections)
 */
@Service
public class ScheduleService {
    private final CourseRepository repo;
    public ScheduleService(CourseRepository repo){ this.repo = repo; }

    // Disponibles ahora según aprobadas (delegando en Cypher)
    public Flux<Course> availableNow(java.util.List<String> approved) {
        java.util.List<String> approvedList = (approved == null) ? new java.util.ArrayList<String>() : approved;
        return repo.availableWith(approvedList);
    }

    // --- Greedy: llenar cuatrimestre con maxHours, ordenando por valor/horas ---
    public Flux<Course> greedySchedule(java.util.List<String> approved, String value, int maxHours) {
        return availableNow(approved).collectList().flatMapMany(list -> {
            // Convertir a arreglos
            Course[] courses = new Course[list.size()];
            for (int i = 0; i < list.size(); i++) {
                courses[i] = list.get(i);
            }
            
            // Ordenar por score descendente (bubble sort)
            for (int i = 0; i < courses.length - 1; i++) {
                for (int j = 0; j < courses.length - 1 - i; j++) {
                    double score1 = score(courses[j], value);
                    double score2 = score(courses[j + 1], value);
                    if (score1 < score2) {
                        Course temp = courses[j];
                        courses[j] = courses[j + 1];
                        courses[j + 1] = temp;
                    }
                }
            }
            
            // Seleccionar cursos
            Course[] pick = new Course[courses.length];
            int pickSize = 0;
            int sumH = 0;
            for (int i = 0; i < courses.length; i++) {
                int h = courses[i].getHours() == null ? 0 : courses[i].getHours();
                if (sumH + h <= maxHours) {
                    pick[pickSize++] = courses[i];
                    sumH += h;
                }
            }
            
            Course[] finalPick = new Course[pickSize];
            for (int i = 0; i < pickSize; i++) {
                finalPick[i] = pick[i];
            }
            
            // Convertir a List para Flux
            java.util.List<Course> resultList = new java.util.ArrayList<Course>();
            for (int i = 0; i < finalPick.length; i++) {
                resultList.add(finalPick[i]);
            }
            return Flux.fromIterable(resultList);
        });
    }
    
    private double score(Course c, String value) {
        String val = (value == null) ? "credits" : value;
        if ("difficulty".equals(val)) {
            return c.getDifficulty() == null ? 0 : (6 - c.getDifficulty());
        } else if ("hours".equals(val)) {
            return c.getHours() == null ? 0 : (10.0 - c.getHours());
        } else {
            return c.getCredits() == null ? 0 : c.getCredits();
        }
    }

    // --- DP Knapsack: maximizar valor con constraint de horas ---
    public Mono<java.util.List<Course>> dpKnapsack(java.util.List<String> approved, String value, int maxHours) {
        return availableNow(approved).collectList().map(items -> {
            int n = items.size();
            int[] W = new int[n];
            int[] V = new int[n];
            Course[] itemsArr = new Course[n];
            for (int i = 0; i < n; i++) {
                itemsArr[i] = items.get(i);
                W[i] = itemsArr[i].getHours() == null ? 0 : itemsArr[i].getHours();
                V[i] = (int)Math.round(score(itemsArr[i], value));
            }
            
            int[][] dp = new int[n+1][maxHours+1];
            boolean[][] take = new boolean[n+1][maxHours+1];
            for (int i = 1; i <= n; i++) {
                for (int w = 0; w <= maxHours; w++) {
                    int notake = dp[i-1][w];
                    int takev = (W[i-1] <= w) ? dp[i-1][w-W[i-1]] + V[i-1] : Integer.MIN_VALUE / 4;
                    if (takev > notake) {
                        dp[i][w] = takev;
                        take[i][w] = true;
                    } else {
                        dp[i][w] = notake;
                    }
                }
            }
            
            // Reconstrucción
            Course[] pick = new Course[n];
            int pickSize = 0;
            int w = maxHours;
            for (int i = n; i >= 1; i--) {
                if (take[i][w]) {
                    pick[pickSize++] = itemsArr[i-1];
                    w -= W[i-1];
                }
            }
            
            // Revertir arreglo
            for (int i = 0; i < pickSize / 2; i++) {
                Course temp = pick[i];
                pick[i] = pick[pickSize - 1 - i];
                pick[pickSize - 1 - i] = temp;
            }
            
            java.util.List<Course> resultList = new java.util.ArrayList<Course>();
            for (int i = 0; i < pickSize; i++) {
                resultList.add(pick[i]);
            }
            return resultList;
        });
    }

    // Clase auxiliar para grafo de adyacencia
    private static class GraphAdj {
        String[] nodes;
        String[][] adjacency;
        int[] adjSizes;
        
        GraphAdj(String[] nodes) {
            this.nodes = nodes;
            this.adjacency = new String[nodes.length][];
            this.adjSizes = new int[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                this.adjacency[i] = new String[nodes.length];
                this.adjSizes[i] = 0;
            }
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
            String[] result = new String[adjSizes[idx]];
            for (int i = 0; i < adjSizes[idx]; i++) {
                result[i] = adjacency[idx][i];
            }
            return result;
        }
        
        void addEdge(String from, String to) {
            int fromIdx = getIndex(from);
            if (fromIdx >= 0 && adjSizes[fromIdx] < adjacency[fromIdx].length) {
                adjacency[fromIdx][adjSizes[fromIdx]++] = to;
            }
        }
    }

    // --- Backtracking: rutas desde 'from' a 'to' respetando prereqs (sobre REQUIRES) ---
    public Mono<java.util.List<java.util.List<String>>> backtrackingPaths(String from, String to, int maxDepth) {
        return repo.allCourses().collectList().map(all -> {
            // Construir grafo usando arreglos
            String[] codes = new String[all.size()];
            for (int i = 0; i < all.size(); i++) {
                codes[i] = all.get(i).getCode();
            }
            GraphAdj adj = new GraphAdj(codes);
            
            for (int i = 0; i < all.size(); i++) {
                Course c = all.get(i);
                if (c.getPrereqs() != null) {
                    for (Course p : c.getPrereqs()) {
                        adj.addEdge(c.getCode(), p.getCode());
                    }
                }
            }
            
            // Backtracking
            String[][] paths = new String[100][]; // Máximo 100 paths
            int pathsSize = 0;
            String[] currentPath = new String[maxDepth + 1];
            int[] currentPathSize = new int[]{0};
            String[] visited = new String[codes.length];
            int[] visitedSize = new int[]{0};
            
            backtrack(from, to, maxDepth, adj, currentPath, currentPathSize, visited, visitedSize, paths, new int[]{0});
            
            // Convertir a List de List
            java.util.List<java.util.List<String>> result = new java.util.ArrayList<java.util.List<String>>();
            for (int i = 0; i < pathsSize; i++) {
                if (paths[i] != null) {
                    java.util.List<String> pathList = new java.util.ArrayList<String>();
                    for (int j = 0; j < paths[i].length; j++) {
                        pathList.add(paths[i][j]);
                    }
                    result.add(pathList);
                }
            }
            return result;
        });
    }
    
    private void backtrack(String u, String to, int md, GraphAdj adj,
                          String[] path, int[] pathSizeRef,
                          String[] visited, int[] visitedSizeRef,
                          String[][] out, int[] outSizeRef) {
        if (md < 0 || u == null || !adj.containsKey(u)) {
            return;
        }
        path[pathSizeRef[0]++] = u;
        visited[visitedSizeRef[0]++] = u;
        
        if (u.equals(to)) {
            // Guardar path actual
            String[] pathCopy = new String[pathSizeRef[0]];
            for (int i = 0; i < pathSizeRef[0]; i++) {
                pathCopy[i] = path[i];
            }
            out[outSizeRef[0]++] = pathCopy;
        } else {
            String[] neighbors = adj.getAdj(u);
            if (neighbors != null) {
                for (int i = 0; i < neighbors.length; i++) {
                    String v = neighbors[i];
                    if (!contains(visited, visitedSizeRef[0], v)) {
                        backtrack(v, to, md - 1, adj, path, pathSizeRef, visited, visitedSizeRef, out, outSizeRef);
                    }
                }
            }
        }
        
        pathSizeRef[0]--;
        removeFromArray(visited, visitedSizeRef, u);
    }

    // --- Branch & Bound: mejor plan a N cuatrimestres (maximiza créditos) ---
    public Mono<java.util.List<java.util.List<String>>> branchAndBoundPlan(java.util.List<String> approved, int semesters, int maxHours) {
        return repo.allCourses().collectList().map(all -> {
            // Crear mapas usando arreglos
            Course[] courses = new Course[all.size()];
            String[] courseCodes = new String[all.size()];
            for (int i = 0; i < all.size(); i++) {
                courses[i] = all.get(i);
                courseCodes[i] = all.get(i).getCode();
            }
            
            // Construir prereqs usando arreglos
            String[][] prereqs = new String[all.size()][];
            int[] prereqSizes = new int[all.size()];
            for (int i = 0; i < all.size(); i++) {
                prereqs[i] = new String[all.size()];
                prereqSizes[i] = 0;
                Course c = all.get(i);
                if (c.getPrereqs() != null) {
                    for (Course p : c.getPrereqs()) {
                        prereqs[i][prereqSizes[i]++] = p.getCode();
                    }
                }
            }
            
            String[] approvedArr;
            if (approved == null || approved.isEmpty()) {
                approvedArr = new String[0];
            } else {
                approvedArr = new String[approved.size()];
                for (int i = 0; i < approved.size(); i++) {
                    approvedArr[i] = approved.get(i);
                }
            }
            
            // BnB
            String[][][] bestPlan = new String[1][][];
            int[] bestValue = new int[]{-1};
            String[][] currentPlan = new String[semesters][];
            int[] currentPlanSize = new int[]{0};
            
            bnb(0, semesters, maxHours, approvedArr, courseCodes, courses, prereqs, prereqSizes,
                currentPlan, currentPlanSize, 0, bestValue, bestPlan);
            
            // Convertir a List de List
            java.util.List<java.util.List<String>> result = new java.util.ArrayList<java.util.List<String>>();
            if (bestPlan[0] != null) {
                for (int i = 0; i < bestPlan[0].length; i++) {
                    if (bestPlan[0][i] != null) {
                        java.util.List<String> semesterList = new java.util.ArrayList<String>();
                        for (int j = 0; j < bestPlan[0][i].length; j++) {
                            semesterList.add(bestPlan[0][i][j]);
                        }
                        result.add(semesterList);
                    }
                }
            }
            return result;
        });
    }

    private void bnb(int sem, int S, int maxH, String[] approved,
                     String[] courseCodes, Course[] courses,
                     String[][] prereqs, int[] prereqSizes,
                     String[][] curPlan, int[] curPlanSizeRef, int curVal,
                     int[] bestVal, String[][][] bestPlan) {
        
        if (sem == S) {
            if (curVal > bestVal[0]) {
                bestVal[0] = curVal;
                // Copiar plan actual
                bestPlan[0] = new String[curPlanSizeRef[0]][];
                for (int i = 0; i < curPlanSizeRef[0]; i++) {
                    if (curPlan[i] != null) {
                        bestPlan[0][i] = new String[curPlan[i].length];
                        for (int j = 0; j < curPlan[i].length; j++) {
                            bestPlan[0][i][j] = curPlan[i][j];
                        }
                    }
                }
            }
            return;
        }
        
        // Candidatos disponibles
        String[] cand = new String[courseCodes.length];
        int candSize = 0;
        for (int i = 0; i < courseCodes.length; i++) {
            String c = courseCodes[i];
            if (!contains(approved, approved.length, c)) {
                // Verificar que todos los prereqs estén aprobados
                boolean allPrereqsApproved = true;
                for (int j = 0; j < prereqSizes[i]; j++) {
                    if (!contains(approved, approved.length, prereqs[i][j])) {
                        allPrereqsApproved = false;
                        break;
                    }
                }
                if (allPrereqsApproved) {
                    cand[candSize++] = c;
                }
            }
        }
        
        // Upper bound
        int ub = curVal + boundEstimate(cand, candSize, courseCodes, courses, maxH);
        if (ub <= bestVal[0]) {
            return; // poda
        }
        
        // Ordenar candidatos por ratio (bubble sort)
        for (int i = 0; i < candSize - 1; i++) {
            for (int j = 0; j < candSize - 1 - i; j++) {
                Course c1 = getCourseByCode(courses, courseCodes, cand[j]);
                Course c2 = getCourseByCode(courses, courseCodes, cand[j + 1]);
                double r1 = ratio(c1);
                double r2 = ratio(c2);
                if (r1 < r2) {
                    String temp = cand[j];
                    cand[j] = cand[j + 1];
                    cand[j + 1] = temp;
                }
            }
        }
        
        // Greedy: seleccionar máximo factible
        String[] pick = new String[candSize];
        int pickSize = 0;
        int hoursUsed = 0;
        for (int i = 0; i < candSize; i++) {
            Course c = getCourseByCode(courses, courseCodes, cand[i]);
            int h = c.getHours() == null ? 0 : c.getHours();
            if (hoursUsed + h <= maxH) {
                pick[pickSize++] = cand[i];
                hoursUsed += h;
            }
        }
        
        // Rama 1: tomar pick
        String[] nextApproved = new String[approved.length + pickSize];
        int nextApprovedSize = 0;
        for (int i = 0; i < approved.length; i++) {
            nextApproved[nextApprovedSize++] = approved[i];
        }
        for (int i = 0; i < pickSize; i++) {
            nextApproved[nextApprovedSize++] = pick[i];
        }
        
        String[][] nextPlan = new String[S][];
        int nextPlanSize = curPlanSizeRef[0];
        for (int i = 0; i < curPlanSizeRef[0]; i++) {
            if (curPlan[i] != null) {
                nextPlan[i] = new String[curPlan[i].length];
                for (int j = 0; j < curPlan[i].length; j++) {
                    nextPlan[i][j] = curPlan[i][j];
                }
            }
        }
        nextPlan[nextPlanSize] = new String[pickSize];
        for (int i = 0; i < pickSize; i++) {
            nextPlan[nextPlanSize][i] = pick[i];
        }
        nextPlanSize++;
        
        int valAdd = 0;
        for (int i = 0; i < pickSize; i++) {
            Course c = getCourseByCode(courses, courseCodes, pick[i]);
            valAdd += c.getCredits() == null ? 0 : c.getCredits();
        }
        
        int[] nextPlanSizeRef = new int[]{nextPlanSize};
        bnb(sem + 1, S, maxH, nextApproved, courseCodes, courses, prereqs, prereqSizes,
            nextPlan, nextPlanSizeRef, curVal + valAdd, bestVal, bestPlan);
        
        // Rama 2: alternativa mínima
        if (pickSize > 0) {
            String[] alt = new String[]{pick[0]};
            String[] altApproved = new String[approved.length + 1];
            int altApprovedSize = 0;
            for (int i = 0; i < approved.length; i++) {
                altApproved[altApprovedSize++] = approved[i];
            }
            altApproved[altApprovedSize++] = pick[0];
            
            String[][] altPlan = new String[S][];
            int altPlanSize = curPlanSizeRef[0];
            for (int i = 0; i < curPlanSizeRef[0]; i++) {
                if (curPlan[i] != null) {
                    altPlan[i] = new String[curPlan[i].length];
                    for (int j = 0; j < curPlan[i].length; j++) {
                        altPlan[i][j] = curPlan[i][j];
                    }
                }
            }
            altPlan[altPlanSize] = new String[]{pick[0]};
            altPlanSize++;
            
            Course c = getCourseByCode(courses, courseCodes, pick[0]);
            int altVal = c.getCredits() == null ? 0 : c.getCredits();
            int[] altPlanSizeRef = new int[]{altPlanSize};
            bnb(sem + 1, S, maxH, altApproved, courseCodes, courses, prereqs, prereqSizes,
                altPlan, altPlanSizeRef, curVal + altVal, bestVal, bestPlan);
        } else {
            // Cuatri vacío
            String[][] emptyPlan = new String[S][];
            int emptyPlanSize = curPlanSizeRef[0];
            for (int i = 0; i < curPlanSizeRef[0]; i++) {
                if (curPlan[i] != null) {
                    emptyPlan[i] = new String[curPlan[i].length];
                    for (int j = 0; j < curPlan[i].length; j++) {
                        emptyPlan[i][j] = curPlan[i][j];
                    }
                }
            }
            emptyPlan[emptyPlanSize] = new String[0];
            emptyPlanSize++;
            int[] emptyPlanSizeRef = new int[]{emptyPlanSize};
            bnb(sem + 1, S, maxH, approved, courseCodes, courses, prereqs, prereqSizes,
                emptyPlan, emptyPlanSizeRef, curVal, bestVal, bestPlan);
        }
    }

    private Course getCourseByCode(Course[] courses, String[] codes, String code) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i] != null && codes[i].equals(code)) {
                return courses[i];
            }
        }
        return null;
    }

    private double ratio(Course c) {
        if (c == null) return 0.0;
        int cr = c.getCredits() == null ? 0 : c.getCredits();
        int hr = c.getHours() == null ? 1 : c.getHours();
        return hr == 0 ? cr : (double)cr / hr;
    }

    private int boundEstimate(String[] cand, int candSize, String[] courseCodes, Course[] courses, int maxH) {
        // Crear arreglo de cursos candidatos
        Course[] candCourses = new Course[candSize];
        for (int i = 0; i < candSize; i++) {
            candCourses[i] = getCourseByCode(courses, courseCodes, cand[i]);
        }
        
        // Ordenar por ratio descendente
        for (int i = 0; i < candSize - 1; i++) {
            for (int j = 0; j < candSize - 1 - i; j++) {
                double r1 = ratio(candCourses[j]);
                double r2 = ratio(candCourses[j + 1]);
                if (r1 < r2) {
                    Course temp = candCourses[j];
                    candCourses[j] = candCourses[j + 1];
                    candCourses[j + 1] = temp;
                }
            }
        }
        
        int h = 0, v = 0;
        for (int i = 0; i < candSize; i++) {
            Course c = candCourses[i];
            if (c == null) continue;
            int ch = c.getHours() == null ? 0 : c.getHours();
            int cv = c.getCredits() == null ? 0 : c.getCredits();
            if (h + ch <= maxH) {
                h += ch;
                v += cv;
            }
        }
        return v;
    }

    // Utilidades
    private boolean contains(String[] arr, int size, String val) {
        if (val == null) return false;
        for (int i = 0; i < size; i++) {
            if (arr[i] != null && arr[i].equals(val)) {
                return true;
            }
        }
        return false;
    }

    private void removeFromArray(String[] arr, int[] sizeRef, String val) {
        for (int i = 0; i < sizeRef[0]; i++) {
            if (arr[i] != null && arr[i].equals(val)) {
                for (int j = i; j < sizeRef[0] - 1; j++) {
                    arr[j] = arr[j + 1];
                }
                sizeRef[0]--;
                break;
            }
        }
    }
}
