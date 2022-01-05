package com.faner.infrastructure.datasource.properties;

import lombok.Data;

/**
 * single rule configuration properties.
 * @作者 Faner
 * @创建时间 2021/12/31 20:59
 */
@Data
public class SingleRuleProperties {

    /**
     * 引用的ref
     */
    private String dataSourceRef;

}
