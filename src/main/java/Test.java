import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
//        InvertedIndex.init();

        String basePath = Test.class.getClassLoader().getResource(".").getPath();
        System.out.println(basePath);
        File dir = new File(basePath);
        System.out.println(dir.getAbsolutePath());

//        String token = "follow";
//        String term = InvertedIndex.getTerm(token);
//        Map<Integer, ArrayList<Integer>> postings = InvertedIndex.invertedIndex.get(term);
//        postings.forEach((integer, integers) -> System.out.println("DocId: " + integer));
    }
}
