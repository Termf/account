package com.binance.account.data.sharding.rule;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;

/**
 * Created by pcx
 */
@Component
public class IsolatedMarginUserBindingTableRule extends TableRuleSharding {

    public IsolatedMarginUserBindingTableRule() {
        super();
        this.setLogicTable("isolated_margin_user_binding");
        this.setActualDataNodes("db1.isolated_margin_user_binding_${0..19}");
        this.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("root_user_id", new TableShardingUserIdAlgorithm()));
    }

}