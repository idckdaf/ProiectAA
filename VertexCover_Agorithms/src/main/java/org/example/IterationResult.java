package org.example;

import java.util.List;

/**
 * JSON model for storing individual iteration results.
 */
public record IterationResult(
        String testName,
        String algorithm,
        int iteration,
        int vertexCount,
        int edgeCount,
        int coverSize,
        List<Integer> coverVertices,
        double timeMs,
        double prepTimeMs,
        long memoryBytes,
        boolean valid,
        String timestamp) {
}
