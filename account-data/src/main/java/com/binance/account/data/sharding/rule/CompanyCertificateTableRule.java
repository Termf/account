package com.binance.account.data.sharding.rule;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

@Component
public class CompanyCertificateTableRule extends TableRuleSharding {

    public CompanyCertificateTableRule() {
        super();
        this.setLogicTable("company_certificate");
        this.setActualDataNodes("db1.company_certificate_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
        this.setKeyGenerator(new DefaultKeyGenerator());
    }
}
