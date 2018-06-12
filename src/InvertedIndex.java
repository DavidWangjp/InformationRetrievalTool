import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

class InvertedIndex
{
    private static final HashSet<Character> Punctuations = new HashSet<Character>()
    {{
        add(';');
        add(',');
        add('.');
        add('+');
        add('-');
        add('\"');
        add('\'');
    }};

    private static final String path = ".\\src";

    // stores all tokens
    private static HashSet<String> tokenDictionary = new HashSet<>();

    // stores all terms and doc frequency
    private static HashMap<String, Integer> termDictionary = new HashMap<>();

    private static TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> invertedIndex = new TreeMap<>();
    private static Stemmer stemmer = new Stemmer();

    /**
     * convert word to term, return null if stopword
     *
     * @param word
     * @return
     */
    static String getTerm(String word)
    {
        String token = getToken(word.trim().toLowerCase());
        if (token == null || Stopword.stopwrods.contains(token))
            return null;
        stemmer.add(token.toCharArray(), token.length());
        stemmer.stem();
        return stemmer.toString();

    }

    static void init()
    {
        System.out.println("Initializing...");
        long startTime = System.currentTimeMillis();
        File invertedIndexFile = new File(path + "\\InvertedIndex");
        File tokenDictionaryFile = new File(path + "\\TokenDictionary");
        File termDictionaryFile = new File(path + "\\TermDictionary");
        try
        {
            ObjectInputStream invertedIndexInputStream = new ObjectInputStream(new FileInputStream(invertedIndexFile));
            ObjectInputStream tokenDictionaryInputStream = new ObjectInputStream(new FileInputStream(tokenDictionaryFile));
            ObjectInputStream termDictionaryInputStream = new ObjectInputStream(new FileInputStream(termDictionaryFile));

            invertedIndex = (TreeMap<String, TreeMap<Integer, ArrayList<Integer>>>) invertedIndexInputStream.readObject();
            tokenDictionary = (HashSet<String>) tokenDictionaryInputStream.readObject();
            termDictionary = (HashMap<String, Integer>) termDictionaryInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            build();
            try
            {
                ObjectOutputStream invertedIndexOutputStream = new ObjectOutputStream(new FileOutputStream(invertedIndexFile));
                ObjectOutputStream tokenDictionaryOutputStream = new ObjectOutputStream(new FileOutputStream(tokenDictionaryFile));
                ObjectOutputStream termDictionaryOutputStream = new ObjectOutputStream(new FileOutputStream(termDictionaryFile));

                invertedIndexOutputStream.writeObject(invertedIndex);
                tokenDictionaryOutputStream.writeObject(tokenDictionary);
                termDictionaryOutputStream.writeObject(termDictionary);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        System.out.println("Finish in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private static void build()
    {
        File dir = new File(path + "\\Reuters");

        String[] files = dir.list();

        assert files != null;
        for (String file : files)
        {
            int docId = Integer.parseInt(file.split("\\.")[0]);
            String file_name = dir + "\\" + file;
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(new File(file_name)));
                String content;
                int position = 0;
                while ((content = reader.readLine()) != null)
                {
                    String[] words = content.trim().split("(,\\s)|(\\.\\s)|\"| ");
                    for (String word : words)
                    {

                        if (word == null || word.isEmpty())
                            continue;

                        String token = getToken(word.trim().toLowerCase());
                        if (token == null)
                        {
                            position++;
                            continue;
                        }

                        /*
                        add token to dictionary
                         */
                        tokenDictionary.add(token);

                        if (Stopword.stopwrods.contains(token))
                        {
                            position++;
                            continue;
                        }

                        /*
                        convert to term and add to inverted index
                         */
                        stemmer.add(token.toCharArray(), token.length());
                        stemmer.stem();
                        String term = stemmer.toString();

                        if (termDictionary.containsKey(term))
                            termDictionary.put(term, termDictionary.get(term) + 1);
                        else
                            termDictionary.put(term, 1);

                        if (invertedIndex.containsKey(term))
                        {
                            TreeMap<Integer, ArrayList<Integer>> index = invertedIndex.get(term);
                            if (index.containsKey(docId))
                                invertedIndex.get(term).get(docId).add(position);
                            else
                            {
                                ArrayList<Integer> positions = new ArrayList<>();
                                positions.add(position);
                                index.put(docId, positions);
                            }
                        }
                        else
                        {
                            ArrayList<Integer> positions = new ArrayList<>();
                            positions.add(position);
                            invertedIndex.put(term, new TreeMap<Integer, ArrayList<Integer>>()
                            {{
                                put(docId, positions);
                            }});
                        }
                        position++;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * remove punctuations of a token at start and end position
     *
     * @param token
     * @return
     */
    private static String getToken(String token)
    {
        while (Punctuations.contains(token.charAt(0)))
        {
            token = token.substring(1);
            if (token.isEmpty())
                return null;
        }
        while (Punctuations.contains(token.charAt(token.length() - 1)))
        {
            token = token.substring(0, token.length() - 1);
            if (token.isEmpty())
                return null;
        }
        return token;
    }
}
