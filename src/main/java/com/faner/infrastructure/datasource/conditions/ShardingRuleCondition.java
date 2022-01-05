package com.faner.infrastructure.datasource.conditions;

import com.faner.infrastructure.datasource.consts.DataSourceConst;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Sharding rule condition.
 *x
 * @author taorz1
 */
public final class ShardingRuleCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean isShardingRule = PropertyUtil
                .containPropertyPrefix(conditionContext.getEnvironment(), DataSourceConst.DATA_SOURCE_RULES_SHARDING_PREFIX);
        boolean isMasterSlaveRule = new MasterSlaveRuleCondition().getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch();
        boolean isEncryptRule = new EncryptRuleCondition().getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch();
        return (isMasterSlaveRule || isEncryptRule || !isShardingRule) ? ConditionOutcome.noMatch("Have found master-slave or encrypt rule in environment") : ConditionOutcome.match();
    }
}

