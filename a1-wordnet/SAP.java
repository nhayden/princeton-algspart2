// Class SAP
// Author: Nathaniel Hayden
// Purpose: Useds WordNet digraph to find shortest ancestral path (SAP)
//   between two vertices to a common ancestor
// Based on assignment 1 of Princeton Algorithms II course

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;

public class SAP {
    private final Digraph G;
    
    public SAP(Digraph G) {
        if (new DirectedCycle(G).hasCycle()) {
            throw new IllegalArgumentException("digraph contains cycle");
        }
        this.G = new Digraph(G);
    }
    
    private class SAPResult {
        private final int ancestor;
        private final int length;
        public SAPResult(Digraph G, Iterable<Integer> v, Iterable<Integer> w) {
            BreadthFirstDirectedPaths pathsv = new BreadthFirstDirectedPaths(G, v);
            BreadthFirstDirectedPaths pathsw = new BreadthFirstDirectedPaths(G, w);
            
            int shortestLength = Integer.MAX_VALUE;
            int curLength;
            int ancestor = -1;
            for (int cur = 0; cur < G.V(); cur++) {
                if (pathsv.hasPathTo(cur) && pathsw.hasPathTo(cur)) {
                    curLength = pathsv.distTo(cur) + pathsw.distTo(cur);
                    if (curLength < shortestLength) {
                        ancestor = cur;
                        shortestLength = curLength;
                    }
                }
            }
            this.ancestor = ancestor;
            if (shortestLength == Integer.MAX_VALUE) this.length = -1;
            else this.length = shortestLength;
        }
    }
    
    private Iterable<Integer> makeIterable(int val) {
        return new ArrayList<>(Arrays.asList(val));
    }
    
    public int ancestor(int v, int w) {
        return ancestor(makeIterable(v), makeIterable(w));
    }
    public int length(int v, int w) {
        return length(makeIterable(v), makeIterable(w));
    }
    
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        return new SAPResult(this.G, v, w).length;
    }
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        return new SAPResult(this.G, v, w).ancestor;
    }
    
    public static void main(String[] args) {
        String fname = "1";
        In in = new In("wordnet/digraph" + fname + ".txt");
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }

    }

}
