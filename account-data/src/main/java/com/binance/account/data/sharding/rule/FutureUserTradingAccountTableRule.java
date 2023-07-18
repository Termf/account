package com.binance.account.data.sharding.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;

@Component
public class FutureUserTradingAccountTableRule extends TableRuleSharding {

    public FutureUserTradingAccountTableRule() {
        super();
        this.setLogicTable("future_user_trading_account");
        this.setActualDataNodes("db1.future_user_trading_account_${0..19}");
        // DefaultKeyGenerator.setWorkerId(workerId);
        // 选库规则
        // this.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("master_id",
        // "db_${master_id % 2}");
        // 选表规则
        this.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("trading_account",
                new UserTradingAccountTableShardingAlgorithm()));
    }

    public static class UserTradingAccountTableShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

        @Override
        public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
            final List<String> tables = new ArrayList<String>(availableTargetNames);
            Long index = shardingValue.getValue() % availableTargetNames.size();
            return tables.get(index.intValue());
        }
    }
}
