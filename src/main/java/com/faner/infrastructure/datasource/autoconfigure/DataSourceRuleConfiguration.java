package com.faner.infrastructure.datasource.autoconfigure;

import com.faner.infrastructure.datasource.consts.DataSourceConst;
import com.faner.infrastructure.datasource.properties.DataSourceProperties;
import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * 数据源配置类
 * @作者 Faner
 * @创建时间 2021/12/31 20:51
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@ConditionalOnProperty(prefix = DataSourceConst.DATA_SOURCE_RULES_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@AutoConfigureAfter(DataSourcePoolConfiguration.class)
@RequiredArgsConstructor
public class DataSourceRuleConfiguration implements EnvironmentAware, ApplicationContextAware {

    /**
     * 规则配置
     */
    @Autowired
    private DataSourceProperties dataSourceProperties;

    /**
     * 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 数据源链接池binding信息
     */
    private Map<String, Object> dataSourcePoolBindings = Maps.newLinkedHashMap();

    /**
     * 数据源规则binding信息
     */
    private Map<String, DataSourceRuleProperties> dataSourceRuleBindings = Maps.newLinkedHashMap();

    /**
     * 环境上下文
     */
    private Environment environment;


    @Override
    public final void setEnvironment(final Environment environment) {
        this.environment = environment;
        this.dataSourcePoolBindings = DataSourceUtils.getDataSourcePoolBindings(environment);
        this.dataSourceRuleBindings = DataSourceUtils.getDataSourceRuleBindings(environment);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}

