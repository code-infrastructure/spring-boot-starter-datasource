package com.faner.infrastructure.datasource.callback;

/**
 * 动态数据源预留回调接口。
 *
 * @作者 Faner
 * @创建时间 2021/12/31 20:42
 */
public interface DynamicDataSourceCallback {

    /**
     * @DS未指定注解的情况下,设置默认的DataSource.
     * 预留次接口,方便特殊场景下定制数据源路由策略。
     */
    void setDefaultDataSource();
}
