package com.faner.infrastructure.datasource.factory;

import com.faner.infrastructure.datasource.dynamic.DynamicDataSource;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * 动态数据源工厂
 * @作者 Faner
 * @创建时间 2021/12/31 21:40
 */
public class DynamicDataSourceFactory extends BaseDataSourceFactory{

    private Map<String,Boolean> dataSourceNames = Maps.newHashMap();

    private String defaultDataSourceName;

    public DynamicDataSourceFactory(Map<String,Boolean> dataSourceNames, String defaultDataSourceName){
        this.dataSourceNames = dataSourceNames;
        this.defaultDataSourceName = defaultDataSourceName;
    }

    @Override
    public DataSource getDataSource() throws SQLException {

        DataSource defaultDataSource = null;
        Map<Object,Object> targetDataSource = Maps.newHashMap();
        for(Map.Entry<String,Boolean> entry : dataSourceNames.entrySet()){

            DataSource dataSource = this.getApplicationContext()
                    .getBean(DataSourceUtils.normalizeDataSourceName(entry.getKey()), DataSource.class);
            Preconditions.checkState(dataSource!=null, "datasource [%s]", entry.getKey());
            targetDataSource.put(entry.getKey(), dataSource);

            /*
             * 1. The 1st is default datasource if no default data source.
             * 2. Set default datasource if found.
             */
            if(defaultDataSource == null || entry.getKey().equals(defaultDataSourceName)){
                defaultDataSource = dataSource;
            }
        }


        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSource);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

        return dynamicDataSource;
    }

}
