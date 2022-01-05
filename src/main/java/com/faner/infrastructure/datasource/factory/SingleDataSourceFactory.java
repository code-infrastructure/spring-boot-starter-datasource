package com.faner.infrastructure.datasource.factory;

import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import com.google.common.base.Preconditions;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * 单数据源工厂
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:45
 */
public class SingleDataSourceFactory extends BaseDataSourceFactory{

    private Map.Entry<String, DataSourceRuleProperties> rule;

    public SingleDataSourceFactory(Map.Entry<String,DataSourceRuleProperties> rule){
        Preconditions
                .checkNotNull(rule.getValue().getSingleRule(), "'singleRule' should not be null");
        this.rule = rule;
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        DataSource dataSource = this.getDataSourceMap(rule).get(rule.getValue().getSingleRule().getDataSourceRef());
        Preconditions.checkNotNull(dataSource, "'data-source-ref' %s for single-rule %s ", rule.getValue().getSingleRule().getDataSourceRef(),rule.getKey());
        return dataSource;
    }
}
