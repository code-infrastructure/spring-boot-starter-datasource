package com.faner.infrastructure.datasource.autoconfigure;


import com.alibaba.druid.pool.DruidDataSource;
import com.faner.infrastructure.datasource.metrics.DruidDataSourceMetrics;
import com.faner.infrastructure.datasource.metrics.DruidProperties;
import com.faner.infrastructure.datasource.metrics.OnlyOnceLoggingDenyMeterFilter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Druid数据源监控指标配置
 * @作者 Faner
 * @创建时间 2021/12/31 20:53
 */
@Configuration
@ConditionalOnClass(name = {"com.alibaba.druid.pool.DruidDataSource"})
@AutoConfigureAfter(name = {
        "io.micrometer.spring.autoconfigure.MetricsAutoConfiguration",
        "io.micrometer.spring.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration",
        "com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure",
})
@EnableConfigurationProperties(DruidProperties.class)
@ConditionalOnProperty(name = "metrics.druid.enabled", matchIfMissing = true)
public class DruidDataSourceMetricsConfiguration {

    private static final String DATASOURCE_SUFFIX = "dataSource";


    @Autowired(required = false)
    public void bindDataSourcesToRegistry(MeterRegistry registry, Map<String, DataSource> dataSources, DruidProperties druidProperties) {
        if (dataSources != null && !dataSources.isEmpty()) {
            dataSources.forEach((beanName, dataSource) -> {
                if (dataSource instanceof DruidDataSource) {
                    String dataSourceName = getDataSourceName(beanName);
                    new DruidDataSourceMetrics((DruidDataSource) dataSource, dataSourceName
                            , druidProperties)
                            .bindTo(registry);
                }
            });
        }
    }

    /**
     * 设置SQL总数上限的过滤器，防止采集的指标过多，默认包含SQL Tag的指标，最多采集不超过200个SQL
     *
     * @return 指标总数过滤器
     */
    @Bean
    @Order(-2)
    public MeterFilter metricsSQLTagFilter() {
        MeterFilter denyFilter = new OnlyOnceLoggingDenyMeterFilter(() -> "Reached the maximum number of sql tags for " +
                "druid.sql.execute.*, stop collecting more.");

        return MeterFilter.maximumAllowableTags("druid.sql.execute", "sql", 200, denyFilter);
    }

    /**
     * 根据Bean名称取得数据源名称
     *
     * @param beanName bean名称
     * @return 数据源名称
     */
    private String getDataSourceName(String beanName) {
        if (beanName.length() > DATASOURCE_SUFFIX.length()
                && StringUtils.endsWithIgnoreCase(beanName, DATASOURCE_SUFFIX)) {
            return beanName.substring(0,
                    beanName.length() - DATASOURCE_SUFFIX.length());
        }
        return beanName;
    }
}

