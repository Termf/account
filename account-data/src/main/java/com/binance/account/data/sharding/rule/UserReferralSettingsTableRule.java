package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class UserReferralSettingsTableRule extends TableRuleSharding {

    public UserReferralSettingsTableRule() {
        super();
        this.setLogicTable("user_referral_settings");
        this.setActualDataNodes("db1.user_referral_settings_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
        this.setKeyGenerator(new DefaultKeyGenerator());
    }
}
