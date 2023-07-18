package com.binance.account.data.sharding.rule;

import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
@Component
public class KycFillInfoTableRule extends TableRuleSharding {

    public KycFillInfoTableRule() {
        super();
        this.setLogicTable("kyc_fill_info");
        this.setActualDataNodes("db1.kyc_fill_info_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
        this.setKeyGenerator(new DefaultKeyGenerator());
    }
}
