import java.util.*;

import static java.lang.Math.log10;

/**
 * @author reeve
 */
public class Query {
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOT = "NOT";
    public static final String LEFT_PAR = "(";
    public static final String RIGHT_PAR = ")";

    private static boolean initialized = false;
    private final static int K = 100;

    public Query() {
    }

    public static void main(String[] args) {
        while (true) {
            queryUserInterface();
        }
    }

    public static void queryUserInterface() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("[1] Word or phrase query");
        System.out.println("[2] Boolean query");
        System.out.print("Please choose a query mode: ");

        int mode = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Please enter your query in a line: ");
        String query = scanner.nextLine();

        switch (mode) {
            case 1:
                String[] splitTokens = query.split("\\s+");
                if (splitTokens.length == 1) {
                    queryWordTopK(splitTokens[0], K);
                } else if (splitTokens.length == 2) {
                    queryPhraseTopK(splitTokens[0], splitTokens[1], K);
                }
                break;
            case 2:
                queryBooleanTopK(query, K);
                break;
            default:
                System.out.println("Invalid mode");
        }
    }

    public static void queryWordTopK(String token, int k) {
        initInvertedIndex();

        PriorityQueue<DocIdScorePositionsEntry> resultMaxHeap = new PriorityQueue<>((o1, o2) ->
                Double.compare(o2.getScore(), o1.getScore()));

        String term = InvertedIndex.getTerm(token);

        // Return null in case of stop words.
        if (term == null) {
            System.out.println("Please make your query more specific");
            return;
        }

        // TODO: term correction.

        if (InvertedIndex.termDictionary.containsKey(term) && InvertedIndex.invertedIndex.containsKey(term)) {
            // Compute idf of the term.
            double idf = log10(1.0 * InvertedIndex.FILE_SIZE / InvertedIndex.termDictionary.get(term));

            // Get the posting list.
            Map<Integer, ArrayList<Integer>> postings = InvertedIndex.invertedIndex.get(term);

            // Compute the score of each document.
            for (Map.Entry<Integer, ArrayList<Integer>> posting : postings.entrySet()) {
                Integer docId = posting.getKey();
                List<Integer> positions = posting.getValue();

                // Compute tf-idf.
                double score = idf * (1.0 + log10(positions.size()));
                score /= InvertedIndex.docLen.get(docId);

                // Add to the heap.
                resultMaxHeap.add(new DocIdScorePositionsEntry(docId, score, positions));
            }

            // Print the result.
            System.out.println("Result:");
            for (int i = 0; i < k && i < resultMaxHeap.size(); i++) {
                DocIdScorePositionsEntry resultEntry = resultMaxHeap.poll();
                assert resultEntry != null;

                System.out.format("  DocId: %5d, Score: %6.2f, Positions: %s\n",
                        resultEntry.getDocId(),
                        resultEntry.getScore(),
                        resultEntry.getPositions());
            }
        }
    }

    public static void queryPhraseTopK(String leftToken, String rightToken, int k) {
        initInvertedIndex();

        Map<Integer, List<Integer>> phrasePostings = new HashMap<>();

        PriorityQueue<DocIdScorePositionsEntry> resultMaxHeap = new PriorityQueue<>((o1, o2) ->
                Double.compare(o2.getScore(), o1.getScore()));

        String leftTerm = InvertedIndex.getTerm(leftToken);
        String rightTerm = InvertedIndex.getTerm(rightToken);

        // Return null in case of stop words.
        if (leftTerm == null && rightTerm == null) {
            System.out.println("Please make your query more specific");
            return;
        } else if (leftTerm == null) {
            System.out.format("%s is omitted", leftToken);
            queryWordTopK(rightTerm, K);
            return;
        } else if (rightTerm == null) {
            System.out.format("%s is omitted", rightToken);
            queryWordTopK(leftTerm, K);
            return;
        }

        // TODO: term correction.

        if (InvertedIndex.termDictionary.containsKey(leftTerm) && InvertedIndex.invertedIndex.containsKey(leftTerm)
                && InvertedIndex.termDictionary.containsKey(rightTerm)
                && InvertedIndex.invertedIndex.containsKey(rightTerm)) {
            // Get posting lists of both terms.
            Map<Integer, ArrayList<Integer>> leftPostings = InvertedIndex.invertedIndex.get(leftTerm);
            Map<Integer, ArrayList<Integer>> rightPostings = InvertedIndex.invertedIndex.get(rightTerm);

            // Build the posting list of the phrase.
            for (Map.Entry<Integer, ArrayList<Integer>> leftPosting : leftPostings.entrySet()) {
                if (rightPostings.containsKey(leftPosting.getKey())) {
                    List<Integer> leftPositions = leftPosting.getValue();
                    List<Integer> rightPositions = rightPostings.get(leftPosting.getKey());

                    List<Integer> positions = new ArrayList<>();
                    for (Integer leftPosition : leftPositions) {
                        for (Integer rightPosition : rightPositions) {
                            if (leftPosition.equals(rightPosition - 1)) {
                                positions.add(leftPosition);
                            }
                        }
                    }

                    if (positions.size() != 0) {
                        phrasePostings.put(leftPosting.getKey(), positions);
                    }
                }
            }

            // Compute the score of each document.
            for (Map.Entry<Integer, List<Integer>> posting : phrasePostings.entrySet()) {
                // Compute idf.
                double idf = log10(1.0 * InvertedIndex.FILE_SIZE / phrasePostings.size());

                Integer docId = posting.getKey();
                List<Integer> positions = posting.getValue();

                // Compute tf-idf.
                double score = idf * (1.0 + log10(positions.size()));
                score /= InvertedIndex.docLen.get(docId);

                // Add to the heap.
                resultMaxHeap.add(new DocIdScorePositionsEntry(docId, score, positions));
            }

            // Print the result.
            System.out.println("Result:");
            for (int i = 0; i < k && i < resultMaxHeap.size(); i++) {
                DocIdScorePositionsEntry resultEntry = resultMaxHeap.poll();
                assert resultEntry != null;

                System.out.format("  DocId: %5d, Score: %6.2f, Positions: %s\n",
                        resultEntry.getDocId(),
                        resultEntry.getScore(),
                        resultEntry.getPositions());
            }
        }
    }

    public static void queryBooleanTopK(String query, int k) {
        initInvertedIndex();
        List<String> tokens = parseBoolean(query);
        final Map<Integer, Double> scores = new HashMap<>();

        List<Integer> docIds = queryBooleanAuxiliary(tokens, scores);
        if (docIds == null) {
            System.out.println("Please make your query more specific");
            return;
        }

        final PriorityQueue<DocIdScoreEntry> resultMaxHeap = new PriorityQueue<>((o1, o2) ->
                Double.compare(o2.getScore(), o1.getScore()));
        docIds.forEach(docId -> {
            Double score = scores.get(docId);
            assert score != null;
            resultMaxHeap.add(new DocIdScoreEntry(docId, score));
        });

        // Print the result.
        System.out.println("Result:");
        for (int i = 0; i < k && i < resultMaxHeap.size(); i++) {
            DocIdScoreEntry resultEntry = resultMaxHeap.poll();
            assert resultEntry != null;

            System.out.format("  DocId: %5d, Score: %6.2f\n",
                    resultEntry.getDocId(),
                    resultEntry.getScore());
        }
    }

    public static List<Integer> queryBooleanAuxiliary(List<String> tokens, Map<Integer, Double> scores) {
        if (1 == tokens.size()) {
            String term = InvertedIndex.getTerm(tokens.get(0));

            // If the term is a stopping word, return a null list (not an empty one).
            if (term == null) {
                return null;
            }

            // TODO: term correction.

            if (InvertedIndex.termDictionary.containsKey(term) && InvertedIndex.invertedIndex.containsKey(term)) {
                // Compute idf of the term.
                double idf = log10(1.0 * InvertedIndex.FILE_SIZE / InvertedIndex.termDictionary.get(term));

                // Get the posting list.
                Map<Integer, ArrayList<Integer>> postings = InvertedIndex.invertedIndex.get(term);

                // Computer the score of each document on this term.
                for (Map.Entry<Integer, ArrayList<Integer>> posting : postings.entrySet()) {
                    Integer docId = posting.getKey();
                    List<Integer> positions = posting.getValue();

                    // Compute tf-idf.
                    double score = idf * (1.0 + log10(positions.size()));
                    score /= InvertedIndex.docLen.get(docId);

                    // Accumulate the score of each document.
                    if (!scores.containsKey(docId)) {
                        scores.put(docId, score);
                    } else {
                        scores.put(docId, scores.get(docId) + score);
                    }
                }

                // Return docIds of the posting list.
                return getDocIdsOfPostings(postings);
            } else {
                return new ArrayList<>();
            }
        }

        // If there is a pair of parentheses surrounding the whole expression, remove it.
        if (Objects.equals(tokens.get(0), LEFT_PAR) && Objects.equals(tokens.get(tokens.size() - 1), RIGHT_PAR)) {
            int depthParentheses = 0;
            for (String str : tokens.subList(1, tokens.size() - 1)) {
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
                return queryBooleanAuxiliary(tokens.subList(1, tokens.size() - 1), scores);
            }
        }

        // Locate the operator with the lowest priority, and partition the whole expression by it.
        int lowestPriority = Integer.MAX_VALUE;
        int indexLowestPriorityOp = -1;
        int i = 0;
        while (i < tokens.size()) {
            if (Objects.equals(tokens.get(i), LEFT_PAR)) {
                // Skip parentheses and the part of the expression they surrounds.
                int depthParentheses = 1;
                while (++i < tokens.size()) {
                    if (Objects.equals(tokens.get(i), LEFT_PAR)) {
                        depthParentheses++;
                    }
                    if (Objects.equals(tokens.get(i), RIGHT_PAR)) {
                        depthParentheses--;
                    }
                    if (0 == depthParentheses) {
                        break;
                    }
                }
                i++;
            } else {
                if (isOperator(tokens.get(i))) {
                    int priority = priorityOf(tokens.get(i));
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

        List<Integer> leftRes = queryBooleanAuxiliary(tokens.subList(0, indexLowestPriorityOp), scores);
        List<Integer> rightRes = queryBooleanAuxiliary(tokens.subList(indexLowestPriorityOp + 1, tokens.size()), scores);

        if (leftRes == null && rightRes == null) {
            return null;
        } else if (leftRes == null) {
            return rightRes;
        } else if (rightRes == null) {
            return leftRes;
        }

        if (Objects.equals(tokens.get(indexLowestPriorityOp), AND)) {
            return QueryUtil.andOperate(leftRes, rightRes);
        } else if (Objects.equals(tokens.get(indexLowestPriorityOp), OR)) {
            return QueryUtil.orOperate(leftRes, rightRes);
        } else if (Objects.equals(tokens.get(indexLowestPriorityOp), NOT)) {
            return QueryUtil.notOperate(leftRes, rightRes);
        } else {
            throw new RuntimeException("queryBooleanAuxiliary(): invalid operator");
        }
    }

    private static List<String> parseBoolean(String query) {
        List<String> tokens = new ArrayList<>();
        String[] splitting = query.split("\\s+");

        for (String str : splitting) {
            if (str.length() > 1 && str.startsWith(LEFT_PAR)) {
                tokens.add(LEFT_PAR);
                str = str.substring(1);
            }
            if (str.length() > 1 && str.endsWith(RIGHT_PAR)) {
                tokens.add(str.substring(0, str.length() - 1));
                tokens.add(RIGHT_PAR);
            } else {
                tokens.add(str);
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            String str = tokens.get(i);
            if (Objects.equals(str, NOT)) {
                if (i > 0 && Objects.equals(tokens.get(i - 1), AND)) {
                    tokens.remove(i - 1);
                    i--;
                } else {
                    throw new RuntimeException("parseBoolean(): NOT must follow AND");
                }
            }
        }

        return tokens;
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

    private static void initInvertedIndex() {
        if (!initialized) {
            InvertedIndex.init();
            initialized = true;
        }
    }

    private static List<Integer> getDocIdsOfPostings(Map<Integer, ArrayList<Integer>> postings) {
        final List<Integer> docIds = new ArrayList<>();
        postings.forEach((key, value) -> docIds.add(key));
        return docIds;
    }
}

class DocIdScorePositionsEntry {
    private int docId;
    private double score;
    private List<Integer> positions;

    DocIdScorePositionsEntry(int docId, double score, List<Integer> positions) {
        this.docId = docId;
        this.score = score;
        this.positions = positions;
    }

    int getDocId() {
        return docId;
    }

    double getScore() {
        return score;
    }

    List<Integer> getPositions() {
        return positions;
    }
}

class DocIdScoreEntry {
    private int docId;
    private double score;

    DocIdScoreEntry(int docId, double score) {
        this.docId = docId;
        this.score = score;
    }

    int getDocId() {
        return docId;
    }

    double getScore() {
        return score;
    }
}
