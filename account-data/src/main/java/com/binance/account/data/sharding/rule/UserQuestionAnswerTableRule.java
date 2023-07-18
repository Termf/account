package com.binance.account.data.sharding.rule;

import org.springframework.stereotype.Component;

import com.binance.account.data.sharding.algorithm.TableShardingUserIdAlgorithm;
import com.binance.master.data.sharding.TableRuleSharding;

import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;

@Component
public class UserQuestionAnswerTableRule extends TableRuleSharding {

	public UserQuestionAnswerTableRule() {
		super();
		this.setLogicTable("user_question_answers");
		this.setActualDataNodes("db1.user_question_answers_${0..19}");
		this.setTableShardingStrategyConfig(
				new StandardShardingStrategyConfiguration("user_id", new TableShardingUserIdAlgorithm()));
		this.setKeyGeneratorColumnName("id");
		this.setKeyGenerator(new DefaultKeyGenerator());
	}
}