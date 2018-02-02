// Class WordNet
// Author: Nathaniel Hayden
// Purpose: Construct a digraph of nouns based on WordNet lexicon
// Based on assignment 1 of Princeton Algorithms II course

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class WordNet {
    
    private final HashMap<String, ArrayList<Integer>> words;
    private final HashMap<Integer, String> synsets;
    private final SAP s;
    
    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException();
        In inSynsets = new In(synsets),
                inHypernyms = new In(hypernyms);
        
        // create words map and synsets map
        HashMap<String, ArrayList<Integer>> wordsToIDs = new HashMap<>();
        HashMap<Integer, String> idsToSynsets = new HashMap<>();
        while (inSynsets.hasNextLine()) {
            String[] fields = inSynsets.readLine().split(",");
            int id = Integer.parseInt(fields[0]);
            String synset = fields[1];
            String[] nouns = synset.split(" ");
            for (String noun : nouns) {
                ArrayList<Integer> cur = wordsToIDs.get(noun);
                if (cur == null)
                    wordsToIDs.put(noun, new ArrayList<>(Arrays.asList(id)));
                else
                    cur.add(id);
            }
            idsToSynsets.put(id, synset);
            
        }
        this.words = wordsToIDs;
        this.synsets = idsToSynsets;
        
        // create digraph
        String[] hypernymLines = inHypernyms.readAllLines();
        Digraph g = new Digraph(idsToSynsets.size());
        for (String line : hypernymLines) {
            String[] fields = line.split(",");
            int id = Integer.parseInt(fields[0]);
            for (int i = 1; i < fields.length; i++) {
                g.addEdge(id, Integer.parseInt(fields[i]));
            }
        }
        if (!isRootedDAG(g))
            throw new IllegalArgumentException();
        
        s = new SAP(g);
    }
    
    // check if a rooted directed acyclic graph
    private boolean isRootedDAG(Digraph g) {
        if ((new DirectedCycle(g)).hasCycle())
            return false;
        int root = -1;
        for (int i = 0; i <  g.V(); i++) {
            if (g.outdegree(i) == 0) {
                if (root != -1)
                    return false;
                root = i;
            }
        }
        return true;
    }
    
    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return words.keySet();
    }
    
    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException();
        return words.keySet().contains(word);
    }
    
    // distance between nounA and nounB
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null || !isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException();
        return s.length(words.get(nounA), words.get(nounB));
    }
    
    // a synset (second field of synsets.txt) that is the common ancestor
    // of nounA and nounB in a shortest ancestral path
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null)
            throw new IllegalArgumentException();
        return synsets.get(s.ancestor(words.get(nounA), words.get(nounB)));
    }
    
    public static void main(String[] args) {
        WordNet wordnet = new WordNet("wordnet/synsets.txt",
                "wordnet/hypernyms.txt");
//        wordnet.distance("bird", "worm");
        StdOut.println(wordnet.sap("bird", "worm"));
//        wordnet.distance("Black_Plague", "black_marlin");
    }

}
