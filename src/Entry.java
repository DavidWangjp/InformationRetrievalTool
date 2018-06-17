import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by apple on 2018/6/17.
 * @author cbq
 */
public class Entry {

    private static final String AND = "and";
    private static final String NOT = "not";
    private static final String OR = "or";

    //    (Brutus OR Caesar) AND NOT (Antony OR Cleopatra)


    public static ArrayList andQuery(LinkedHashMap<Integer, ArrayList<Integer>> lword,
                                     LinkedHashMap<Integer, ArrayList<Integer>> rword){
        ArrayList<Integer> res = new ArrayList();




        return res;
    }

    public static ArrayList orQuery(ArrayList lpos, ArrayList rpos){
        ArrayList<Integer> res = new ArrayList();


        return res;
    }

    public static ArrayList notQuery(ArrayList lpos, ArrayList rpos){
        ArrayList<Integer> res = new ArrayList();


        return res;
    }


    public LinkedHashMap phraseQuery(String lword, String rword){
        LinkedHashMap<Integer, ArrayList<Integer>> res = new LinkedHashMap<>();

        LinkedHashMap<Integer, ArrayList<Integer>> leftWord = RetrievalUtil.retrieveWord(lword);
        LinkedHashMap<Integer, ArrayList<Integer>> rightWord = RetrievalUtil.retrieveWord(rword);

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

    public static void main(String[] args){

//        LinkedHashMap<Integer, ArrayList<Integer>> res = RetrievalUtil.retrieveWord("grain");
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNext()){
//            String word = scanner.next();
//        }
        //grain board
        String lword = "g";
        String rword = "program";
        LinkedHashMap<Integer, ArrayList<Integer>> res = new Entry().phraseQuery(lword, rword);

        System.out.println("Phrase query for " + lword + " and " + rword);
        for (Map.Entry entry: res.entrySet()) {
            Integer docId = (Integer) entry.getKey();
            ArrayList<Integer> positions = (ArrayList<Integer>)entry.getValue();

            System.out.println("doc" + docId + ":");
            for (Integer pos:positions) {
                System.out.println("pos " + pos);
            }
            System.out.println();
        }


    }

}
