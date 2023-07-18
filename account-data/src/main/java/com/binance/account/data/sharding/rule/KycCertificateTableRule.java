package com.binance.account.data.sharding.rule;

import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
@Component
public class KycCertificateTableRule extends TableRuleSharding {

    public KycCertificateTableRule() {
        super();
        this.setLogicTable("kyc_certificate");
        this.setActualDataNodes("db1.kyc_certificate_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGenerator(new DefaultKeyGenerator());
    }

}
