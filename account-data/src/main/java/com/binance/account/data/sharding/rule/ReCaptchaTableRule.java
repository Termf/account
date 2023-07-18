package com.binance.account.data.sharding.rule;

import com.binance.master.data.sharding.TableRuleSharding;
import com.binance.master.utils.HashAlgorithms;
import io.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mengjuan on 2018/11/21.
 */
@Component
public class ReCaptchaTableRule extends TableRuleSharding{
    public ReCaptchaTableRule() {
        super();
        this.setLogicTable("re_captcha");
        this.setActualDataNodes("db1.re_captcha_${0..19}");
        this.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("email", new UserTableShardingAlgorithm()));
    }

    public static class UserTableShardingAlgorithm implements PreciseShardingAlgorithm<String> {
        @Override
        public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
            final List<String> tables = new ArrayList<String>(availableTargetNames);
            int index = Math.abs(HashAlgorithms.FNVHash1(shardingValue.getValue()) % availableTargetNames.size());
            return tables.get(index);
        }
    }
}