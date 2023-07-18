package com.binance.account.data.sharding.algorithm;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TableShardingUserIdAlgorithm implements PreciseShardingAlgorithm<Long> {

    /*
     * @SuppressWarnings("unchecked")
     * 
     * @Override public Collection<String> doSharding(Collection<String> availableTargetNames,
     * Collection<ShardingValue> shardingValues) { List<Integer> indexs = new ArrayList<Integer>(); for
     * (ShardingValue shardingValue : shardingValues) { if
     * (StringUtils.equals(shardingValue.getColumnName(), "user_id")) {// 账号hash分库 for (Long userId :
     * ((ListShardingValue<Long>) shardingValue).getValues()) { Long index = userId %
     * availableTargetNames.size(); if (!indexs.contains(index.intValue())) {
     * indexs.add(index.intValue()); } } } } if (indexs.size() < 0) { return availableTargetNames; }
     * final List<String> tables = new ArrayList<String>(availableTargetNames); List<String> result =
     * new ArrayList<String>(); indexs.forEach(e -> { result.add(tables.get(e)); }); return result; }
     */

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        final List<String> tables = new ArrayList<String>(availableTargetNames);
        Long index = shardingValue.getValue() % availableTargetNames.size();
        return tables.get(index.intValue());
    }

}
