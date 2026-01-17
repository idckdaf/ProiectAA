import random
import math
import os
from typing import Set, Tuple

def write_graph(filename, N, edges):
    with open(filename, 'w') as f:
        f.write(f"{N} {len(edges)}\n")
        for x, y in sorted(edges):
            f.write(f"{x} {y}\n")

def random_edges(N, M):
    edges = set()
    max_edges = N * (N - 1) // 2
    M = min(M, max_edges)
    
    while len(edges) < M:
        x, y = random.sample(range(1, N + 1), 2)
        edges.add((min(x, y), max(x, y)))
    return edges

def gen_random(densities=[0.10, 0.25, 0.40, 0.60, 0.70, 0.80]):
    graphs = []
    for density in densities:
        N = random.randint(15, 20)
        M = int(density * N * (N - 1) // 2)
        edges = random_edges(N, M)
        name = f"random_{int(density*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_bipartite(densities=[0.20, 0.50, 0.80]):
    graphs = []
    for d in densities:
        N = random.randint(15, 20)
        s1, s2 = N // 2, N - N // 2
        M = int(d * s1 * s2)
        
        edges = set()
        while len(edges) < M:
            x = random.randint(1, s1)
            y = random.randint(s1 + 1, N)
            edges.add((min(x, y), max(x, y)))
        
        name = f"bipartite_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_complete():
    N = random.randint(15, 20)
    edges = {(i, j) for i in range(1, N + 1) for j in range(i + 1, N + 1)}
    write_graph("complete.txt", N, edges)
    return ["complete.txt"]

def gen_tree(bushiness=[0.20, 0.50, 0.80]):
    graphs = []
    for bush in bushiness:
        N = random.randint(15, 20)
        edges = set()
        unconnected = list(range(2, N + 1))
        random.shuffle(unconnected)
        connected = [1]
        
        while unconnected:
            if random.random() < bush:
                parent = random.choice(connected[-max(1, len(connected)//2):])
            else:
                parent = random.choice(connected)
            child = unconnected.pop()
            edges.add((min(parent, child), max(parent, child)))
            connected.append(child)
        
        name = f"tree_{int(bush*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_cycle(extra=[0.20, 0.50, 0.80]):
    graphs = []
    for d in extra:
        N = random.randint(15, 20)
        edges = {(min(i, (i % N) + 1), max(i, (i % N) + 1)) for i in range(1, N + 1)}
        
        max_extra = N * (N - 3) // 2
        target = N + int(d * max_extra)
        
        while len(edges) < target:
            x, y = random.sample(range(1, N + 1), 2)
            if abs(x - y) > 1 and not (x == 1 and y == N):
                edges.add((min(x, y), max(x, y)))
        
        name = f"cycle_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_star(outer=[0.20, 0.50, 0.80]):
    graphs = []
    for d in outer:
        N = random.randint(15, 20)
        edges = {(1, i) for i in range(2, N + 1)}
        
        max_outer = (N - 1) * (N - 2) // 2
        target = (N - 1) + int(d * max_outer)
        
        while len(edges) < target:
            x, y = random.sample(range(2, N + 1), 2)
            edges.add((min(x, y), max(x, y)))
        
        name = f"star_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_grid(extra=[0.20, 0.50, 0.80]):
    graphs = []
    for d in extra:
        N = random.randint(15, 20)
        rows = int(math.sqrt(N))
        cols = (N + rows - 1) // rows
        
        edges = set()
        node_map = {}
        node_id = 1
        
        for r in range(rows):
            for c in range(cols):
                if node_id <= N:
                    node_map[(r, c)] = node_id
                    node_id += 1
        
        for (r, c), node in node_map.items():
            if (r, c+1) in node_map:
                edges.add((min(node, node_map[(r, c+1)]), max(node, node_map[(r, c+1)])))
            if (r+1, c) in node_map:
                edges.add((min(node, node_map[(r+1, c)]), max(node, node_map[(r+1, c)])))
        
        base = len(edges)
        target = base + int(d * (N * (N - 1) // 2 - base))
        edges.update(random_edges(N, target) - edges)
        
        name = f"grid_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_clique(inter=[0.20, 0.50, 0.80]):
    graphs = []
    for d in inter:
        N = random.randint(15, 20)
        num_cliques = random.randint(3, 4)
        sizes = [N // num_cliques] * num_cliques
        for i in range(N % num_cliques):
            sizes[i] += 1
        
        edges = set()
        cliques = []
        node = 1
        
        for size in sizes:
            clique = list(range(node, node + size))
            cliques.append(clique)
            for i in clique:
                for j in clique:
                    if i < j:
                        edges.add((i, j))
            node += size
        
        max_inter = sum(len(c1) * len(c2) for i, c1 in enumerate(cliques) 
                       for c2 in cliques[i+1:])
        target = int(d * max_inter)
        
        added = 0
        while added < target:
            c1, c2 = random.sample(cliques, 2)
            x, y = random.choice(c1), random.choice(c2)
            edge = (min(x, y), max(x, y))
            if edge not in edges:
                edges.add(edge)
                added += 1
        
        name = f"clique_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_scale_free(strengths=[0.20, 0.50, 0.80]):
    graphs = []
    for strength in strengths:
        N = random.randint(15, 20)
        m = max(2, int(strength * 5))
        
        edges = set()
        degrees = [0] * (N + 1)
        
        for i in range(1, m + 2):
            for j in range(i + 1, m + 2):
                edges.add((i, j))
                degrees[i] += 1
                degrees[j] += 1
        
        for new_node in range(m + 2, N + 1):
            targets = set()
            existing = list(range(1, new_node))
            
            while len(targets) < min(m, len(existing)):
                if random.random() < strength and sum(degrees[1:new_node]) > 0:
                    probs = [degrees[i] for i in existing]
                    total = sum(probs)
                    if total > 0:
                        r = random.uniform(0, total)
                        cumsum = 0
                        for i, p in enumerate(probs):
                            cumsum += p
                            if cumsum >= r:
                                targets.add(existing[i])
                                break
                else:
                    targets.add(random.choice(existing))
            
            for target in targets:
                edges.add((min(new_node, target), max(new_node, target)))
                degrees[new_node] += 1
                degrees[target] += 1
        
        name = f"scale_free_{int(strength*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_planar(densities=[0.20, 0.50, 0.80]):
    graphs = []
    for d in densities:
        N = random.randint(15, 20)
        M = int(d * (3 * N - 6))
        
        edges = {(i-1, i) for i in range(2, N + 1)}
        
        while len(edges) < M:
            x, y = random.sample(range(1, N + 1), 2)
            edges.add((min(x, y), max(x, y)))
        
        name = f"planar_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_dense(densities=[0.85, 0.95]):
    graphs = []
    for d in densities:
        N = random.randint(15, 20)
        M = int(d * N * (N - 1) // 2)
        edges = random_edges(N, M)
        name = f"dense_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_sparse(densities=[0.05, 0.15]):
    graphs = []
    for d in densities:
        N = random.randint(15, 20)
        M = max(N - 1, int(d * N * (N - 1) // 2))
        edges = random_edges(N, M)
        name = f"sparse_{int(d*100)}pct.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_regular(degrees=[2, 4, 6]):
    graphs = []
    for target_deg in degrees:
        N = random.randint(16, 20)
        if N % 2 == 1:
            N += 1
        if target_deg >= N:
            target_deg = N - 1
        
        edges = set()
        adj = [set() for _ in range(N + 1)]
        
        nodes = list(range(1, N + 1))
        random.shuffle(nodes)
        
        for node in nodes:
            while len(adj[node]) < target_deg:
                candidates = [n for n in range(1, N + 1) 
                            if n != node and n not in adj[node] 
                            and len(adj[n]) < target_deg]
                if not candidates:
                    break
                other = min(candidates, key=lambda x: len(adj[x]))
                adj[node].add(other)
                adj[other].add(node)
                edges.add((min(node, other), max(node, other)))
        
        name = f"regular_deg{target_deg}.txt"
        write_graph(name, N, edges)
        graphs.append(name)
    return graphs

def gen_large_random():
    graphs = []
    node_counts = [30, 40, 50, 60, 70, 80, 100, 120, 150]
    
    for n in node_counts:
        m_sparse = int(n * math.log2(n))
        m_dense = int(n * n / 4)
        m_medium = (m_sparse + m_dense) // 2
        
        for m, label in [(m_sparse, "sparse"), (m_medium, "medium"), (m_dense, "dense")]:
            edges = set()
            while len(edges) < m:
                u, v = random.randint(0, n-1), random.randint(0, n-1)
                if u != v:
                    edges.add(tuple(sorted((u, v))))
            
            name = f"random_n{n}_{label}.txt"
            with open(name, 'w') as f:
                f.write(f"{n} {m}\n")
                for u, v in edges:
                    f.write(f"{u} {v}\n")
            graphs.append(name)
    
    return graphs

if __name__ == "__main__":
    random.seed(42)
    
    test_dir = "src/main/resources/tests"
    os.makedirs(test_dir, exist_ok=True)
    os.chdir(test_dir)
    
    all_graphs = []
    all_graphs.extend(gen_random())
    all_graphs.extend(gen_bipartite())
    all_graphs.extend(gen_complete())
    all_graphs.extend(gen_tree())
    all_graphs.extend(gen_cycle())
    all_graphs.extend(gen_star())
    all_graphs.extend(gen_grid())
    all_graphs.extend(gen_clique())
    all_graphs.extend(gen_scale_free())
    all_graphs.extend(gen_planar())
    all_graphs.extend(gen_dense())
    all_graphs.extend(gen_sparse())
    all_graphs.extend(gen_regular())
    all_graphs.extend(gen_large_random())
    
    print(f"Generated {len(all_graphs)} test graphs")
