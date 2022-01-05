package com.faner.infrastructure.datasource.factory;

import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import com.faner.infrastructure.datasource.properties.EnvironmentAwareProperties;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * 主从数据源工厂.
 * @作者 Faner
 * @创建时间 2021/12/31 21:43
 */
public class MasterSlaveDataSourceFactory extends BaseDataSourceFactory{

    private Map.Entry<String, DataSourceRuleProperties> rule;

    public MasterSlaveDataSourceFactory(Map.Entry<String,DataSourceRuleProperties> rule){
        Preconditions
                .checkNotNull(rule.getValue().getMasterSlaveRule(), "'masterSlaveRule' should not be null");
        this.rule = rule;
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        DataSource masterSlaveDataSource = org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory
                .createDataSource(getDataSourceMap(rule),
                        new MasterSlaveRuleConfigurationYamlSwapper()
                                .swap(rule.getValue().getMasterSlaveRule()),
                        new EnvironmentAwareProperties(
                                DataSourceUtils.envAwarePropertyPrefix(rule.getKey()),
                                rule.getValue().getProps()));

        return masterSlaveDataSource;
    }
}
