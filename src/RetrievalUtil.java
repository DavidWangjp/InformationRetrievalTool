import java.util.*;

import static java.lang.Math.log;
import static java.lang.Math.log10;

public class RetrievalUtil {

    public RetrievalUtil() {
    }

    public static LinkedHashMap retrieveWord(String query) {
        LinkedHashMap<Integer, ArrayList<Integer>> res = new LinkedHashMap<>();

        TreeMap<Integer, Double> docScore = new TreeMap<>();
        InvertedIndex.init();

        String term = InvertedIndex.getTerm(query);
        // stop words
        if (term == null)
            return null;

        // look up in term dictionary
        if (InvertedIndex.termDictionary.containsKey(term)) {
            double idf = log10(1.0*InvertedIndex.FILE_SIZE/InvertedIndex.termDictionary.get(term));

            if (InvertedIndex.invertedIndex.containsKey(term)) {
                TreeMap<Integer, ArrayList<Integer>> index = InvertedIndex.invertedIndex.get(term);

                for(Map.Entry entry: index.entrySet()) {
                    Integer docId = (Integer) entry.getKey();
                    ArrayList<Integer> positions = (ArrayList<Integer>)entry.getValue();
                    // compute tf-idf

                    double score = idf *(1.0+log10(positions.size()));

                    score /= InvertedIndex.docLen.get(docId);
                    if (!docScore.containsKey(docId)) {
                        docScore.put(docId, score);
                    } else {
                        docScore.put(docId, docScore.get(docId)+score);
                    }
                }

                // sort tree_map according to score
                class scoreComparator implements Comparator<Map.Entry<Integer, Double>>
                {
                    @Override
                    public int compare(Map.Entry<Integer,Double>m, Map.Entry<Integer,Double> n)
                    {
                        return n.getValue() - m.getValue()>0?1:-1;
                    }
                }

                List<Map.Entry<Integer, Double>> entryArrayList = new ArrayList<>(docScore.entrySet());
                Collections.sort(entryArrayList, new scoreComparator());

                for (Map.Entry entry: entryArrayList) {
                    res.put((Integer)entry.getKey(), index.get(entry.getKey()));
                    System.out.print("doc"+entry.getKey() + " "+"score:");
                    System.out.println(entry.getValue());
                }
            }
        }

        return res;
    }

}
