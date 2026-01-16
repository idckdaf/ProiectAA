package org.example;

import java.util.*;

/**
 * 2-Approximation Vertex Cover algorithm using Maximal Matching.
 * Guaranteed <= 2 * OPT.
 */
public class VertexCover2Approx {

    public static final List<Long> lastRunPrepTimings = new ArrayList<>();

    public Set<Integer> findVertexCover(Graph graph) {
        long startPrep = System.nanoTime();
        Set<Integer> cover = new HashSet<>();
        Set<Integer> matched = new HashSet<>();
        long endPrep = System.nanoTime();

        synchronized (lastRunPrepTimings) {
            lastRunPrepTimings.add(endPrep - startPrep);
        }

        for (int[] edge : graph.getEdges()) {
            int u = edge[0];
            int v = edge[1];

            if (!matched.contains(u) && !matched.contains(v)) {
                matched.add(u);
                matched.add(v);
                cover.add(u);
                cover.add(v);
            }
        }
        return cover;
    }
}
