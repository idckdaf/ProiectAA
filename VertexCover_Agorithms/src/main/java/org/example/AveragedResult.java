package org.example;

import java.util.List;

/**
 * JSON model for storing averaged/aggregated results.
 */
public record AveragedResult(
        String testName,
        String algorithm,
        int vertexCount,
        int edgeCount,
        int iterations,
        double avgCoverSize,
        double minCoverSize,
        double maxCoverSize,
        double avgTimeMs,
        double minTimeMs,
        double maxTimeMs,
        double stdDevTimeMs,
        double avgPrepTimeMs,
        double avgMemoryBytes,
        boolean allValid,
        String timestamp) {
    /**
     * Calculate aggregated statistics from a list of iteration results.
     */
    public static AveragedResult fromIterations(List<IterationResult> results) {
        if (results == null || results.isEmpty())
            return null;

        IterationResult first = results.get(0);
        double sumCover = 0, sumTime = 0, sumMemory = 0;
        double minCover = Double.MAX_VALUE, maxCover = Double.MIN_VALUE;
        double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE;
        boolean allValid = true;

        for (IterationResult iter : results) {
            sumCover += iter.coverSize();
            sumTime += iter.timeMs();
            sumMemory += iter.memoryBytes();
            minCover = Math.min(minCover, iter.coverSize());
            maxCover = Math.max(maxCover, iter.coverSize());
            minTime = Math.min(minTime, iter.timeMs());
            maxTime = Math.max(maxTime, iter.timeMs());
            allValid &= iter.valid();
        }

        double avgTime = sumTime / results.size();
        double avgPrepTime = results.stream().mapToDouble(IterationResult::prepTimeMs).average().orElse(0.0);
        double avgMemory = sumMemory / results.size();
        double sumSquaredDiff = 0;
        for (IterationResult iter : results) {
            sumSquaredDiff += Math.pow(iter.timeMs() - avgTime, 2);
        }

        return new AveragedResult(
                first.testName(), first.algorithm(), first.vertexCount(), first.edgeCount(),
                results.size(), sumCover / results.size(), minCover, maxCover,
                avgTime, minTime, maxTime, Math.sqrt(sumSquaredDiff / results.size()),
                avgPrepTime,
                avgMemory,
                allValid, java.time.Instant.now().toString());
    }
}
