package com.faner.infrastructure.datasource.factory;

import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 默认数据源工厂
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:39
 */
public class DefaultDataSourceFactory extends BaseDataSourceFactory{

    private String dataSourceKey;

    public DefaultDataSourceFactory(String dataSourceKey){
        this.dataSourceKey = dataSourceKey;
    }

    @Override
    public DataSource getDataSource() throws SQLException {
        DataSource dataSource = this.getApplicationContext()
                .getBean(DataSourceUtils.normalizeDataSourceName(dataSourceKey),
                        DataSource.class);

        return dataSource;
    }
}