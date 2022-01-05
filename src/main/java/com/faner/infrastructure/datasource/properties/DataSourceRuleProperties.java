package com.faner.infrastructure.datasource.properties;

import lombok.Data;

import java.util.Properties;

/**
 * 数据源规则配置
 * @作者 Faner
 * @创建时间 2021/12/31 20:58
 */
@Data
public class DataSourceRuleProperties {

    /**
     * 单表规则
     */
    private SingleRuleProperties singleRule;

    /**
     * 分库分表配置规则
     */
    private ShardingRuleProperties shardingRule;

    /**
     * 主从配置规则
     */
    private MasterSlaveRuleProperties masterSlaveRule;

    /**
     * 数据脱敏规则
     */
    private EncryptRuleProperties encryptRule;

    /**
     * 扩展属性
     */
    private Properties props;
}
