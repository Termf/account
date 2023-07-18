package com.binance.account.data.sharding.rule;

import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;

@Component
public class UserIpChangeTableRule extends TableRuleSharding {

    public UserIpChangeTableRule() {
        super();
        this.setLogicTable("user_ip_change");
        this.setActualDataNodes("db1.user_ip_change_${0..19}");
        // DefaultKeyGenerator.setWorkerId(workerId);
        // 选库规则
        // this.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("master_id",
        // "db_${master_id % 2}");
        // 选表规则

        this.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
    }
}