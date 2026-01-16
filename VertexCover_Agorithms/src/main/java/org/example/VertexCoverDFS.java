package org.example;

import java.util.*;

/**
 * DFS Tree 2-Approximation Vertex Cover Algorithm.
 * 
 * Strategy:
 * 1. Compute a Depth First Search (DFS) tree of the graph.
 * 2. Select all non-leaf vertices (internal nodes) of the DFS tree.
 * 
 * Guarantee:
 * - The set of internal nodes forms a valid Vertex Cover.
 * - The size is guaranteed to be <= 2 * OPT.
 * 
 * Proof:
 * In a DFS tree, all graph edges are either tree edges or back edges (no cross
 * edges).
 * Every edge is incident to an ancestor. Leaves only have edges to ancestors
 * (which are internal nodes).
 * Thus, picking all internal nodes covers all edges.
 * It is a known result that |Internal Nodes| <= 2 * OPT.
 */
public class VertexCoverDFS {

    public static final List<Long> lastRunPrepTimings = new ArrayList<>();

    public Set<Integer> findVertexCover(Graph graph) {
        long startPrep = System.nanoTime();
        Set<Integer> cover = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> leaves = new HashSet<>();
        long endPrep = System.nanoTime();

        synchronized (lastRunPrepTimings) {
            lastRunPrepTimings.add(endPrep - startPrep);
        }

        // We need to handle disconnected components
        for (int v : graph.getVertices()) {
            if (!visited.contains(v)) {
                dfs(graph, v, visited, leaves, cover);
            }
        }

        return cover;
    }

    private void dfs(Graph graph, int u, Set<Integer> visited, Set<Integer> leaves, Set<Integer> cover) {
        visited.add(u);
        boolean isLeaf = true;

        for (int v : graph.getNeighbors(u)) {
            if (!visited.contains(v)) {
                isLeaf = false;
                dfs(graph, v, visited, leaves, cover);
            }
        }

        if (isLeaf) {
            leaves.add(u);
        } else {
            cover.add(u);
        }
    }
}
