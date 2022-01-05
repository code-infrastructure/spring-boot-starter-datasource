package com.faner.infrastructure.datasource.factory;

import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import com.faner.infrastructure.datasource.properties.EnvironmentAwareProperties;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * 分库分表数据源工厂.
 * @作者 Faner
 * @创建时间 2021/12/31 21:44
 */
public class ShardingDataSourceFactory extends BaseDataSourceFactory{

    private Map.Entry<String, DataSourceRuleProperties> rule;

    public ShardingDataSourceFactory(Map.Entry<String,DataSourceRuleProperties> rule){
        Preconditions.checkNotNull(rule.getValue().getShardingRule(), "'shardingRule' should not be null");
        this.rule = rule;
    }

    @Override
    public DataSource getDataSource() throws SQLException {

        DataSource shardingDataSource = org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory
                .createDataSource(getDataSourceMap(rule),
                        new ShardingRuleConfigurationYamlSwapper()
                                .swap(rule.getValue().getShardingRule()),
                        new EnvironmentAwareProperties(
                                DataSourceUtils.envAwarePropertyPrefix(rule.getKey()),
                                rule.getValue().getProps()));

        return shardingDataSource;
    }

}

