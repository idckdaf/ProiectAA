import random
import math
from typing import Set, Tuple, List

def write_graph(filename: str, N: int, edges: Set[Tuple[int, int]]):
    """Write graph to file in specified format"""
    with open(filename, 'w') as f:
        f.write(f"{N} {len(edges)}\n")
        for x, y in sorted(edges):
            f.write(f"{x} {y}\n")

def random_edges(N: int, M: int) -> Set[Tuple[int, int]]:
    """Generate M random unique edges"""
    edges = set()
    max_edges = N * (N - 1) // 2
    M = min(M, max_edges)
    
    while len(edges) < M:
        x, y = random.sample(range(1, N + 1), 2)
        edges.add((min(x, y), max(x, y)))
    return edges

# 1. RANDOM GRAPHS (6 graphs)
def generate_random_graphs():
    densities = [0.10, 0.25, 0.40, 0.60, 0.70, 0.80]
    graphs = []
    
    for i, density in enumerate(densities, 1):
        N = random.randint(15, 20)
        max_edges = N * (N - 1) // 2
        M = int(density * max_edges)
        edges = random_edges(N, M)
        filename = f"random_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={M}")
    
    return graphs

# 2. BIPARTITE GRAPHS (3 graphs)
def generate_bipartite_graphs():
    densities = [0.20, 0.50, 0.80]
    graphs = []
    
    for density in densities:
        N = random.randint(15, 20)
        set1_size = N // 2
        set2_size = N - set1_size
        max_edges = set1_size * set2_size
        M = int(density * max_edges)
        
        edges = set()
        while len(edges) < M:
            x = random.randint(1, set1_size)
            y = random.randint(set1_size + 1, N)
            edges.add((min(x, y), max(x, y)))
        
        filename = f"bipartite_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={M}")
    
    return graphs

# 3. COMPLETE GRAPH (1 graph)
def generate_complete_graph():
    N = random.randint(15, 20)
    edges = set()
    for i in range(1, N + 1):
        for j in range(i + 1, N + 1):
            edges.add((i, j))
    
    filename = "complete.txt"
    write_graph(filename, N, edges)
    print(f"Generated {filename}: N={N}, M={len(edges)}")
    return [filename]

# 4. TREE GRAPHS (3 graphs)
def generate_tree_graphs():
    bushiness = [0.20, 0.50, 0.80]  # Controls branching factor
    graphs = []
    
    for bush in bushiness:
        N = random.randint(15, 20)
        edges = set()
        
        # Start with node 1 as root
        unconnected = list(range(2, N + 1))
        random.shuffle(unconnected)
        
        connected = [1]
        
        while unconnected:
            # Choose parent based on bushiness
            if random.random() < bush:
                parent = random.choice(connected[-max(1, len(connected)//2):])
            else:
                parent = random.choice(connected)
            
            child = unconnected.pop()
            edges.add((min(parent, child), max(parent, child)))
            connected.append(child)
        
        filename = f"tree_{int(bush*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 5. CYCLE GRAPHS (3 graphs)
def generate_cycle_graphs():
    extra_densities = [0.20, 0.50, 0.80]
    graphs = []
    
    for density in extra_densities:
        N = random.randint(15, 20)
        edges = set()
        
        # Create basic cycle
        for i in range(1, N + 1):
            next_node = (i % N) + 1
            edges.add((min(i, next_node), max(i, next_node)))
        
        # Add extra edges
        max_extra = N * (N - 3) // 2  # Max edges beyond cycle
        extra = int(density * max_extra)
        
        while len(edges) < N + extra:
            x, y = random.sample(range(1, N + 1), 2)
            if abs(x - y) > 1 and not (x == 1 and y == N) and not (x == N and y == 1):
                edges.add((min(x, y), max(x, y)))
        
        filename = f"cycle_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 6. STAR GRAPHS (3 graphs)
def generate_star_graphs():
    outer_densities = [0.20, 0.50, 0.80]
    graphs = []
    
    for density in outer_densities:
        N = random.randint(15, 20)
        edges = set()
        center = 1
        
        # Connect all to center
        for i in range(2, N + 1):
            edges.add((center, i))
        
        # Add edges among outer nodes
        max_outer = (N - 1) * (N - 2) // 2
        outer_edges = int(density * max_outer)
        
        while len(edges) < (N - 1) + outer_edges:
            x, y = random.sample(range(2, N + 1), 2)
            edges.add((min(x, y), max(x, y)))
        
        filename = f"star_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 7. GRID GRAPHS (3 graphs)
def generate_grid_graphs():
    extra_densities = [0.20, 0.50, 0.80]
    graphs = []
    
    for density in extra_densities:
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
        
        # Add grid edges
        for (r, c), node in node_map.items():
            if (r, c+1) in node_map:
                edges.add((min(node, node_map[(r, c+1)]), max(node, node_map[(r, c+1)])))
            if (r+1, c) in node_map:
                edges.add((min(node, node_map[(r+1, c)]), max(node, node_map[(r+1, c)])))
        
        # Add extra edges
        base_edges = len(edges)
        max_extra = N * (N - 1) // 2 - base_edges
        extra = int(density * max_extra)
        
        edges.update(random_edges(N, base_edges + extra) - edges)
        
        filename = f"grid_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 8. CLIQUE-BASED GRAPHS (3 graphs)
def generate_clique_graphs():
    inter_densities = [0.20, 0.50, 0.80]
    graphs = []
    
    for density in inter_densities:
        N = random.randint(15, 20)
        num_cliques = random.randint(3, 4)
        clique_sizes = [N // num_cliques] * num_cliques
        for i in range(N % num_cliques):
            clique_sizes[i] += 1
        
        edges = set()
        cliques = []
        node = 1
        
        # Create cliques
        for size in clique_sizes:
            clique = list(range(node, node + size))
            cliques.append(clique)
            for i in clique:
                for j in clique:
                    if i < j:
                        edges.add((i, j))
            node += size
        
        # Add inter-clique edges
        max_inter = sum(len(c1) * len(c2) for i, c1 in enumerate(cliques) 
                       for c2 in cliques[i+1:])
        inter_edges = int(density * max_inter)
        
        added = 0
        while added < inter_edges:
            c1, c2 = random.sample(cliques, 2)
            x, y = random.choice(c1), random.choice(c2)
            edge = (min(x, y), max(x, y))
            if edge not in edges:
                edges.add(edge)
                added += 1
        
        filename = f"clique_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 9. SCALE-FREE GRAPHS (3 graphs)
def generate_scale_free_graphs():
    attachment_strengths = [0.20, 0.50, 0.80]
    graphs = []
    
    for strength in attachment_strengths:
        N = random.randint(15, 20)
        m = max(2, int(strength * 5))  # Edges per new node
        
        edges = set()
        degrees = [0] * (N + 1)
        
        # Start with small complete graph
        for i in range(1, m + 2):
            for j in range(i + 1, m + 2):
                edges.add((i, j))
                degrees[i] += 1
                degrees[j] += 1
        
        # Add remaining nodes with preferential attachment
        for new_node in range(m + 2, N + 1):
            targets = set()
            existing = list(range(1, new_node))
            
            while len(targets) < min(m, len(existing)):
                # Preferential attachment
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
        
        filename = f"scale_free_{int(strength*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 10. PLANAR GRAPHS (3 graphs)
def generate_planar_graphs():
    densities = [0.20, 0.50, 0.80]
    graphs = []
    
    for density in densities:
        N = random.randint(15, 20)
        max_planar = 3 * N - 6
        M = int(density * max_planar)
        
        # Start with tree to ensure connectivity
        edges = set()
        for i in range(2, N + 1):
            edges.add((min(i-1, i), max(i-1, i)))
        
        # Add more edges (simple heuristic, not guaranteed planar)
        while len(edges) < M:
            x, y = random.sample(range(1, N + 1), 2)
            edges.add((min(x, y), max(x, y)))
        
        filename = f"planar_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}")
    
    return graphs

# 11. DENSE GRAPHS (2 graphs)
def generate_dense_graphs():
    densities = [0.85, 0.95]
    graphs = []
    
    for density in densities:
        N = random.randint(15, 20)
        max_edges = N * (N - 1) // 2
        M = int(density * max_edges)
        edges = random_edges(N, M)
        
        filename = f"dense_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={M}")
    
    return graphs

# 12. SPARSE GRAPHS (2 graphs)
def generate_sparse_graphs():
    densities = [0.05, 0.15]
    graphs = []
    
    for density in densities:
        N = random.randint(15, 20)
        max_edges = N * (N - 1) // 2
        M = max(N - 1, int(density * max_edges))  # At least connected
        edges = random_edges(N, M)
        
        filename = f"sparse_{int(density*100)}pct.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={M}")
    
    return graphs

# 13. REGULAR GRAPHS (3 graphs)
def generate_regular_graphs():
    degrees = [2, 4, 6]
    graphs = []
    
    for target_degree in degrees:
        N = random.randint(16, 20)
        if N % 2 == 1:
            N += 1
        
        if target_degree >= N:
            target_degree = N - 1
        
        edges = set()
        adj = [set() for _ in range(N + 1)]
        
        # Build edges with degree constraints
        max_attempts = N * N * 10
        attempts = 0
        
        nodes = list(range(1, N + 1))
        random.shuffle(nodes)
        
        # First pass: try to get everyone to target degree
        for node in nodes:
            while len(adj[node]) < target_degree and attempts < max_attempts:
                attempts += 1
                # Find nodes with lowest degree
                candidates = [n for n in range(1, N + 1) 
                            if n != node and n not in adj[node] 
                            and len(adj[n]) < target_degree]
                
                if not candidates:
                    break
                
                # Pick candidate with lowest degree
                other = min(candidates, key=lambda x: len(adj[x]))
                
                adj[node].add(other)
                adj[other].add(node)
                edges.add((min(node, other), max(node, other)))
        
        filename = f"regular_deg{target_degree}.txt"
        write_graph(filename, N, edges)
        graphs.append(filename)
        print(f"Generated {filename}: N={N}, M={len(edges)}, avg_degree={2*len(edges)/N:.1f}")
    
    return graphs

# MAIN GENERATION
if __name__ == "__main__":
    random.seed(42)  # For reproducibility
    
    all_graphs = []
    
    print("=== GENERATING VERTEX COVER TEST GRAPHS ===\n")
    
    all_graphs.extend(generate_random_graphs())
    all_graphs.extend(generate_bipartite_graphs())
    all_graphs.extend(generate_complete_graph())
    all_graphs.extend(generate_tree_graphs())
    all_graphs.extend(generate_cycle_graphs())
    all_graphs.extend(generate_star_graphs())
    all_graphs.extend(generate_grid_graphs())
    all_graphs.extend(generate_clique_graphs())
    all_graphs.extend(generate_scale_free_graphs())
    all_graphs.extend(generate_planar_graphs())
    all_graphs.extend(generate_dense_graphs())
    all_graphs.extend(generate_sparse_graphs())
    all_graphs.extend(generate_regular_graphs())
    
    print(f"\n=== TOTAL: {len(all_graphs)} graphs generated ===")
    print("\nGenerated files:")
    for g in all_graphs:
        print(f"  - {g}")