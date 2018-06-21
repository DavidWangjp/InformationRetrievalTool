import java.util.*;

/**
 * @author cbq
 */
public class QueryEntry {

    private static final String AND = "and";
    private static final String NOT = "not";
    private static final String OR = "or";

    //    (Brutus OR Caesar) AND NOT (Antony OR Cleopatra)

    public static List<Integer> andQuery(List<Integer> lpos, List<Integer> rpos) {
        List<Integer> res = new ArrayList<>();
        for (Integer lpo : lpos) {
            for (Integer rpo : rpos) {
                if (lpo.equals(rpo)) {
                    res.add(lpo);
                }
            }
        }
        return res;
    }

    public static List<Integer> orQuery(List<Integer> lpos, List<Integer> rpos) {
        Set<Integer> lset = new HashSet<>(lpos);
        Set<Integer> rset = new HashSet<>(rpos);
        lset.addAll(rset);

        return new ArrayList<>(lset);
    }

    public static List<Integer> notQuery(List<Integer> lpos, List<Integer> rpos) {
        List<Integer> res = new ArrayList<>(lpos);
        for (Integer lpo : lpos) {
            for (Integer rpo : rpos) {
                if (lpo.equals(rpo)) {
                    res.remove(lpo);
                }
            }
        }
        return res;
    }

    public static Map<Integer, ArrayList<Integer>> phraseQuery(LinkedHashMap<Integer, ArrayList<Integer>> leftWord,
                                                                         LinkedHashMap<Integer, ArrayList<Integer>> rightWord) {
        Map<Integer, ArrayList<Integer>> res = new LinkedHashMap<>();

/// TODO: delete code of no use.
//        if(leftWord.size() < rightWord.size()){
//
//        }
        // key : doc Id
        // value : positions
        for (Map.Entry<Integer, ArrayList<Integer>> entry : leftWord.entrySet()) {
            if (rightWord.containsKey(entry.getKey())) {

                List<Integer> lpos = entry.getValue();
                List<Integer> rpos = rightWord.get(entry.getKey());

                ArrayList<Integer> docPos = new ArrayList<>();
                for (Integer lpo : lpos) {
                    for (Integer rpo : rpos) {
                        if (lpo.equals(rpo - 1)) {
                            docPos.add(lpo);
                        }
                    }
                }
                if (docPos.size() != 0) {
                    res.put(entry.getKey(), docPos);
                }
            }
        }
        return res;
    }

    public static List<Integer> getDocIds(List<PairOfDocIdAndPositions> t) {
        List<Integer> docIds = new ArrayList<>();
        if (t != null) {
            for (PairOfDocIdAndPositions pair : t) {
                docIds.add(pair.getDocId());
            }
        }
        return docIds;
    }

    public static void main(String[] args) {

//        LinkedHashMap<Integer, ArrayList<Integer>> res = RetrievalUtil.retrieveWord("grain");
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNext()){
//            String word = scanner.next();
//        }
        //grain board
        String lword = "grain";
        String rword = "board";
        QueryEntry entry = new QueryEntry();
        LinkedHashMap<Integer, ArrayList<Integer>> lres = RetrievalUtil.retrieveWord(lword);
        LinkedHashMap<Integer, ArrayList<Integer>> rres = RetrievalUtil.retrieveWord(rword);
        LinkedHashMap<Integer, ArrayList<Integer>> res = QueryEntry.phraseQuery(lres, rres);

        ArrayList<Integer> lpos = QueryEntry.getDocIds(lres);
        ArrayList<Integer> rpos = QueryEntry.getDocIds(rres);

        List<Integer> andRes = QueryEntry.andQuery(lpos, rpos);
        List<Integer> orRes = QueryEntry.orQuery(lpos, rpos);
        List<Integer> notRes = QueryEntry.notQuery(lpos, rpos);

        System.out.println("and query result:");
        for (int i = 0; i < andRes.size(); ++i) {
            System.out.println("pos = " + andRes.get(i));
        }
        System.out.println("or query result:");
        for (int i = 0; i < orRes.size(); ++i) {
            System.out.println("pos = " + orRes.get(i));
        }
        System.out.println("not query result:");
        for (int i = 0; i < notRes.size(); ++i) {
            System.out.println("pos = " + notRes.get(i));
        }
//        System.out.println("Phrase query for " + lword + " " + rword);
//        for (Map.Entry entry2 : res.entrySet()) {
//            Integer docId = (Integer) entry2.getKey();
//            ArrayList<Integer> positions = (ArrayList<Integer>) entry2.getValue();
//
//            System.out.println("doc" + docId + ":");
//            for (Integer pos:positions) {
//                System.out.println("pos " + pos);
//            }
//            System.out.println();
//        }

    }

}
