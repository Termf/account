package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import org.springframework.stereotype.Component;

/**
 * @author liliang1
 * @date 2018-08-27 15:32
 */
@Component
public class UserSecurityResetFaceLogRule extends TableRuleSharding {

    public UserSecurityResetFaceLogRule() {
        super();
        this.setLogicTable("user_security_reset_face_log");
        this.setActualDataNodes("db1.user_security_reset_face_log_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
        this.setKeyGeneratorColumnName("id");
        this.setKeyGenerator(new DefaultKeyGenerator());
    }
}
