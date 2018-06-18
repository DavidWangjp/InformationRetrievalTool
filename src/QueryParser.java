import java.util.*;

/**
 * @author reeve
 */
public class QueryParser {
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOT = "NOT";
    public static final String LEFT_PAR = "(";
    public static final String RIGHT_PAR = ")";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str = "(Brutus OR Caesar) AND NOT (Antony OR Cleopatra)";
        List<String> list = parseBoolean(str);
        List<Integer> res = queryBoolean(parseBoolean(str));
        System.out.println("Found in docs: " + res);
    }

    private static List<String> parseBoolean(String expString) {
        List<String> exp = new ArrayList<>();
        String[] splitting = expString.split("\\s+");

        for (String str : splitting) {
            if (str.length() > 1 && str.startsWith(LEFT_PAR)) {
                exp.add(LEFT_PAR);
                str = str.substring(1);
            }
            if (str.length() > 1 && str.endsWith(RIGHT_PAR)) {
                exp.add(str.substring(0, str.length() - 1));
                exp.add(RIGHT_PAR);
            } else {
                exp.add(str);
            }
        }

        for (int i = 0; i < exp.size(); i++) {
            String str = exp.get(i);
            if (Objects.equals(str, NOT)) {
                if (i > 0 && Objects.equals(exp.get(i - 1), AND)) {
                    exp.remove(i-1);
                    i--;
                } else {
                    throw new RuntimeException("parseBoolean(): NOT must follow AND");
                }
            }
        }

        return exp;
    }

    public static List<Integer> queryBoolean(final List<String> exp) {
        // When the expression is a single word.
        if (1 == exp.size()) {
            return QueryEntry.getDocIds(RetrievalUtil.retrieveWord(exp.get(0)));
        }

        // If there is a pair of parentheses surrounding the whole expression, remove it.
        if (Objects.equals(exp.get(0), LEFT_PAR) && Objects.equals(exp.get(exp.size() - 1), RIGHT_PAR)) {
            int depthParentheses = 0;
            for (String str : exp.subList(1, exp.size() - 1)) {
                if (Objects.equals(str, LEFT_PAR)) {
                    depthParentheses++;
                }
                if (Objects.equals(str, RIGHT_PAR)) {
                    depthParentheses--;
                }
                if (depthParentheses < 0) {
                    break;
                }
            }
            if (depthParentheses == 0) {
                // There really is such a pair, calculate the expression after
                // removing it.
                return queryBoolean(exp.subList(1, exp.size() - 1));
            }
        }

        // Locate the operator with the lowest priority, and partition the whole
        // expression by it.
        int lowestPriority = Integer.MAX_VALUE;
        int indexLowestPriorityOp = -1;
        int i = 0;
        while (i < exp.size()) {
            if (Objects.equals(exp.get(i), LEFT_PAR)) {
                // Skip parentheses and the part of the expression they surrounds.
                int depthParentheses = 1;
                while (++i < exp.size()) {
                    if (Objects.equals(exp.get(i), LEFT_PAR)) {
                        depthParentheses++;
                    }
                    if (Objects.equals(exp.get(i), RIGHT_PAR)) {
                        depthParentheses--;
                    }
                    if (0 == depthParentheses) {
                        break;
                    }
                }
                i++;
            } else {
                if (isOperator(exp.get(i))) {
                    int priority = priorityOf(exp.get(i));
                    if (priority < lowestPriority) {
                        lowestPriority = priority;
                        indexLowestPriorityOp = i;
                        if (0 == lowestPriority) {
                            break;
                        }
                    }
                }
                i++;
            }
        }

        List<Integer> leftRes = queryBoolean(exp.subList(0, indexLowestPriorityOp));
        List<Integer> rightRes = queryBoolean(exp.subList(indexLowestPriorityOp + 1, exp.size()));

        if (Objects.equals(exp.get(indexLowestPriorityOp), AND)) {
            return QueryEntry.andQuery(leftRes, rightRes);
        } else if (Objects.equals(exp.get(indexLowestPriorityOp), OR)) {
            return QueryEntry.orQuery(leftRes, rightRes);
        } else if (Objects.equals(exp.get(indexLowestPriorityOp), NOT)) {
            return QueryEntry.notQuery(leftRes, rightRes);
        } else {
            throw new RuntimeException("queryBoolean(): invalid operator");
        }
    }

    private static boolean isOperator(String str) {
        // Exclude parentheses.
        return (Objects.equals(str, AND) || Objects.equals(str, OR) || Objects.equals(str, NOT));
    }

    private static int priorityOf(String operator) {
        if (Objects.equals(operator, OR)) {
            return 0;
        } else if (Objects.equals(operator, AND)) {
            return 1;
        } else if (Objects.equals(operator, NOT)) {
            return 2;
        } else {
            throw new RuntimeException("priorityOf(): invalid operator");
        }
    }
}
