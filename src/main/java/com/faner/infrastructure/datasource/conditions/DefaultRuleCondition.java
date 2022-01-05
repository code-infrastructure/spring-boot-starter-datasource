package com.faner.infrastructure.datasource.conditions;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Default Condition.
 *
 */
public class DefaultRuleCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean isMasterSlaveRule = new MasterSlaveRuleCondition().getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch();
        boolean isEncryptRule = new EncryptRuleCondition().getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch();
        boolean isShardingRule = new ShardingRuleCondition().getMatchOutcome(conditionContext, annotatedTypeMetadata).isMatch();
        return (isMasterSlaveRule || isEncryptRule || isShardingRule) ? ConditionOutcome.noMatch("Have found encrypt, master-slave, or sharding rule in environment") : ConditionOutcome.match();
    }

}

