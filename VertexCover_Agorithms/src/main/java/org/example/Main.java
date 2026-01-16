package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final String TESTS_DIR = "src/main/resources/tests";
    private static final String RESULTS_DIR = "src/main/resources/results";
    private static final String PURE_RESULTS_DIR = RESULTS_DIR + "/pure_results";
    private static final String AVERAGED_DIR = RESULTS_DIR + "/averaged";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=".repeat(60));
            System.out.println("VERTEX COVER ALGORITHMS - JMH BENCHMARK RUNNER");
            System.out.println("=".repeat(60));
            System.out.println("\nChoose an algorithm to benchmark:");
            System.out.println("  1. Backtracking (Optimal)");
            System.out.println("  2. DFS Tree 2-Approximation");
            System.out.println("  3. Hvala Ensemble Heuristic");
            System.out.println("  4. Run All Algorithms\n");
            System.out.print("Enter your choice (1-4): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice < 1 || choice > 4) {
                    System.out.println("Invalid choice.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                return;
            }

            System.out.print("How many JMH measurement iterations (max 100): ");
            int iterations;
            try {
                iterations = Math.max(1, Math.min(100, Integer.parseInt(scanner.nextLine().trim())));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using 5 iterations.");
                iterations = 5;
            }

            System.out.println("\nRunning JMH benchmarks with " + iterations + " measurement iteration(s)...");
            System.out.println("(3 warmup iterations will be performed first)\n");

            ensureDirectories();

            List<File> testFiles = getTestFiles();
            if (testFiles.isEmpty()) {
                System.out.println("No test files found in " + TESTS_DIR);
                return;
            }
            System.out.println("Found " + testFiles.size() + " test file(s): "
                    + testFiles.stream().map(File::getName).collect(Collectors.joining(", ")));

            List<String> methodsToRun = new ArrayList<>();
            if (choice == 1 || choice == 4)
                methodsToRun.add("backtracking");
            if (choice == 2 || choice == 4)
                methodsToRun.add("dfs");
            if (choice == 3 || choice == 4)
                methodsToRun.add("hvala");

            try {
                runJMHBenchmarks(methodsToRun, testFiles, iterations);
            } catch (RunnerException e) {
                System.err.println("Error running benchmarks: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("All JMH benchmark results saved to:");
            System.out.println("  Pure results: " + PURE_RESULTS_DIR);
            System.out.println("  Averaged results: " + AVERAGED_DIR);
        }
    }

    private static void runJMHBenchmarks(List<String> methods, List<File> testFiles, int iterations)
            throws RunnerException {
        for (String method : methods) {
            String algorithmName = mapMethodToName(method);
            String displayName = mapMethodToDisplay(method);

            List<AveragedResult> allAveragedResults = new ArrayList<>();

            // We run JMH for each test file individually to maintain our specific output
            // structure
            // and to avoid mixing results in a single JMH run report.
            for (File testFile : testFiles) {
                String testName = testFile.getName().replace(".txt", "");
                System.out.println("  Benchmarking: " + testFile.getName());

                // Clear prep timings for the algorithm being benchmarked
                switch (method) {
                    case "hvala" -> VertexCoverHvala.lastRunPrepTimings.clear();
                    case "dfs" -> VertexCoverDFS.lastRunPrepTimings.clear();
                    case "approx2" -> VertexCover2Approx.lastRunPrepTimings.clear();
                    case "backtracking" -> VertexCoverBacktracking.lastRunPrepTimings.clear();
                }

                Options opt = new OptionsBuilder()
                        .include(VertexCoverBenchmark.class.getSimpleName() + "." + method)
                        .param("testFileName", testFile.getAbsolutePath())
                        .mode(org.openjdk.jmh.annotations.Mode.SingleShotTime)
                        .forks(0) // Disable forking to avoid ClassNotFoundException in exec:java
                        .warmupIterations(3)
                        .measurementIterations(iterations)
                        .shouldDoGC(true)
                        .addProfiler(org.openjdk.jmh.profile.GCProfiler.class)
                        .build();

                Collection<RunResult> runResults = new Runner(opt).run();

                if (!runResults.isEmpty()) {
                    RunResult result = runResults.iterator().next();
                    // Let's modify processAndSaveResults to return the AveragedResult
                    AveragedResult avg = processAndSaveResults(result, algorithmName, testName,
                            testFile.getAbsolutePath(), iterations);
                    allAveragedResults.add(avg);
                }
            }
            writeJsonFile(AVERAGED_DIR + "/" + algorithmName + "/summary.json", allAveragedResults);
            System.out.println("  Results saved for " + displayName);
        }

    }

    private static AveragedResult processAndSaveResults(RunResult result, String algorithmName, String testName,
            String graphPath, int expectedIterations) {
        // Extract raw iteration times from JMH results
        List<Double> times = new ArrayList<>();
        for (var benchmarkResult : result.getBenchmarkResults()) {
            for (var iterationResult : benchmarkResult.getIterationResults()) {
                // Convert to milliseconds (JMH returns values in the OutputTimeUnit, which is
                // MILLISECONDS)
                times.add(iterationResult.getPrimaryResult().getScore());
            }
        }

        // Extract memory allocation (bytes/op)
        // GCProfiler returns "gc.alloc.rate.norm" as a secondary result
        long memoryBytes = 0;
        var secondaryResults = result.getSecondaryResults();
        if (secondaryResults.containsKey("gc.alloc.rate.norm")) {
            // The score is usually the average allocation rate
            memoryBytes = (long) secondaryResults.get("gc.alloc.rate.norm").getScore();
        }

        List<IterationResult> iterationResults = new ArrayList<>();
        Graph graph = null;
        try {
            graph = loadGraph(graphPath);
        } catch (IOException e) {
            System.err.println("Failed to reload graph for metadata: " + e.getMessage());
            return null;
        }

        // We need to run the algorithm once to get the cover size and validity for the
        // report
        // JMH doesn't return the return value of the method.
        Set<Integer> cover = runAlgorithmOnce(algorithmName, graph);
        boolean valid = isValidCover(graph, cover);
        int coverSize = cover.size();
        List<Integer> coverVertices = cover.stream().sorted().collect(Collectors.toList());

        for (int i = 0; i < times.size(); i++) {
            double prepTimeMs = 0;

            List<Long> prepTimings = switch (algorithmName) {
                case "hvala" -> VertexCoverHvala.lastRunPrepTimings;
                case "dfs" -> VertexCoverDFS.lastRunPrepTimings;
                case "approx2" -> VertexCover2Approx.lastRunPrepTimings;
                case "backtracking" -> VertexCoverBacktracking.lastRunPrepTimings;
                default -> null;
            };

            if (prepTimings != null) {
                int totalRecorded = prepTimings.size();
                int startIndex = totalRecorded - expectedIterations;
                if (startIndex >= 0 && (startIndex + i) < totalRecorded) {
                    prepTimeMs = prepTimings.get(startIndex + i) / 1_000_000.0;
                }
            }

            iterationResults.add(new IterationResult(
                    testName, algorithmName, i + 1, graph.getVertexCount(), graph.getEdgeCount(),
                    coverSize, coverVertices,
                    times.get(i), prepTimeMs, memoryBytes, valid, Instant.now().toString()));
        }
        writeJsonFile(PURE_RESULTS_DIR + "/" + algorithmName + "/" + testName + "_iterations.json", iterationResults);

        AveragedResult averaged = AveragedResult.fromIterations(iterationResults);
        writeJsonFile(AVERAGED_DIR + "/" + algorithmName + "/" + testName + "_averaged.json", averaged);

        System.out.printf("    Cover size: %.0f | Avg time: %.4f ms | Std dev: %.4f ms%n",
                averaged.avgCoverSize(), averaged.avgTimeMs(), averaged.stdDevTimeMs());

        return averaged;
    }

    private static String mapMethodToName(String method) {
        return switch (method) {
            case "backtracking" -> "backtracking";
            case "dfs" -> "dfs";
            case "hvala" -> "hvala";
            default -> method;
        };
    }

    private static String mapMethodToDisplay(String method) {
        return switch (method) {
            case "backtracking" -> "Backtracking (Optimal)";
            case "dfs" -> "DFS Tree 2-Approximation";
            case "hvala" -> "Hvala Ensemble Heuristic";
            default -> method;
        };
    }

    private static void ensureDirectories() {
        try {
            for (String type : List.of(PURE_RESULTS_DIR, AVERAGED_DIR)) {
                for (String algo : List.of("backtracking", "dfs", "hvala")) {
                    Files.createDirectories(Paths.get(type, algo));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not create directories: " + e.getMessage());
        }
    }

    private static List<File> getTestFiles() {
        File testsDir = new File(TESTS_DIR);
        if (testsDir.exists() && testsDir.isDirectory()) {
            File[] files = testsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                return Arrays.stream(files)
                        .sorted(Comparator.comparing(File::getName))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private static Set<Integer> runAlgorithmOnce(String algorithmName, Graph graph) {
        return switch (algorithmName) {
            case "backtracking" -> new VertexCoverBacktracking().findMinimumVertexCover(graph);
            case "dfs" -> new VertexCoverDFS().findVertexCover(graph);
            case "hvala" -> new VertexCoverHvala().solve(graph);
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        };
    }

    private static void writeJsonFile(String path, Object data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            writer.print(gson.toJson(data));
        } catch (IOException e) {
            System.err.println("Could not write to file: " + path);
        }
    }

    private static boolean isValidCover(Graph graph, Set<Integer> cover) {
        for (int[] edge : graph.getEdges()) {
            if (!cover.contains(edge[0]) && !cover.contains(edge[1]))
                return false;
        }
        return true;
    }

    public static Graph loadGraph(String filename) throws IOException {
        Graph graph = new Graph();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String[] parts = reader.readLine().trim().split("\\s+");
            int n = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            for (int i = 0; i < n; i++)
                graph.addVertex(i);
            for (int i = 0; i < m; i++) {
                String[] edge = reader.readLine().trim().split("\\s+");
                graph.addEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]));
            }
        }
        return graph;
    }
}
