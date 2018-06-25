# 信息检索

## InvertedIndex

tokenDictionary是所有出现的单词词条化之后的词典，可能拼写校正要用到

## Correction

目前使用的方式如下，创建一个String数组
String[] query = {"conclsion", "calenda", "ar", "true", "canlendae",  "ture"};
然后新建一个Correction类调用correct方法就可以得到纠正后的结果：
new irt.Correction().correct(query);
单词纠正算法参考ppt上的Levenshtein距离算法，从Levenshtein为1 到 N逐增, 如果对应一个距离，词库里有合法的单词，选择tf最高的词项返回。
N的值可以在correct方法中设置。

## QueryUtil

主要是实现布尔查询（andOperate 、 orOperate 、notOperate）和短语查询(phraseQuery)

其中三个布尔查询接受的参数为左右边词项出现的docId集合

返回值值为其交/并/否的集合。

短语查询传入 irt.Query.retrieveWord()返回的对象。

getDocIds为获得docId的辅助函数。

## Query

实现单词查询、双词短语查询和布尔查询，均为 TopK 查询。

### 单词查询

`void queryWordTopK(String token, int k)`

#### 说明

- 打印评分排名前 K 个文档的 DocId，Score 和 Postions。
- 如果单词为停用词，将不进行查询，并提示 `"Please make your query more specific"`。
- 如果单词在词典中不存在，会提示校正。接受校正将查询校正词，不接受将打印 `"No result"`。
- 分数采用对于该词的 idf-tf 权重。

### 双词短语查询

`void queryPhraseTopK(String leftToken, String rightToken, Int k)`

#### 说明

- 打印评分排名前 K 个文档的 DocId，Score 和 Postions。
- 如果有且仅有一个单词为停用词，会忽略该词，并提示 `"'xxx' is omitted\n"`。
- 如果两个单词均为停用词，将不进行查询，并提示 `"Please make your query more specific"`。
- 如果单词在词典中不存在，会提示校正。接受校正将使用校正词代替原词，不接受将打印 `"No result"`。
- 分数采用对于该双词短语（作为整体）的 idf-tf 权重。

### 布尔查询

`void queryBooleanTopK(String query, int k)`

#### 辅助函数

- `List<Integer> queryBooleanAuxiliary(List<String> tokens, Map<Integer, Double> scores)`
- `List<String> parseBoolean(String query)`

#### 说明

- 打印评分排名前 K 个文档的 DocId 和 Score。
- 如果有停用词会忽略该词，并提示 `"'xxx' is omitted\n"`。
- 如果所有单词都是停用词，会提示 `"Please make your query more 
- 不进行单词校正。
- 查询语句中 `NOT` 必须紧跟 `AND`，即以 `AND NOT` 的形式出现。
- 没有对布尔查询进行优化，计算的顺序为小括号内优先，`NOT` > `AND` > `OR`，同优先级从左往右。
- 分数采用对于查询语句中每一个词（除了紧跟 `NOT` 的那些）的 idf-tf 权重的累积。

## 用户接口

如题。