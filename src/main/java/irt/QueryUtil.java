package irt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryUtil {
    public static List<Integer> andOperate(List<Integer> lpos, List<Integer> rpos) {
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

    public static List<Integer> orOperate(List<Integer> lpos, List<Integer> rpos) {
        Set<Integer> lset = new HashSet<>(lpos);
        Set<Integer> rset = new HashSet<>(rpos);
        lset.addAll(rset);

        return new ArrayList<>(lset);
    }

    public static List<Integer> notOperate(List<Integer> lpos, List<Integer> rpos) {
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
}
