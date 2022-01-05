/**
 * @作者 Faner
 * @创建时间 2021/12/31 21:24
 */
package com.faner.infrastructure.datasource.dynamic;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * 动态数据源.
 *
 * @author taorz1
 * @date 2020/6/5
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceHolder.getDataSource();
    }

    public DataSource getTargetDataSource(){
        return this.determineTargetDataSource();
    }
}
