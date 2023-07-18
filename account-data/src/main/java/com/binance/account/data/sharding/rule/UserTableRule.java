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
public class UserTableRule extends TableRuleSharding {

    public UserTableRule() {
        super();
        this.setLogicTable("user");
        this.setActualDataNodes("db1.user_${0..19}");
        // DefaultKeyGenerator.setWorkerId(workerId);
        // 选库规则
        // this.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("master_id",
        // "db_${master_id % 2}");
        // 选表规则
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("email", new UserTableShardingAlgorithm()));
    }

    public static class UserTableShardingAlgorithm implements PreciseShardingAlgorithm<String> {

        @Override
        public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
            final List<String> tables = new ArrayList<String>(availableTargetNames);
            int index = Math.abs(HashAlgorithms.FNVHash1(shardingValue.getValue()) % availableTargetNames.size());
            return tables.get(index);
        }
    }

    public static void main(String[] args) {

    }

}
