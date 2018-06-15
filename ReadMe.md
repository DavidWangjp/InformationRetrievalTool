# 信息检索

## InvertedIndex

tokenDictionary是所有出现的单词词条化之后的词典，可能拼写校正要用到

## RetriverlUtil类

检索单个单词 retrieveWord(String query)，得到的结果存放在LinkedHashMap里  

* 每个entry的key是docId，
* 每个entry的value是单词在文档中出现的位置链表，用ArrayList<Integer>来表示
* 检索得到的文档已经按照已排序
  
如果查询的单词是停用词，返回null
