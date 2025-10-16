package com.tp.PlanificadorMat.servicio;

import com.tp.PlanificadorMat.modelo.Course;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.tp.PlanificadorMat.repositorio.CourseRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Greedy (selección de materias por cuatri),
 * DP (knapsack),
 * Backtracking (todas las rutas factibles a target),
 * Branch & Bound (mejor plan a N cuatris)
 */
@Service
public class ScheduleService {
    private final CourseRepository repo;
    public ScheduleService(CourseRepository repo){ this.repo = repo; }

    // Disponibles ahora según aprobadas (delegando en Cypher)
    public Flux<Course> availableNow(List<String> approved) {
        return repo.availableWith(approved==null? List.of() : approved);
    }

    // --- Greedy: llenar cuatrimestre con maxHours, ordenando por valor/horas ---
    public Flux<Course> greedySchedule(List<String> approved, String value, int maxHours) {
        return availableNow(approved).collectList().flatMapMany(list -> {
            Comparator<Course> cmp = Comparator.comparingDouble(c -> -score(c, value)); // desc
            list.sort(cmp);
            int sumH = 0;
            List<Course> pick = new ArrayList<>();
            for (Course c : list) {
                int h = c.getHours()==null? 0 : c.getHours();
                if (sumH + h <= maxHours) { pick.add(c); sumH+=h; }
            }
            return Flux.fromIterable(pick);
        });
    }
    private double score(Course c, String value) {
        value = value==null? "credits" : value;
        return switch (value) {
            case "difficulty" -> c.getDifficulty()==null? 0 : (6 - c.getDifficulty()); // menos difícil = mayor score
            case "hours"      -> c.getHours()==null? 0 : (10.0 - c.getHours()); // menos horas = mayor score
            default           -> c.getCredits()==null? 0 : c.getCredits(); // créditos
        };
    }

    // --- DP Knapsack: maximizar valor con constraint de horas ---
    public Mono<List<Course>> dpKnapsack(List<String> approved, String value, int maxHours) {
        return availableNow(approved).collectList().map(items -> {
            int n = items.size();
            int[] W = new int[n];
            int[] V = new int[n];
            for (int i=0;i<n;i++){
                W[i] = Optional.ofNullable(items.get(i).getHours()).orElse(0);
                V[i] = (int)Math.round(score(items.get(i), value));
            }
            int[][] dp = new int[n+1][maxHours+1];
            boolean[][] take = new boolean[n+1][maxHours+1];
            for (int i=1;i<=n;i++){
                for (int w=0;w<=maxHours;w++){
                    int notake = dp[i-1][w];
                    int takev = (W[i-1]<=w)? dp[i-1][w-W[i-1]] + V[i-1] : Integer.MIN_VALUE/4;
                    if (takev > notake) { dp[i][w] = takev; take[i][w]=true; }
                    else dp[i][w] = notake;
                }
            }
            // reconstrucción
            List<Course> pick = new ArrayList<>();
            int w = maxHours;
            for (int i=n;i>=1;i--){
                if (take[i][w]) { pick.add(items.get(i-1)); w -= W[i-1]; }
            }
            Collections.reverse(pick);
            return pick;
        });
    }

    // --- Backtracking: rutas desde 'from' a 'to' respetando prereqs (sobre REQUIRES) ---
    public Mono<List<List<String>>> backtrackingPaths(String from, String to, int maxDepth) {
        return repo.allCourses().collectList().map(all -> {
            Map<String, Set<String>> adj = new HashMap<>();
            for (Course c : all) {
                adj.putIfAbsent(c.getCode(), new HashSet<>());
                if (c.getPrereqs()!=null) for (Course p : c.getPrereqs()) adj.get(c.getCode()).add(p.getCode());
            }
            List<List<String>> res = new ArrayList<>();
            backtrack(from, to, maxDepth, new ArrayList<>(), new HashSet<>(), adj, res);
            return res;
        });
    }
    private void backtrack(String u, String to, int md, List<String> path, Set<String> vis,
                           Map<String, Set<String>> adj, List<List<String>> out) {
        if (md < 0 || u==null || !adj.containsKey(u)) return;
        path.add(u); vis.add(u);
        if (u.equals(to)) out.add(new ArrayList<>(path));
        else for (String v : adj.getOrDefault(u, Set.of()))
            if (!vis.contains(v)) backtrack(v, to, md-1, path, vis, adj, out);
        path.remove(path.size()-1);
        vis.remove(u);
    }

    // --- Branch & Bound: mejor plan a N cuatrimestres (maximiza créditos) ---
    public Mono<List<List<String>>> branchAndBoundPlan(List<String> approved, int semesters, int maxHours) {
        return repo.allCourses().collectList().map(all -> {
            Map<String, Course> map = all.stream().collect(Collectors.toMap(Course::getCode, c->c));
            Map<String, Set<String>> prereqs = new HashMap<>();
            for (Course c : all) {
                prereqs.putIfAbsent(c.getCode(), new HashSet<>());
                if (c.getPrereqs()!=null) for (Course p : c.getPrereqs()) prereqs.get(c.getCode()).add(p.getCode());
            }
            Set<String> approvedSet = new HashSet<>(Optional.ofNullable(approved).orElse(List.of()));
            List<List<String>> bestPlan = new ArrayList<>();
            int[] bestValue = { -1 };

            // BnB
            bnb(0, semesters, maxHours, approvedSet, prereqs, map, new ArrayList<>(), 0, bestValue, bestPlan);
            return bestPlan;
        });
    }

    private void bnb(int sem, int S, int maxH, Set<String> approved,
                     Map<String, Set<String>> prereqs, Map<String, Course> map,
                     List<List<String>> curPlan, int curVal,
                     int[] bestVal, List<List<String>> bestPlan) {

        if (sem == S) {
            if (curVal > bestVal[0]) { bestVal[0] = curVal; bestPlan.clear(); bestPlan.addAll(curPlan.stream().map(ArrayList::new).toList()); }
            return;
        }
        // candidatos disponibles
        List<String> cand = prereqs.keySet().stream()
                .filter(c -> !approved.contains(c))
                .filter(c -> approved.containsAll(prereqs.getOrDefault(c, Set.of())))
                .toList();

        // upper bound (cota): créditos actuales + suma topK créditos (apróx) limitada por horas
        int ub = curVal + boundEstimate(cand, map, maxH);
        if (ub <= bestVal[0]) return; // poda

        // elegir subconjunto factible por horas (heurística: ordenar por créditos/horas desc)
        List<String> ordered = new ArrayList<>(cand);
        ordered.sort((a,b)->Double.compare(ratio(map.get(b)), ratio(map.get(a))));

        // explorar por ramas: incluimos de a uno (simple, para no explotar combinaciones)
        // versión simple: greedy parcial por cuatri + recursión
        int hoursUsed = 0;
        List<String> pick = new ArrayList<>();
        for (String c : ordered) {
            int h = Optional.ofNullable(map.get(c).getHours()).orElse(0);
            if (hoursUsed + h <= maxH) { pick.add(c); hoursUsed += h; }
        }
        // rama 1: tomar pick
        List<List<String>> nextPlan = new ArrayList<>(curPlan);
        nextPlan.add(pick);
        Set<String> nextApproved = new HashSet<>(approved); nextApproved.addAll(pick);
        int valAdd = pick.stream().mapToInt(x -> Optional.ofNullable(map.get(x).getCredits()).orElse(0)).sum();
        bnb(sem+1, S, maxH, nextApproved, prereqs, map, nextPlan, curVal+valAdd, bestVal, bestPlan);

        // rama 2: alternativa mínima (tomar menos materias este cuatri)
        if (!pick.isEmpty()) {
            List<String> alt = new ArrayList<>();
            alt.add(pick.get(0));
            List<List<String>> altPlan = new ArrayList<>(curPlan);
            altPlan.add(alt);
            Set<String> altApproved = new HashSet<>(approved); altApproved.addAll(alt);
            int altVal = Optional.ofNullable(map.get(alt.get(0)).getCredits()).orElse(0);
            bnb(sem+1, S, maxH, altApproved, prereqs, map, altPlan, curVal+altVal, bestVal, bestPlan);
        } else {
            // no hay cursables -> cuatri vacío
            List<List<String>> emptyPlan = new ArrayList<>(curPlan);
            emptyPlan.add(List.of());
            bnb(sem+1, S, maxH, approved, prereqs, map, emptyPlan, curVal, bestVal, bestPlan);
        }
    }

    private double ratio(Course c) {
        int cr = Optional.ofNullable(c.getCredits()).orElse(0);
        int hr = Optional.ofNullable(c.getHours()).orElse(1);
        return hr==0? cr : (double)cr/hr;
    }

    private int boundEstimate(List<String> cand, Map<String, Course> map, int maxH) {
        List<Course> list = cand.stream().map(map::get).filter(Objects::nonNull).toList();
        list = new ArrayList<>(list);
        list.sort((a,b)->Double.compare(ratio(b), ratio(a)));
        int h=0, v=0;
        for (Course c : list) {
            int ch = Optional.ofNullable(c.getHours()).orElse(0);
            int cv = Optional.ofNullable(c.getCredits()).orElse(0);
            if (h + ch <= maxH) { h+=ch; v+=cv; }
        }
        return v;
    }
}
