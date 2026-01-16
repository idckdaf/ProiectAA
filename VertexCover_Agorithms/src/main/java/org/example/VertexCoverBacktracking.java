package org.example;

import java.util.*;

/**
 * Optimized Exact Vertex Cover using Recursive Backtracking.
 * 
 * Optimizations:
 * 1. Internal integer-based graph representation (no object overhead)
 * 2. Vertex-based branching: v vs N(v)
 * 3. Degree-1 reduction (always pick neighbor)
 * 4. Maximal matching lower bound pruning
 * 5. BitSet state management (no graph copying)
 */
public class VertexCoverBacktracking {

    private int bestSize;
    private BitSet bestCover;
    private long startTime;
    private static final long TIME_LIMIT_MS = 30000; // Safety timeout

    // Static list to store prep timing results for the last run (in nanoseconds)
    public static final List<Long> lastRunPrepTimings = new ArrayList<>();

    // Reusable buffer for matching to avoid allocations in recursion
    private boolean[] matchingBuffer;

    public Set<Integer> findMinimumVertexCover(Graph graph) {
        long startPrep = System.nanoTime();

        // 1. Convert to internal representation
        int n = graph.getVertexCount();
        int[][] adj = new int[n][];
        int[] degree = new int[n];

        // Map original IDs to 0..n-1
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        Map<Integer, Integer> toInternal = new HashMap<>();
        int[] toOriginal = new int[n];

        for (int i = 0; i < n; i++) {
            int v = vertices.get(i);
            toInternal.put(v, i);
            toOriginal[i] = v;
        }

        for (int i = 0; i < n; i++) {
            int u = toOriginal[i];
            Set<Integer> neighbors = graph.getNeighbors(u);
            adj[i] = new int[neighbors.size()];
            int idx = 0;
            for (int v : neighbors) {
                adj[i][idx++] = toInternal.get(v);
            }
            degree[i] = neighbors.size();
        }

        // 2. Initialize state
        bestSize = n + 1;
        bestCover = null;
        BitSet currentCover = new BitSet(n);
        boolean[] active = new boolean[n];
        Arrays.fill(active, true);
        matchingBuffer = new boolean[n];

        // Initial upper bound using DFS 2-approximation
        VertexCoverDFS dfsApprox = new VertexCoverDFS();
        Set<Integer> initialSol = dfsApprox.findVertexCover(graph);
        bestSize = initialSol.size();
        bestCover = new BitSet(n);
        for (int v : initialSol)
            bestCover.set(toInternal.get(v));

        long endPrep = System.nanoTime();
        synchronized (lastRunPrepTimings) {
            lastRunPrepTimings.add(endPrep - startPrep);
        }

        startTime = System.currentTimeMillis();

        // 3. Start recursion
        solve(adj, degree, active, currentCover, 0, n);

        // 4. Convert back to original IDs
        Set<Integer> result = new HashSet<>();
        for (int i = bestCover.nextSetBit(0); i >= 0; i = bestCover.nextSetBit(i + 1)) {
            result.add(toOriginal[i]);
        }
        return result;
    }

    private void solve(int[][] adj, int[] degree, boolean[] active, BitSet currentCover, int currentSize, int n) {
        // Pruning: if current >= best, stop
        if (currentSize >= bestSize)
            return;

        // Timeout check
        if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS)
            return;

        // Find max degree vertex among active vertices
        int maxDeg = -1;
        int v = -1;
        int activeEdges = 0;

        for (int i = 0; i < n; i++) {
            if (active[i]) {
                if (degree[i] > maxDeg) {
                    maxDeg = degree[i];
                    v = i;
                }
                activeEdges += degree[i];
            }
        }
        activeEdges /= 2;

        // Base case: no edges left
        if (activeEdges == 0) {
            if (currentSize < bestSize) {
                bestSize = currentSize;
                bestCover = (BitSet) currentCover.clone();
            }
            return;
        }

        // Lower bound pruning: Maximal Matching
        // We need at least 'matchingSize' more vertices
        // Simple greedy matching on active edges
        int matchingSize = getMatchingSize(adj, active, n);
        if (currentSize + matchingSize >= bestSize)
            return;

        // Degree-1 Reduction
        if (maxDeg == 1) {
            // Find a degree-1 vertex
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (active[i] && degree[i] == 1) {
                    u = i;
                    break;
                }
            }

            // Find its neighbor
            int neighbor = -1;
            for (int w : adj[u]) {
                if (active[w]) {
                    neighbor = w;
                    break;
                }
            }

            // Always pick neighbor
            if (neighbor != -1) {
                currentCover.set(neighbor);
                toggleVertex(neighbor, adj, degree, active, false);
                solve(adj, degree, active, currentCover, currentSize + 1, n);
                toggleVertex(neighbor, adj, degree, active, true); // Backtrack
                currentCover.clear(neighbor);
            }
            return;
        }

        // Branching: v vs N(v)

        // Branch 1: Include v
        currentCover.set(v);
        toggleVertex(v, adj, degree, active, false);
        solve(adj, degree, active, currentCover, currentSize + 1, n);
        toggleVertex(v, adj, degree, active, true); // Backtrack
        currentCover.clear(v);

        // Branch 2: Exclude v -> must include all neighbors
        // Only valid if currentSize + degree[v] < bestSize
        if (currentSize + degree[v] < bestSize) {
            // Store which neighbors we actually added to the cover
            // (some might already be in active=false state if we were more complex,
            // but here 'active' is our main state)

            // We can use a simple array to track which neighbors were added to backtrack
            // correctly
            int[] addedNeighbors = new int[degree[v]];
            int count = 0;

            for (int neighbor : adj[v]) {
                if (active[neighbor]) {
                    addedNeighbors[count++] = neighbor;
                    currentCover.set(neighbor);
                }
            }

            // Remove all neighbors and v
            for (int i = 0; i < count; i++) {
                toggleVertex(addedNeighbors[i], adj, degree, active, false);
            }
            boolean vWasActive = active[v];
            if (vWasActive)
                toggleVertex(v, adj, degree, active, false);

            solve(adj, degree, active, currentCover, currentSize + count, n);

            // Backtrack
            if (vWasActive)
                toggleVertex(v, adj, degree, active, true);
            for (int i = 0; i < count; i++) {
                int neighbor = addedNeighbors[i];
                toggleVertex(neighbor, adj, degree, active, true);
                currentCover.clear(neighbor);
            }
        }
    }

    private void toggleVertex(int v, int[][] adj, int[] degree, boolean[] active, boolean state) {
        active[v] = state;
        for (int neighbor : adj[v]) {
            if (active[neighbor]) {
                if (state)
                    degree[neighbor]++; // Re-adding v
                else
                    degree[neighbor]--; // Removing v
            }
        }
    }

    // Simple greedy maximal matching for lower bound
    private int getMatchingSize(int[][] adj, boolean[] active, int n) {
        int size = 0;
        Arrays.fill(matchingBuffer, false);

        for (int i = 0; i < n; i++) {
            if (active[i] && !matchingBuffer[i]) {
                for (int neighbor : adj[i]) {
                    if (active[neighbor] && !matchingBuffer[neighbor]) {
                        matchingBuffer[i] = true;
                        matchingBuffer[neighbor] = true;
                        size++;
                        break;
                    }
                }
            }
        }
        return size;
    }
}
