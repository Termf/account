package com.binance.account.common.query.es;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch复合查询条件构造器（<a href="https://www.elastic.co/guide/en/elasticsearch/reference/6.4/query-dsl-bool-query.html">参考</a>）
 * Created by Shining.Cai on 2018/09/20.
 **/
@Data
public class ESQueryBuilder implements ESQueryConst{

    private static final String BOOL = "bool";

    private String queryType = BOOL;
    private List<ESQueryCondition> mustCondition = new ArrayList<>();
    private List<ESQueryCondition> mustNotCondition = new ArrayList<>();
    private List<ESQueryCondition> shouldCondition = new ArrayList<>();
    private List<ESQueryCondition> filterCondition = new ArrayList<>();
    private List<ESSortCondition> sortCondition = new ArrayList<>();
    private int minimumShouldMatch = 0;

    /** 偏移量 */
    private int from = 0;
    /** 返回条数 */
    private int size = 100;


    public static ESQueryBuilder instance(){
        return new ESQueryBuilder();
    }


    public ESQueryBuilder type(String queryType){
        this.queryType = queryType;
        return this;
    }

    public ESQueryBuilder limit(int from, int size){
        this.from = from;
        this.size = size;
        return this;
    }

    public ESQueryBuilder must(ESQueryCondition... conditions){
        if (conditions != null){
            mustCondition.addAll(Arrays.asList(conditions));
        }
        return this;
    }
    public ESQueryBuilder must(List<ESQueryCondition> conditions){
        if (!CollectionUtils.isEmpty(conditions)){
            mustCondition.addAll(conditions);
        }
        return this;
    }

    public ESQueryBuilder mustNot(ESQueryCondition... conditions){
        if (conditions != null){
            mustNotCondition.addAll(Arrays.asList(conditions));
        }
        return this;
    }
    public ESQueryBuilder mustNot(List<ESQueryCondition> conditions){
        if (!CollectionUtils.isEmpty(conditions)){
            mustNotCondition.addAll(conditions);
        }
        return this;
    }

    public ESQueryBuilder should(ESQueryCondition... conditions){
        if (conditions != null){
            shouldCondition.addAll(Arrays.asList(conditions));
        }
        return this;
    }
    public ESQueryBuilder should(List<ESQueryCondition> conditions){
        if (!CollectionUtils.isEmpty(conditions)){
            shouldCondition.addAll(conditions);
        }
        return this;
    }

    public ESQueryBuilder filter(ESQueryCondition... conditions){
        if (conditions != null){
            filterCondition.addAll(Arrays.asList(conditions));
        }
        return this;
    }

    public ESQueryBuilder filter(List<ESQueryCondition> conditions){
        if (!CollectionUtils.isEmpty(conditions)){
            filterCondition.addAll(conditions);
        }
        return this;
    }

    /**
     * 排序条件
     * @param conditions 多个排序时，排在前面的优先级较高
     */
    public ESQueryBuilder sort(ESSortCondition... conditions){
        if (conditions != null){
            sortCondition.addAll(Arrays.asList(conditions));
        }
        return this;
    }

    public Map<String, Object> build(){
        Map<String, Object> map = new HashMap<>();
        map.put(LIMIT_FROM, from);
        map.put(LIMIT_SIZE, size);

        Map<String, Object> boolMap = ImmutableMap.of(queryType,
                ImmutableMap.of(TYPE_MUST, ESQueryCondition.toMap(mustCondition),
                                TYPE_MUST_NOT, ESQueryCondition.toMap(mustNotCondition),
                                TYPE_SHOULD, ESQueryCondition.toMap(shouldCondition),
                                TYPE_MINIMUM_SHOULD_MATCH, minimumShouldMatch,
                                TYPE_FILTER, ESQueryCondition.toMap(filterCondition)));

        map.put(TYPE_QUERY, boolMap);
        map.put(TYPE_SORT, ESSortCondition.toMap(sortCondition));
        return map;
    }

}
