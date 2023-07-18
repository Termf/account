package com.binance.account.common.query.es;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * es 查询结果集
 * Created by Shining.Cai on 2018/09/20.
 **/
@Data
public class ESResultSet {
    /**
     * 总数
     */
    private int total;
    /**
     * 结果
     */
    private JSONArray hits;
}
