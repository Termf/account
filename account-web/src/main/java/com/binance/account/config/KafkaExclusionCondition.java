package com.binance.account.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @description ug不装载kafka配置
 * @auther renyeqiao
 * @create 2019-12-03 16:12
 */
public class KafkaExclusionCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        if ("ug".equalsIgnoreCase(conditionContext.getEnvironment().getProperty("TARGET_EXCHANGE"))) {
            return false;
        }
        return true;
    }

}
