package com.faner.infrastructure.datasource.properties;


import com.faner.infrastructure.datasource.druid.DruidDataSourceWrapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置属性
 * @作者 Faner
 * @创建时间 2021/12/31 20:56
 */
@Data
@ConfigurationProperties("datasource")
@Component
public class DataSourceProperties {

    /**
     * 数据源连接池，可配置多个数据源
     */
    private Map<String, DruidDataSourceWrapper> pool = new HashMap<>();

    /**
     * 数据源规则配置，可以配置多规则
     */
    private Map<String, DataSourceRuleProperties> rules = new HashMap<>();

    /**
     * 主数据源
     */
    private String primary = "default";

    /**
     * 是否自动设置
     */
    private boolean autoSetPrimary = true;

    /**
     * Druid数据源进行KeepAlive是否使用ping,默认关闭,
     * 在代理情况可能会出现连接断开的情况。
     */
    private boolean druidKeepAliveUsePing = false;

}
