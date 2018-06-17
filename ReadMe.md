# 信息检索

## InvertedIndex

tokenDictionary是所有出现的单词词条化之后的词典，可能拼写校正要用到

## RetriverlUtil类

检索单个单词 retrieveWord(String query)，得到的结果存放在LinkedHashMap里  

* 每个entry的key是docId，
* 每个entry的value是单词在文档中出现的位置链表，用ArrayList<Integer>来表示
* 检索得到的文档已经按照已排序
  
如果查询的单词是停用词，返回null

##Correction类

目前使用的方式如下，创建一个String数组
String[] query = {"conclsion", "calenda", "ar", "true", "canlendae",  "ture"};
然后新建一个Correction类调用correct方法就可以得到纠正后的结果：
new Correction().correct(query);
单词纠正算法参考ppt上的Levenshtein距离算法，从Levenshtein为1 到 N逐增, 如果对应一个距离，词库里有合法的单词，选择tf最高的词项返回。
N的值可以在correct方法中设置。



## QueryEntry

主要是实现布尔查询（andQuery 、 orQuery 、notQuery）和短语查询(phraseQuery)

其中三个布尔查询接受的参数为左右边词项出现的docId集合

返回值值为其交/并/否的集合。

短语查询传入 RetrievalUtil.retrieveWord()返回的对象。

getDocIds为获得docId的辅助函数。



所以目前还没实现对输入的词法分析。。

比如(Brutus OR Caesar) AND NOT (Antony OR Cleopatra)



