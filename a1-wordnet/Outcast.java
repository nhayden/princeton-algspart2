// Class Outcast
// Author: Nathaniel Hayden
// Purpose: Given a group of nouns in WordNet digraph, computer the outlier
//   in terms of relatedness
// Based on assignment 1 of Princeton Algorithms II course

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet w;
    
    public Outcast(WordNet wordnet) {
        if (wordnet == null) throw new IllegalArgumentException();
        this.w = wordnet;
    }
    
    public String outcast(String[] nouns) {
        if (nouns == null) throw new IllegalArgumentException();
        
        int maxDist = 0;
        String maxNoun = null;
        for (int i = 0; i < nouns.length; i++) {
            int curDist = 0;
            for (int j = 0; j < nouns.length; j++) {
                if (j == i) continue;
                curDist += w.distance(nouns[i], nouns[j]);
            }
            if (curDist > maxDist) {
                maxDist = curDist;
                maxNoun = nouns[i];
            }
        }
        return maxNoun;
    }

    public static void main(String[] args) {
        Outcast outcast = new Outcast(new WordNet("wordnet/synsets.txt",
                "wordnet/hypernyms.txt"));
        String[] nouns = (new In("wordnet/outcast11.txt")).readAllStrings();
        StdOut.println(outcast.outcast(nouns));

    }

}
