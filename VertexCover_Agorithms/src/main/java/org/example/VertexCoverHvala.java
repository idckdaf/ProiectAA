package org.example;

import java.util.*;

public class VertexCoverHvala {

    public static final List<Long> lastRunPrepTimings = new ArrayList<>();

    public Set<Integer> solve(Graph graph) {
        long startPrep = System.nanoTime();

        int maxId = -1;
        for (int v : graph.getVertices()) {
            if (v > maxId)
                maxId = v;
        }
        int arraySize = maxId + 1;

        int[][] adj = new int[arraySize][];
        int[] degrees = new int[arraySize];

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

        synchronized (lastRunPrepTimings) {
            lastRunPrepTimings.add(prepTime);
        }

        BitSet cover1 = solveGreedyMaxDegree(adj, degrees.clone(), arraySize);
        BitSet cover2 = solveMaximalMatching(adj, arraySize);
        BitSet cover3 = solveMinToMin(adj, degrees.clone(), arraySize);

        BitSet bestBitSet = getSmallest(cover1, cover2, cover3);

        Set<Integer> result = new HashSet<>();
        for (int i = bestBitSet.nextSetBit(0); i >= 0; i = bestBitSet.nextSetBit(i + 1)) {
            result.add(i);
        }

        return result;
    }

    private BitSet solveGreedyMaxDegree(int[][] adj, int[] degrees, int N) {
        BitSet cover = new BitSet(N);
        boolean[] removed = new boolean[N];
        int remainingEdges = countEdges(degrees);

        while (remainingEdges > 0) {
            int bestVertex = -1;
            int maxDegree = -1;

            for (int v = 0; v < N; v++) {
                if (!removed[v] && degrees[v] > maxDegree) {
                    maxDegree = degrees[v];
                    bestVertex = v;
                }
            }

            if (bestVertex != -1 && maxDegree > 0) {
                cover.set(bestVertex);
                removeVertex(bestVertex, adj, degrees, removed);
                remainingEdges = countEdges(degrees);
            } else {
                break;
            }
        }
        return cover;
    }

    private BitSet solveMaximalMatching(int[][] adj, int N) {
        BitSet cover = new BitSet(N);
        boolean[] removed = new boolean[N];

        for (int u = 0; u < N; u++) {
            if (removed[u] || adj[u] == null)
                continue;

            for (int v : adj[u]) {
                if (!removed[v]) {
                    cover.set(u);
                    cover.set(v);
                    removed[u] = true;
                    removed[v] = true;
                    break;
                }
            }
        }
        return cover;
    }

    private BitSet solveMinToMin(int[][] adj, int[] degrees, int N) {
        BitSet cover = new BitSet(N);
        boolean[] removed = new boolean[N];
        int remainingEdges = countEdges(degrees);

        while (remainingEdges > 0) {
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

            if (u != -1) {
                cover.set(u);
                removeVertex(u, adj, degrees, removed);
                remainingEdges = countEdges(degrees);
            } else {
                removeVertex(minDegreeV, adj, degrees, removed);
                remainingEdges = countEdges(degrees);
            }
        }
        return cover;
    }

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
