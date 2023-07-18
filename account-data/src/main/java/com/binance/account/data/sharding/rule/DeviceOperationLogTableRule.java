package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;


@Component
public class DeviceOperationLogTableRule extends TableRuleSharding {

    public DeviceOperationLogTableRule() {
        super();
        this.setLogicTable("device_operation_log");
        this.setActualDataNodes("db1.device_operation_log_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
    }
}
