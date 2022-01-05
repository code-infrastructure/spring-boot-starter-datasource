package com.faner.infrastructure.datasource.conditions;

import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * Encrypt condition.
 *
 * @author taorz1
 */
public final class EncryptRuleCondition extends SpringBootCondition {

    private static final String ENCRYPT_ENCRYPTORS_PREFIX = "datasource.rules.encrypt.encryptors";

    private static final String ENCRYPT_TABLES_PREFIX = "datasource.rules.encrypt.tables";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean isEncrypt = PropertyUtil.containPropertyPrefix(conditionContext.getEnvironment(), ENCRYPT_ENCRYPTORS_PREFIX)
                && PropertyUtil.containPropertyPrefix(conditionContext.getEnvironment(), ENCRYPT_TABLES_PREFIX);
        return isEncrypt ? ConditionOutcome.match() : ConditionOutcome.noMatch("Can't find ShardingSphere encrypt rule configuration in environment.");
    }
}
