package org.example;

import java.util.*;

public class VertexCoverDFS {

    public static final List<Long> lastRunPrepTimings = new ArrayList<>();

    public Set<Integer> findVertexCover(Graph graph) {
        long startPrep = System.nanoTime();

        int n = graph.getVertexCount();
        BitSet cover = new BitSet(n);
        BitSet visited = new BitSet(n);

        int[][] adj = new int[n][];
        int[] degrees = new int[n];
        for (int v : graph.getVertices()) {
            Set<Integer> neighbors = graph.getNeighbors(v);
            degrees[v] = neighbors.size();
            adj[v] = new int[degrees[v]];
            int i = 0;
            for (int neighbor : neighbors)
                adj[v][i++] = neighbor;
        }

        long endPrep = System.nanoTime();
        synchronized (lastRunPrepTimings) {
            lastRunPrepTimings.add(endPrep - startPrep);
        }

        // deg(1) case
        for (int i = 0; i < n; i++) {
            if (degrees[i] == 1 && !visited.get(i)) {
                int neighbor = adj[i][0];
                cover.set(neighbor);
                visited.set(neighbor);
                visited.set(i);
            }
        }

        for (int i = 0; i < n; i++) {
            if (!visited.get(i) && degrees[i] > 0) {
                iterativeDfs(i, adj, visited, cover);
            }
        }

        prune(cover, adj);

        apply2To1Swap(cover, adj, n);

        return convertToSet(cover);
    }

    private void iterativeDfs(int root, int[][] adj, BitSet visited, BitSet cover) {
        Stack<Integer> stack = new Stack<>();
        stack.push(root);
        visited.set(root);
        BitSet isInternal = new BitSet();
        int[] nextEdgeIdx = new int[adj.length];

        while (!stack.isEmpty()) {
            int u = stack.peek();
            boolean foundChild = false;
            while (nextEdgeIdx[u] < adj[u].length) {
                int v = adj[u][nextEdgeIdx[u]++];
                if (!visited.get(v)) {
                    visited.set(v);
                    isInternal.set(u);
                    stack.push(v);
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                if (isInternal.get(u))
                    cover.set(u);
                stack.pop();
            }
        }
    }

    private void apply2To1Swap(BitSet cover, int[][] adj, int n) {
        BitSet outside = new BitSet(n);
        for (int i = 0; i < n; i++)
            if (!cover.get(i))
                outside.set(i);

        for (int w = outside.nextSetBit(0); w >= 0; w = outside.nextSetBit(w + 1)) {
            List<Integer> coverNeighbors = new ArrayList<>();
            for (int neighbor : adj[w]) {
                if (cover.get(neighbor))
                    coverNeighbors.add(neighbor);
            }

            if (coverNeighbors.size() >= 2) {
                for (int i = 0; i < coverNeighbors.size(); i++) {
                    for (int j = i + 1; j < coverNeighbors.size(); j++) {
                        int u = coverNeighbors.get(i);
                        int v = coverNeighbors.get(j);

                        cover.set(w);
                        if (isRedundant(u, adj, cover)) {
                            cover.clear(u);
                            if (isRedundant(v, adj, cover)) {
                                cover.clear(v);
                                return;
                            }
                            cover.set(u);
                        }
                        cover.clear(w);
                    }
                }
            }
        }
    }

    private boolean isRedundant(int u, int[][] adj, BitSet cover) {
        for (int v : adj[u])
            if (!cover.get(v))
                return false;
        return true;
    }

    private void prune(BitSet cover, int[][] adj) {
        for (int u = cover.nextSetBit(0); u >= 0; u = cover.nextSetBit(u + 1)) {
            if (isRedundant(u, adj, cover))
                cover.clear(u);
        }
    }

    private Set<Integer> convertToSet(BitSet bitSet) {
        Set<Integer> res = new HashSet<>();
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1))
            res.add(i);
        return res;
    }
}