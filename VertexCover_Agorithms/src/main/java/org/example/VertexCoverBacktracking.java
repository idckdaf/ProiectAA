package org.example;
import java.util.*;
public class VertexCoverBacktracking {
    public static final List<Long> lastRunPrepTimings = new ArrayList<>();
    private long start;
    public Set<Integer> findMinimumVertexCover(Graph g) {
        start = System.currentTimeMillis();
        long s = System.nanoTime();
        List<int[]> edges = g.getEdges();
        synchronized(lastRunPrepTimings){ lastRunPrepTimings.add(System.nanoTime()-s); }
        return solve(edges);
    }
    private Set<Integer> solve(List<int[]> edges) {
        if (System.currentTimeMillis() - start > 10000) throw new RuntimeException("Timeout");
        if (edges.isEmpty()) return new HashSet<>();
        int[] e = edges.get(0);
        Set<Integer> s1 = new HashSet<>(Set.of(e[0])); s1.addAll(solve(filter(edges, e[0])));
        Set<Integer> s2 = new HashSet<>(Set.of(e[1])); s2.addAll(solve(filter(edges, e[1])));
        return s1.size() < s2.size() ? s1 : s2;
    }
    private List<int[]> filter(List<int[]> edges, int v) {
        List<int[]> res = new ArrayList<>();
        for(int[] e : edges) if(e[0]!=v && e[1]!=v) res.add(e);
        return res;
    }
}
