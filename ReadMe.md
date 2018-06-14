#信息检索
##RetriverlUtil类
检索单个单词 retrieveWord(String query)，得到的结果存放在LinkedHashMap里  

* 每个entry的key是docId，
* 每个entry的value是单词在文档中出现的位置链表，用ArrayList<Integer>来表示，已排序