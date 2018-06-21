import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Math.log10;

public class RetrievalUtil {
    private static boolean initialized = false;

    public RetrievalUtil() {
    }

    public static List<PairOfDocIdAndPositions> retrieveWord(String query) {
        initInvertedIndex();

        List<PairOfDocIdAndPositions> res = new ArrayList<>();
        Map<Integer, Double> docScore = new TreeMap<>();

        String term = InvertedIndex.getTerm(query);

        // Return null in case of stop words.
        if (term == null) {
            return null;
        }

        // Look up in the term dictionary.
        if (InvertedIndex.termDictionary.containsKey(term)) {
            double idf = log10(1.0 * InvertedIndex.FILE_SIZE / InvertedIndex.termDictionary.get(term));

            if (InvertedIndex.invertedIndex.containsKey(term)) {
                Map<Integer, ArrayList<Integer>> index = InvertedIndex.invertedIndex.get(term);

                computeScores(docScore, idf, index);

                List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(docScore.entrySet());
                entryList.sort((o1, o2) -> o2.getValue() - o1.getValue() > 0 ? 1 : -1);

                for (Map.Entry<Integer, Double> entry : entryList) {
                    res.add(new PairOfDocIdAndPositions(entry.getKey(), index.get(entry.getKey())));
                    System.out.print("doc" + entry.getKey() + " " + "score:");
                    System.out.println(entry.getValue());
                }
            }
        }

        return res;
    }

    public static List<PairOfDocIdAndPositions> retrieveTopKWord(String query, int k) {
        initInvertedIndex();

        List<PairOfDocIdAndPositions> res = new ArrayList<>();
        Map<Integer, Double> docScore = new TreeMap<>();

        String term = InvertedIndex.getTerm(query);

        // Return null in case of stop words.
        if (term == null) {
            return null;
        }

        // Look up in the term dictionary.
        if (InvertedIndex.termDictionary.containsKey(term)) {
            double idf = log10(1.0 * InvertedIndex.FILE_SIZE / InvertedIndex.termDictionary.get(term));

            if (InvertedIndex.invertedIndex.containsKey(term)) {
                Map<Integer, ArrayList<Integer>> index = InvertedIndex.invertedIndex.get(term);

                computeScores(docScore, idf, index);

                PriorityQueue<Map.Entry<Integer, Double>> entryMaxHeap = new PriorityQueue<>(docScore.size(),
                        (o1, o2) -> Double.compare(o2.getValue(), o1.getValue()));
                entryMaxHeap.addAll(docScore.entrySet());

                for (int i = 0; i < k && i < entryMaxHeap.size(); i++) {
                    Map.Entry<Integer, Double> entry = entryMaxHeap.poll();
                    assert entry != null;
                    res.add(new PairOfDocIdAndPositions(entry.getKey(), index.get(entry.getKey())));
                    System.out.print("doc" + entry.getKey() + " " + "score:");
                    System.out.println(entry.getValue());
                }
            }
        }

        return res;
    }

    private static void computeScores(Map<Integer, Double> docScore, double idf, Map<Integer, ArrayList<Integer>> index) {
        for (Map.Entry<Integer, ArrayList<Integer>> entry : index.entrySet()) {
            Integer docId = entry.getKey();
            List<Integer> positions = entry.getValue();

            // Compute tf-idf.
            double score = idf * (1.0 + log10(positions.size()));
            score /= InvertedIndex.docLen.get(docId);
            if (!docScore.containsKey(docId)) {
                docScore.put(docId, score);
            } else {
                docScore.put(docId, docScore.get(docId) + score);
            }
        }
    }

    private static void initInvertedIndex() {
        if (!initialized) {
            InvertedIndex.init();
            initialized = true;
        }
    }
}

class PairOfDocIdAndPositions {
    private int docId;
    private List<Integer> positions;

    PairOfDocIdAndPositions(int docId, List<Integer> positions) {
        this.docId = docId;
        this.positions = positions;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }
}
