package com.binance.account.common.query.es;

/**
 * 查询条件
 * Created by Shining.Cai on 2018/09/20.
 **/
public interface ESQueryConst {

    String TYPE_MUST = "must";
    String TYPE_MUST_NOT = "must_not";

    String TYPE_SHOULD = "should";
    String TYPE_MINIMUM_SHOULD_MATCH = "minimum_should_match";
    String TYPE_FILTER = "filter";

    String TYPE_QUERY = "query";
    String TYPE_SORT = "sort";

    String LIMIT_FROM = "from";
    String LIMIT_SIZE = "size";

}
