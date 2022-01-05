package com.faner.infrastructure.datasource.factory;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据源工厂接口
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:38
 */
public interface DataSourceFactory extends ApplicationContextAware, EnvironmentAware {

    /**
     * 创建数据源
     *
     * @return
     * @throws SQLException
     */
    DataSource getDataSource() throws SQLException;

}