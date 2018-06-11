import java.io.*;
import java.util.ArrayList;
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
    }};

    private static final String path = ".\\src";

    /**
     * Score and positions information of a doc in a index term
     */
    private static class IndexInfo implements Serializable
    {
        ArrayList<Integer> positions = new ArrayList<>();

        public float score()
        {
            return positions.size();
        }

        @Override
        public String toString()
        {
            return super.toString();
        }
    }

    private static HashSet<String> tokenDictionary = new HashSet<>();
    private static TreeMap<String, TreeMap<Integer, IndexInfo>> invertedIndex = new TreeMap<>();
    private static Stemmer stemmer = new Stemmer();

    static void init()
    {
        System.out.println("Initializing...");
        File invertedIndexFile = new File(path + "\\InvertedIndex");
        File tokenDictionaryFile = new File(path + "\\TokenDictionary");
        try
        {
            ObjectInputStream invertedIndexInputStream = new ObjectInputStream(new FileInputStream(invertedIndexFile));
            ObjectInputStream tokenDictionaryInputStream = new ObjectInputStream(new FileInputStream(tokenDictionaryFile));
            invertedIndex = (TreeMap<String, TreeMap<Integer, IndexInfo>>) invertedIndexInputStream.readObject();
            tokenDictionary = (HashSet<String>) tokenDictionaryInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            build();
            try
            {
                ObjectOutputStream invertedIndexOutputStream = new ObjectOutputStream(new FileOutputStream(invertedIndexFile));
                ObjectOutputStream tokenDictionaryOutputStream = new ObjectOutputStream(new FileOutputStream(tokenDictionaryFile));
                invertedIndexOutputStream.writeObject(invertedIndex);
                tokenDictionaryOutputStream.writeObject(tokenDictionary);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        System.out.println("Finish");
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

                        /*
                        convert to term and add to inverted index
                         */
                        stemmer.add("1.2".toCharArray(), 3);
                        stemmer.stem();
                        String term = stemmer.toString();

                        if (invertedIndex.containsKey(term))
                        {
                            TreeMap<Integer, IndexInfo> index = invertedIndex.get(term);
                            if (index.containsKey(docId))
                                invertedIndex.get(term).get(docId).positions.add(position);
                            else
                            {
                                IndexInfo info = new IndexInfo();
                                info.positions.add(position);
                                index.put(docId, info);
                            }
                        }
                        else
                        {
                            IndexInfo info = new IndexInfo();
                            info.positions.add(position);
                            invertedIndex.put(term, new TreeMap<Integer, IndexInfo>()
                            {{
                                put(docId, info);
                            }});
                        }
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
