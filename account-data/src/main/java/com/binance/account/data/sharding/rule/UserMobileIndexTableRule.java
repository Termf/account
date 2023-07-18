package com.binance.account.data.sharding.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.binance.master.data.sharding.TableRuleSharding;
import com.binance.master.utils.HashAlgorithms;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;

@Component
public class UserMobileIndexTableRule extends TableRuleSharding {

    public UserMobileIndexTableRule() {
        super();
        this.setLogicTable("user_mobile_index");
        this.setActualDataNodes("db1.user_mobile_index_${0..19}");
        // DefaultKeyGenerator.setWorkerId(workerId);
        // 选库规则
        // this.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("master_id",
        // "db_${master_id % 2}");
        // 选表规则
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("mobile", new UserTableShardingAlgorithm()));
    }

    public static class UserTableShardingAlgorithm implements PreciseShardingAlgorithm<String> {

        // @SuppressWarnings("unchecked")
        // @Override
        // public Collection<String> doSharding(Collection<String> availableTargetNames,
        // Collection<ShardingValue> shardingValues) {
        // List<Integer> indexs = new ArrayList<Integer>();
        // for (ShardingValue shardingValue : shardingValues) {
        // if (StringUtils.equals(shardingValue.getColumnName(), "mobile")) {// 手机hash分库
        // for (String account : ((ListShardingValue<String>) shardingValue).getValues()) {
        // int index = Math.abs(HashAlgorithms.FNVHash1(account) % availableTargetNames.size());
        // if (!indexs.contains(index)) {
        // indexs.add(index);
        // }
        // }
        // }
        // }
        // if (indexs.size() < 0) {
        // return availableTargetNames;
        // }
        // final List<String> tables = new ArrayList<String>(availableTargetNames);
        // List<String> result = new ArrayList<String>();
        // indexs.forEach(e -> {
        // result.add(tables.get(e));
        // });
        // return result;
        // }

        @Override
        public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
            final List<String> tables = new ArrayList<String>(availableTargetNames);
            int index = Math.abs(HashAlgorithms.FNVHash1(shardingValue.getValue()) % availableTargetNames.size());
            return tables.get(index);
        }
    }

}
