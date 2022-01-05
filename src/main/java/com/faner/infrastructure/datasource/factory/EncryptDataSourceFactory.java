
package com.faner.infrastructure.datasource.factory;


import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * 加密组件BeanFactory
 *
 * NOTE:正常不会走进这个factory中
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:42
 */
public class EncryptDataSourceFactory extends BaseDataSourceFactory{

    private Map.Entry<String, DataSourceRuleProperties> rule;

    public EncryptDataSourceFactory(Map.Entry<String,DataSourceRuleProperties> rule){
        this.rule = rule;
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        return getDataSourceMap(rule).get(rule.getKey());
    }
}
