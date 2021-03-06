package irt;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

class InvertedIndex
{
    private static final HashSet<Character> Punctuations = new HashSet<Character>()
    {{
        add('!');
        add('@');
        add('#');
        add('$');
        add('%');
        add('^');
        add('&');
        add('*');
        add('(');
        add(')');
        add('_');
        add('+');
        add('-');
        add('=');
        add('{');
        add('}');
        add('[');
        add(']');
        add(';');
        add(',');
        add('.');
        add('\"');
        add('\'');
        add('<');
        add('>');
    }};

    static final String indexGenerationDirectory = "./GeneratedIndexFiles";

    // stores all tokens
    private static HashSet<String> tokenDictionary = new HashSet<>();

    // stores all terms and doc frequency
    static HashMap<String, Integer> termDictionary = new HashMap<>();

    static TreeMap<String, TreeMap<Integer, ArrayList<Integer>>> invertedIndex = new TreeMap<>();

    // doc length based on VSM
    // vector length
    static HashMap<Integer, Double> docLen = new HashMap<>();
    private static Stemmer stemmer = new Stemmer();
    static int FILE_SIZE;

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

    static void init(String documentCollectionDirectory)
    {
        System.out.print("Initializing the inverted index...");
        File collectionDir = new File(documentCollectionDirectory);
        collectionDir.mkdir();
        FILE_SIZE = Objects.requireNonNull(collectionDir.list()).length;

        File dir = new File(indexGenerationDirectory);
        dir.mkdir();

        String[] files = dir.list();

        long startTime = System.currentTimeMillis();
        File invertedIndexFile = new File(indexGenerationDirectory + "/InvertedIndex");
        File tokenDictionaryFile = new File(indexGenerationDirectory + "/TokenDictionary");
        File termDictionaryFile = new File(indexGenerationDirectory + "/TermDictionary");
        File docLenFile = new File(indexGenerationDirectory + "/DocLen");

        try
        {
            ObjectInputStream invertedIndexInputStream = new ObjectInputStream(new FileInputStream(invertedIndexFile));
            ObjectInputStream tokenDictionaryInputStream = new ObjectInputStream(new FileInputStream(tokenDictionaryFile));
            ObjectInputStream termDictionaryInputStream = new ObjectInputStream(new FileInputStream(termDictionaryFile));
            ObjectInputStream docLenInputStream = new ObjectInputStream(new FileInputStream(docLenFile));

            invertedIndex = (TreeMap<String, TreeMap<Integer, ArrayList<Integer>>>) invertedIndexInputStream.readObject();
            tokenDictionary = (HashSet<String>) tokenDictionaryInputStream.readObject();
            termDictionary = (HashMap<String, Integer>) termDictionaryInputStream.readObject();
            docLen = (HashMap<Integer, Double>) docLenInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            build(documentCollectionDirectory);
            try
            {
                ObjectOutputStream invertedIndexOutputStream = new ObjectOutputStream(new FileOutputStream(invertedIndexFile));
                ObjectOutputStream tokenDictionaryOutputStream = new ObjectOutputStream(new FileOutputStream(tokenDictionaryFile));
                ObjectOutputStream termDictionaryOutputStream = new ObjectOutputStream(new FileOutputStream(termDictionaryFile));
                ObjectOutputStream docLenOutputStream = new ObjectOutputStream(new FileOutputStream(docLenFile));

                invertedIndexOutputStream.writeObject(invertedIndex);
                tokenDictionaryOutputStream.writeObject(tokenDictionary);
                termDictionaryOutputStream.writeObject(termDictionary);
                docLenOutputStream.writeObject(docLen);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        System.out.println("finished in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private static void build(String documentCollectionDirectory)
    {
        File dir = new File(documentCollectionDirectory);
        assert dir.isDirectory();

        String[] files = dir.list();

        assert files != null;
        for (String file : files)
        {
            if (!file.endsWith(".html"))
            {
                continue;
            }
            int docId = Integer.parseInt(file.split("\\.")[0]);
            String file_name = dir + "/" + file;
            HashMap<String, Integer> fileVector = new HashMap<>();
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
                            {
                                invertedIndex.get(term).get(docId).add(position);
                                fileVector.put(term, invertedIndex.get(term).get(docId).size());
                            }
                            else
                            {
                                ArrayList<Integer> positions = new ArrayList<>();
                                positions.add(position);
                                index.put(docId, positions);
                                fileVector.put(term, 1);
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
                            fileVector.put(term, 1);
                        }
                        position++;
                    }
                }

                double len = 0.0;
                for (Map.Entry entry : fileVector.entrySet())
                {
                    len += pow(1.0 + log10((Integer) entry.getValue()), 2);
                }

                docLen.put(docId, sqrt(len));

                reader.close();
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
        if (token.startsWith("&lt;"))
            token = token.substring(4);
        if (token.isEmpty())
            return null;
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
        if (token.endsWith("'s"))
            token = token.substring(0, token.length() - 2);
        return token;
    }


    static int getDf(String term)
    {
        if (!termDictionary.containsKey(term))
            return 0;
        else return invertedIndex.get(term).size();
    }


}
