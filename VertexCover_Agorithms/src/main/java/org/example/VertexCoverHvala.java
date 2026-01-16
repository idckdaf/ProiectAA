package org.example;

import java.util.*;

public class VertexCoverHvala {

    // Static list to store prep timing results for the last run (in nanoseconds)
    public static final List<Long> lastRunPrepTimings = new ArrayList<>();

    /**
     * Solves the Vertex Cover problem using the "Hvala" ensemble approach.
     * Optimized to use int arrays and BitSet to minimize overhead.
     *
     * @param graph The input graph.
     * @return A Set of integers representing the approximate minimum vertex cover.
     */
    public Set<Integer> solve(Graph graph) {
        long startPrep = System.nanoTime();

        // --- Preprocessing: Convert Graph to optimized structures ---
        int N = graph.getVertexCount();

        // Find max vertex ID to size arrays correctly
        int maxId = -1;
        for (int v : graph.getVertices()) {
            if (v > maxId)
                maxId = v;
        }
        int arraySize = maxId + 1;

        // Adjacency list as int[][]
        int[][] adj = new int[arraySize][];
        // Degree array
        int[] degrees = new int[arraySize];

        // Fill structures
        for (int v : graph.getVertices()) {
            Set<Integer> neighbors = graph.getNeighbors(v);
            degrees[v] = neighbors.size();
            adj[v] = new int[degrees[v]];
            int i = 0;
            for (int neighbor : neighbors) {
                adj[v][i++] = neighbor;
            }
        }

        long endPrep = System.nanoTime();
        long prepTime = endPrep - startPrep;

        // Store prep timing
        synchronized (lastRunPrepTimings) {
            lastRunPrepTimings.add(prepTime);
        }

        // --- Strategy 1: Greedy by Maximum Degree ---
        BitSet cover1 = solveGreedyMaxDegree(adj, degrees.clone(), arraySize);

        // --- Strategy 2: Maximal Matching ---
        BitSet cover2 = solveMaximalMatching(adj, arraySize);

        // --- Strategy 3: Min-to-Min Heuristic ---
        BitSet cover3 = solveMinToMin(adj, degrees.clone(), arraySize);

        // --- Select Best Result ---
        BitSet bestBitSet = getSmallest(cover1, cover2, cover3);

        // Convert back to Set<Integer> for the interface
        Set<Integer> result = new HashSet<>();
        for (int i = bestBitSet.nextSetBit(0); i >= 0; i = bestBitSet.nextSetBit(i + 1)) {
            result.add(i);
        }

        return result;
    }

    // --- Strategy 1: Greedy (Highest Degree) ---
    private BitSet solveGreedyMaxDegree(int[][] adj, int[] degrees, int N) {
        BitSet cover = new BitSet(N);
        boolean[] removed = new boolean[N];
        int remainingEdges = countEdges(degrees); // Helper to track if done

        while (remainingEdges > 0) {
            int bestVertex = -1;
            int maxDegree = -1;

            // Find vertex with current maximum degree
            // Optimization: We could maintain a max-heap or buckets, but for N~20 linear
            // scan is fine.
            for (int v = 0; v < N; v++) {
                if (!removed[v] && degrees[v] > maxDegree) {
                    maxDegree = degrees[v];
                    bestVertex = v;
                }
            }

            if (bestVertex != -1 && maxDegree > 0) {
                cover.set(bestVertex);
                removeVertex(bestVertex, adj, degrees, removed);
                remainingEdges = countEdges(degrees); // Re-check (could be optimized)
            } else {
                break;
            }
        }
        return cover;
    }

    // --- Strategy 2: Maximal Matching ---
    private BitSet solveMaximalMatching(int[][] adj, int N) {
        BitSet cover = new BitSet(N);
        boolean[] removed = new boolean[N];

        // Iterate through all edges
        for (int u = 0; u < N; u++) {
            if (removed[u] || adj[u] == null)
                continue;

            for (int v : adj[u]) {
                if (!removed[v]) {
                    // Found an edge (u, v) where neither is removed
                    cover.set(u);
                    cover.set(v);

                    // "Remove" them
                    removed[u] = true;
                    removed[v] = true;
                    break; // Move to next u
                }
            }
        }
        return cover;
    }

    // --- Strategy 3: Min-to-Min (MtM) Heuristic ---
    private BitSet solveMinToMin(int[][] adj, int[] degrees, int N) {
        BitSet cover = new BitSet(N);
        boolean[] removed = new boolean[N];
        int remainingEdges = countEdges(degrees);

        while (remainingEdges > 0) {
            // 1. Find vertex v with MINIMUM degree > 0
            int minDegreeV = -1;
            int minDegreeVal = Integer.MAX_VALUE;

            for (int v = 0; v < N; v++) {
                if (!removed[v] && degrees[v] > 0 && degrees[v] < minDegreeVal) {
                    minDegreeVal = degrees[v];
                    minDegreeV = v;
                }
            }

            if (minDegreeV == -1)
                break;

            // 2. Find neighbor u of v with MINIMUM degree
            int u = -1;
            int minNeighborDegree = Integer.MAX_VALUE;

            if (adj[minDegreeV] != null) {
                for (int neighbor : adj[minDegreeV]) {
                    if (!removed[neighbor]) {
                        if (degrees[neighbor] < minNeighborDegree) {
                            minNeighborDegree = degrees[neighbor];
                            u = neighbor;
                        }
                    }
                }
            }

            // 3. Add u to cover and remove
            if (u != -1) {
                cover.set(u);
                removeVertex(u, adj, degrees, removed);
                remainingEdges = countEdges(degrees);
            } else {
                // If v has no active neighbors (shouldn't happen if degree > 0), remove v to
                // avoid infinite loop
                removeVertex(minDegreeV, adj, degrees, removed);
                remainingEdges = countEdges(degrees);
            }
        }
        return cover;
    }

    // --- Helper Methods ---

    private void removeVertex(int v, int[][] adj, int[] degrees, boolean[] removed) {
        removed[v] = true;
        degrees[v] = 0; // Effectively 0 for selection purposes

        if (adj[v] != null) {
            for (int neighbor : adj[v]) {
                if (!removed[neighbor]) {
                    degrees[neighbor]--;
                }
            }
        }
    }

    private int countEdges(int[] degrees) {
        int sum = 0;
        for (int d : degrees)
            sum += d;
        return sum / 2;
    }

    private BitSet getSmallest(BitSet... sets) {
        BitSet best = null;
        for (BitSet s : sets) {
            if (best == null || s.cardinality() < best.cardinality()) {
                best = s;
            }
        }
        return best;
    }
}
