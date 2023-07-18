package com.binance.account.data.sharding.rule;

import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;

@Component
public class UserSecurityLogTableRule extends TableRuleSharding {

    public UserSecurityLogTableRule() {
        super();
        this.setLogicTable("user_security_log");
        this.setActualDataNodes("db1.user_security_log_${0..19}");
        // DefaultKeyGenerator.setWorkerId(workerId);
        // 选库规则
        // this.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("master_id",
        // "db_${master_id % 2}");
        // 选表规则
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id",new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
        this.setKeyGenerator(new DefaultKeyGenerator());
    }

}
