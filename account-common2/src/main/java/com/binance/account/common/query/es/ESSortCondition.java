package com.binance.account.common.query.es;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排序条件（<a href="https://www.elastic.co/guide/en/elasticsearch/reference/6.4/search-request-sort.html">参考</a>）
 * Created by Shining.Cai on 2018/11/27.
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESSortCondition {


    public static final String DESC = "desc";
    public static final String ASC = "asc";

    /**
     * 排序字段
     */
    private String field;
    /**
     * 排序类型
     */
    private Object value;

    public static ESSortCondition asc(String field){
        return new ESSortCondition(field, ASC);
    }

    public static ESSortCondition desc(String field){
        return new ESSortCondition(field, DESC);
    }

    public static List<Map<String, Object>> toMap(List<ESSortCondition> conditions){
        List<Map<String, Object>> list = new ArrayList<>();
        if (conditions != null){
            conditions.forEach(condition -> {
                if (condition.field!=null && condition.value!=null){
                    list.add(ImmutableMap.of(condition.field, condition.value));
                }
            });
        }
        return list;
    }
}
