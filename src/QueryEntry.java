import java.util.*;

/**
 * Created by apple on 2018/6/17.
 * @author cbq
 */
public class QueryEntry {

    private static final String AND = "and";
    private static final String NOT = "not";
    private static final String OR = "or";

    //    (Brutus OR Caesar) AND NOT (Antony OR Cleopatra)


    public static ArrayList andQuery(ArrayList<Integer> lpos, ArrayList<Integer> rpos){
        ArrayList<Integer> res = new ArrayList();
        for(int i = 0; i < lpos.size(); ++ i){
            for(int j = 0; j < rpos.size(); ++j){
                if(lpos.get(i).equals(rpos.get(j))){
                    res.add(lpos.get(i));
                }
            }
        }
        return res;
    }

    public static ArrayList orQuery(ArrayList<Integer> lpos, ArrayList<Integer> rpos){
        ArrayList<Integer> res = new ArrayList();

        Set<Integer> lset = new HashSet<>(lpos);
        Set<Integer> rset = new HashSet<>(rpos);
        lset.addAll(rset);

        res.addAll(lset);
        return res;
    }

    public static ArrayList notQuery(ArrayList<Integer> lpos, ArrayList<Integer> rpos){
        ArrayList<Integer> res = new ArrayList();
        res.addAll(lpos);
        for(int i = 0; i < lpos.size(); ++ i){
            for(int j = 0; j < rpos.size(); ++j){
                if(lpos.get(i).equals(rpos.get(j))){
                    res.remove(lpos.get(i));
                }
            }
        }
        return res;
    }

    public static LinkedHashMap phraseQuery(LinkedHashMap<Integer, ArrayList<Integer>> leftWord,
                                     LinkedHashMap<Integer, ArrayList<Integer>> rightWord){
        LinkedHashMap<Integer, ArrayList<Integer>> res = new LinkedHashMap<>();

//        if(leftWord.size() < rightWord.size()){
//
//        }
        // key : doc Id
        // value : positions
        for(Map.Entry entry: leftWord.entrySet()){
            if(rightWord.containsKey(entry.getKey())){

                ArrayList<Integer> lpos = (ArrayList<Integer>)entry.getValue();
                ArrayList<Integer> rpos = rightWord.get(entry.getKey());

                ArrayList<Integer> docPos = new ArrayList<>();
                for(int i = 0; i < lpos.size(); ++i){
                    for(int j = 0; j < rpos.size(); ++j){
                        if(lpos.get(i).equals(rpos.get(j) - 1)){
                            docPos.add(lpos.get(i));
                        }
                    }
                }
                if(docPos.size() != 0) {
                    res.put((Integer) entry.getKey(), docPos);
                }
            }
        }
        return  res;
    }

    public static ArrayList<Integer> getDocIds(LinkedHashMap<Integer, ArrayList<Integer>> t){
        ArrayList<Integer> res = new ArrayList();
        for (Map.Entry entry: t.entrySet()) {
            Integer docId = (Integer) entry.getKey();
            res.add(docId);
        }
        return res;
    }

    public static void main(String[] args){

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

        ArrayList<Integer> andRes = QueryEntry.andQuery(lpos, rpos);
        ArrayList<Integer> orRes = QueryEntry.orQuery(lpos, rpos);
        ArrayList<Integer> notRes = QueryEntry.notQuery(lpos, rpos);

        System.out.println("and query result:");
        for(int i = 0; i < andRes.size(); ++i){
            System.out.println("pos = " + andRes.get(i));
        }
        System.out.println("or query result:");
        for(int i = 0; i < orRes.size(); ++i){
            System.out.println("pos = " + orRes.get(i));
        }
        System.out.println("not query result:");
        for(int i = 0; i < notRes.size(); ++i){
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
