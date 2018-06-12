//import java.util.TreeMap;
//
//public class RetrievalUtil {
//    private InvertedIndex invertedIndex;
//    private Stemmer stemmer;
//
//    public RetrievalUtil() {
//        invertedIndex.init();
//    }
//
//    public String retrieval(String query) {
//        String[] words = query.trim().split("(,\\s)|(\\.\\s)|\"| ");
//        for (String word : words)
//        {
//            if (word == null||word.isEmpty())
//                continue;
//
//            String token = getToken(word.trim().toLowerCase());
//
//            // same stemming for query
//            stemmer.add(word.toCharArray(), word.length());
//            stemmer.stem();
//            String term = stemmer.toString();
//
//            if (invertedIndex.containsKey(term))
//            {
//                TreeMap<Integer, InvertedIndex.IndexInfo> index = invertedIndex.get(term);
//                if (index.containsKey(docId))
//                    invertedIndex.get(term).get(docId).positions.add(position);
//                else
//                {
//                    InvertedIndex.IndexInfo info = new InvertedIndex.IndexInfo();
//                    info.positions.add(position);
//                    index.put(docId, info);
//                }
//            }
//        }
//
//
//}
