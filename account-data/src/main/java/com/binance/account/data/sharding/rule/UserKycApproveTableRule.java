package com.binance.account.data.sharding.rule;

import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;

@Component
public class UserKycApproveTableRule extends TableRuleSharding {

    public UserKycApproveTableRule() {
        super();
        this.setLogicTable("user_kyc_approve");
        this.setActualDataNodes("db1.user_kyc_approve_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
    }


}
