package com.faner.infrastructure.datasource.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Druid数据源包装类
 * @作者 Faner
 * @创建时间 2021/12/31 20:49
 */
public class DruidDataSourceWrapper extends DruidDataSource {
    /**
     * 构造方法初始化连接池参数
     */
    public DruidDataSourceWrapper() {
        super();
        // 初始池大小15，在连接池初始化时建立
        super.setInitialSize(15);
        // 连接池内最小Idle连接数量
        super.setMinIdle(15);
        // 最大连接数，30
        super.setMaxActive(30);
        // 获取连接最长等待时间，300ms，快速失败
        super.setMaxWait(300);
        // 对Idle的连接进行连接测试，确保连接的可用性
        super.setTestWhileIdle(true);
        // 获取连接和归还连接时无需测试，提高效率
        super.setTestOnBorrow(false);
        super.setTestOnReturn(false);
        super.setValidationQuery("SELECT 1");
        // 启用连接KeepAlive，每隔120s触发一次Keepalive检查
        super.setKeepAlive(true);
        super.setKeepAliveBetweenTimeMillis(120_000);
        // 池中Idle超过300s的连接才会被回收
        super.setMinEvictableIdleTimeMillis(300_000);
        // 每隔60s，执行一次过期连接回收
        super.setTimeBetweenEvictionRunsMillis(60_000);
    }

    @Autowired(required = false)
    public void autoAddFilters(List<Filter> filters) {
        super.setProxyFilters(filters);
    }

    @Override
    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
        try {
            super.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
        } catch (IllegalArgumentException ignore) {
            super.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
        }
    }
}
