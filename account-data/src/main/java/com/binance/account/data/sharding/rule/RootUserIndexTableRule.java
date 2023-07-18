package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;

@Component
public class RootUserIndexTableRule extends TableRuleSharding {

    public RootUserIndexTableRule() {
        super();
        this.setLogicTable("root_user_index");
        this.setActualDataNodes("db1.root_user_index_${0..19}");
        // DefaultKeyGenerator.setWorkerId(workerId);
        // 选库规则
        // this.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("master_id",
        // "db_${master_id % 2}");
        // 选表规则
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
    }

}
