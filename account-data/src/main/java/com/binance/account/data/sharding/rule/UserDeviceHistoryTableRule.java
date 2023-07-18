package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;

@Component
public class UserDeviceHistoryTableRule extends TableRuleSharding {

    public UserDeviceHistoryTableRule() {
        super();
        this.setLogicTable("user_device_history");
        this.setActualDataNodes("db1.user_device_history_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
    }

}
