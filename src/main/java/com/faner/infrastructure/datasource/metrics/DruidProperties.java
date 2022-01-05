package com.faner.infrastructure.datasource.metrics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @作者 Faner
 * @创建时间 2021/12/31 21:19
 */
@Data
@ConfigurationProperties(prefix = "metrics.druid")
public class DruidProperties {

    @NestedConfigurationProperty
    private SqlProperties sql = new SqlProperties();

    private boolean enabled;

    @Data
    public static class SqlProperties {

        private boolean enable;
    }

}