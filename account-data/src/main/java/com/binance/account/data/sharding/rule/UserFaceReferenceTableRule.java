package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;

/**
 * @author liliang1
 * @date 2018-09-14 11:05
 */
@Component
public class UserFaceReferenceTableRule extends TableRuleSharding {

    public UserFaceReferenceTableRule() {
        super();
        this.setLogicTable("user_face_reference");
        this.setActualDataNodes("db1.user_face_reference_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
    }

}
