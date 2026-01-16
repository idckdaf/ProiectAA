package org.example;

import java.util.*;

/**
 * Undirected graph
 */
public class Graph {
    private final Map<Integer, Set<Integer>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(int v) {
        adjacencyList.putIfAbsent(v, new HashSet<>());
    }

    public void addEdge(int u, int v) {
        addVertex(u);
        addVertex(v);
        adjacencyList.get(u).add(v);
        adjacencyList.get(v).add(u);
    }

    public Set<Integer> getNeighbors(int v) {
        return adjacencyList.getOrDefault(v, Collections.emptySet());
    }

    public Set<Integer> getVertices() {
        return adjacencyList.keySet();
    }

    public List<int[]> getEdges() {
        List<int[]> edges = new ArrayList<>();
        for (int u : adjacencyList.keySet()) {
            for (int v : adjacencyList.get(u)) {
                if (u < v)
                    edges.add(new int[] { u, v });
            }
        }
        return edges;
    }

    public int getDegree(int v) {
        return adjacencyList.getOrDefault(v, Collections.emptySet()).size();
    }

    public void removeEdge(int u, int v) {
        if (adjacencyList.containsKey(u))
            adjacencyList.get(u).remove(v);
        if (adjacencyList.containsKey(v))
            adjacencyList.get(v).remove(u);
    }

    public boolean hasNoEdges() {
        return adjacencyList.values().stream().allMatch(Set::isEmpty);
    }

    public int getVertexCount() {
        return adjacencyList.size();
    }

    public int getEdgeCount() {
        return adjacencyList.values().stream().mapToInt(Set::size).sum() / 2;
    }

    public Graph copy() {
        Graph copy = new Graph();
        for (int v : adjacencyList.keySet()) {
            copy.adjacencyList.put(v, new HashSet<>(adjacencyList.get(v)));
        }
        return copy;
    }
}
