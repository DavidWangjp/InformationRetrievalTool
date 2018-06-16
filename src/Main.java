import java.util.*;

import static java.lang.Math.log10;

public class Main
{

    public static void main(String[] args)
    {

        LinkedHashMap<Integer, ArrayList<Integer>> res = RetrievalUtil.retrieveWord("grain");
        for (Map.Entry entry: res.entrySet()) {
            Integer docId = (Integer) entry.getKey();
            ArrayList<Integer> positions = (ArrayList<Integer>)entry.getValue();

            System.out.println("doc"+docId+":");
            for (Integer pos:positions) {
                System.out.println("pos "+pos);
            }
            System.out.println();
        }
    }
}
