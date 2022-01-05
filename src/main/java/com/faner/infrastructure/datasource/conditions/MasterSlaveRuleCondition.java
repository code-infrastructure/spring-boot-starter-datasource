package com.faner.infrastructure.datasource.conditions;

import com.faner.infrastructure.datasource.consts.DataSourceConst;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Master-slave condition.
 *
 * @author taorz1
 */
public final class MasterSlaveRuleCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        return PropertyUtil
                .containPropertyPrefix(conditionContext.getEnvironment(), DataSourceConst.DATA_SOURCE_RULES_MASTER_SLAVE_PREFIX)
                ? ConditionOutcome.match() : ConditionOutcome.noMatch("Can't find ShardingSphere master-slave rule configuration in environment.");
    }
}
