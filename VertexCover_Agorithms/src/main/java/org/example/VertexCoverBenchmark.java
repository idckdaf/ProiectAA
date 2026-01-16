package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VertexCoverBenchmark {

    @Param("")
    public String testFileName;

    private Graph graph;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        // Load graph from the file path provided by the parameter
        if (testFileName != null && !testFileName.isEmpty()) {
            graph = Main.loadGraph(testFileName);
        }
    }

    @Benchmark
    public void backtracking(Blackhole bh) {
        VertexCoverBacktracking algo = new VertexCoverBacktracking();
        Set<Integer> result = algo.findMinimumVertexCover(graph);
        bh.consume(result);
    }

    @Benchmark
    public void dfs(Blackhole bh) {
        VertexCoverDFS algo = new VertexCoverDFS();
        Set<Integer> result = algo.findVertexCover(graph);
        bh.consume(result);
    }

    @Benchmark
    public void hvala(Blackhole bh) {
        VertexCoverHvala algo = new VertexCoverHvala();
        Set<Integer> result = algo.solve(graph);
        bh.consume(result);
    }
}
