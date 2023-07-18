package com.binance.account.common.query.es;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询条件（<a href="https://www.elastic.co/guide/en/elasticsearch/reference/6.4/term-level-queries.html">参考</a>）
 * Created by Shining.Cai on 2018/09/20.
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESQueryCondition {


    public static final String TYPE_WILDCARD = "wildcard";
    public static final String TYPE_TERM = "term";
    public static final String TYPE_RANGE = "range";
    public static final String TYPE_EXISTS = "exists";

    /**
     * 查询类型
     */
    private String type;

    private String field;

    private Object value;

    /**
     * 通配符模糊匹配查询（<a href="https://www.elastic.co/guide/en/elasticsearch/reference/6.4/query-dsl-wildcard-query.html">参考</a>）
     */
    public static ESQueryCondition wildcard(String field, Object value){
        return new ESQueryCondition(TYPE_WILDCARD, field, value);
    }

    /**
     * 精准匹配（<a href="https://www.elastic.co/guide/en/elasticsearch/reference/6.4/query-dsl-term-query.html">参考</a>）
     */
    public static ESQueryCondition term(String field, Object value){
        return new ESQueryCondition(TYPE_TERM, field, value);
    }

    public static ESQueryCondition range(String field, Object valueFrom, Object valueTo) {
        return new ESQueryCondition(TYPE_RANGE, field, new Object[] { valueFrom, valueTo } );
    }

    public static Map<String, Object> toMap(ESQueryCondition condition){
        Map<String, Object> map = new HashMap<>();

        if (condition.type != null && condition.field != null && condition.value != null) {
            if (TYPE_RANGE.equals(condition.type)) {
                if (condition.value instanceof Object[] && ((Object[])condition.value).length == 2) {
                    Map<String, Object> inner = new HashMap<>();
                    if (((Object[])condition.value)[0] != null) {
                        inner.put("gte", ((Object[])condition.value)[0]);
                    }
                    if (((Object[])condition.value)[1] != null) {
                        inner.put("lte", ((Object[])condition.value)[1]);
                    }
                    map.put(condition.type, ImmutableMap.of(condition.field, inner));
                }
            } else {
                map.put(condition.type, ImmutableMap.of(condition.field, condition.value));
            }
        }

        return map;
    }

    public static List<Map<String, Object>> toMap(List<ESQueryCondition> conditions){
        List<Map<String, Object>> list = new ArrayList<>();
        if (conditions != null){
            conditions.forEach(condition -> {
                Map<String, Object> map = toMap(condition);
                if (!CollectionUtils.isEmpty(map)){
                    list.add(map);
                }

            });
        }
        return list;
    }
}
