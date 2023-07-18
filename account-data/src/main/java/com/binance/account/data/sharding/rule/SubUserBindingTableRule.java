package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@Component
public class SubUserBindingTableRule extends TableRuleSharding {

    public SubUserBindingTableRule() {
        super();
        this.setLogicTable("sub_user_binding");
        this.setActualDataNodes("db1.sub_user_binding_${0..19}");
        this.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("parent_user_id", new TableShardingUserIdAlgorithm()));
    }

}