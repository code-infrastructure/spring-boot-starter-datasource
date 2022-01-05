package com.faner.infrastructure.datasource.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.faner.infrastructure.datasource.consts.DataSourceConst;
import com.faner.infrastructure.datasource.dynamic.DynamicDataSource;
import com.faner.infrastructure.datasource.properties.DataSourceProperties;
import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import lombok.experimental.UtilityClass;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Collections.emptyMap;

/**
 * 数据源工具类
 *
 * @作者 Faner
 * @创建时间 2021/12/31 20:35
 */
@UtilityClass
public class DataSourceUtils {

    /**
     * 是否设置默认数据源。
     */
    private static final ThreadLocal<Boolean> CUSTOMIZED_DATASOURCE_FLAG_HOLDER = new ThreadLocal<Boolean>(){
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };


    /**
     * 应用上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 正规化数据源名称，数据源名称一定是以DataSource为后缀的
     *
     * @param name 数据源名称
     * @return 数据源名称
     */
    public static String normalizeDataSourceName(String name) {
        if (StringUtils.isEmpty(name) || DataSourceConst.DATA_SOURCE_NAME.equals(name)) {
            return DataSourceConst.DATA_SOURCE_NAME;
        }

        if (name.endsWith(DataSourceConst.DATA_SOURCE_NAME_SUFFIX)) {
            return name;
        }

        return name + DataSourceConst.DATA_SOURCE_NAME_SUFFIX;
    }

    /**
     * 取得原始数据源名称
     *
     * @param dsName 原始数据源名称是渠道DataSource后缀的部分
     * @return 数据源名称
     */
    public static String parseDataSourceKey(String dsName) {
        if (dsName.endsWith(DataSourceConst.DATA_SOURCE_NAME_SUFFIX)) {
            return dsName.substring(0, dsName.indexOf(DataSourceConst.DATA_SOURCE_NAME_SUFFIX));
        }

        return dsName;
    }

    /**
     * 获取数据源绑定信息
     *
     * @param environment 环境上下文
     * @return 数据源绑定信息
     */
    public static Map<String, Object> getDataSourcePoolBindings(Environment environment) {
        return Binder.get(environment)
                .bind(DataSourceConst.DATA_SOURCE_POOL_PREFIX,
                        Bindable.mapOf(String.class, Object.class))
                .orElse(emptyMap());
    }

    /**
     * 获取数据源规则绑定信息
     *
     * @param environment 环境上下文
     * @return 数据源绑定信息
     */
    public static Map<String, DataSourceRuleProperties> getDataSourceRuleBindings(Environment environment) {
        return Binder.get(environment)
                .bind(DataSourceConst.DATA_SOURCE_RULES_PREFIX,
                        Bindable.mapOf(String.class, DataSourceRuleProperties.class))
                .orElse(emptyMap());
    }

    /**
     * 绑定数据源
     *
     * @param environment 环境上下文
     * @param dataSourceName 数据源名称
     * @param dataSourceInstance 数据源实例
     */
    public static void bindDataSourceInstance(Environment environment, String dataSourceName,
                                              DataSource dataSourceInstance) {
        Binder.get(environment)
                .bind(dataSourceName,
                        Bindable.ofInstance(dataSourceInstance));
    }

    /**
     * 获取数据源配置
     *
     * @param applicationContext
     * @return
     */
    public static DataSourceProperties getDataSourceProperties(ApplicationContext applicationContext){
        return applicationContext.getBean(DataSourceProperties.class);
    }

    /**
     * 设置应用上下文
     *
     * @param applicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext){
        DataSourceUtils.applicationContext = applicationContext;
    }

    /**
     * 获取应用上下文
     *
     * @return
     */
    public static ApplicationContext getApplicationContext(){
        return DataSourceUtils.applicationContext;
    }

    /**
     * Construct environment aware property prefix.
     *
     * @param ruleKey
     * @return
     */
    public static String envAwarePropertyPrefix(String ruleKey){
        return "datasource.rules."+ruleKey+".props.";
    }

    /**
     * Get default datasource for dynamic datasource.
     * @return
     */
    public static String getDynamicDefaultDataSource(){
        return getApplicationContext().getEnvironment().getProperty("datasource.dynamic-default");
    }

    /**
     * 是否设置默认数据源。
     *
     * @return
     */
    public static Boolean useCustomizedDataSource(){
        return CUSTOMIZED_DATASOURCE_FLAG_HOLDER.get();
    }

    /**
     * 设置设置默认数据源标记。
     */
    public static void setCustomizedDataSourceFlag(){
        CUSTOMIZED_DATASOURCE_FLAG_HOLDER.set(Boolean.TRUE);
    }

    /**
     * 清理设置默认数据源标记。
     */
    public static void clearCustomizedDataSourceFlag(){
        CUSTOMIZED_DATASOURCE_FLAG_HOLDER.set(Boolean.FALSE);
    }



    /**
     * 获取JDBC URL.
     *
     * @param dataSource
     * @return
     */
    public static String getJdbcUrl(DataSource dataSource) {
        DataSource ds = dataSource;
        if (dataSource instanceof DynamicDataSource) {
            ds = ((DynamicDataSource) dataSource).getTargetDataSource();
        }


        StringJoiner sj = new StringJoiner("|");
        if (ds instanceof MasterSlaveDataSource) {
            ((MasterSlaveDataSource) ds).getDataSourceMap().entrySet().stream().findAny().ifPresent(entry -> sj.add(getRawJdbcUrl(entry.getValue())));
        } else if (ds instanceof ShardingDataSource) {
            ((ShardingDataSource) ds).getDataSourceMap().entrySet().stream().findAny().ifPresent(entry -> sj.add(getRawJdbcUrl(entry.getValue())));
        } else if((ds instanceof EncryptDataSource)){
            sj.add(getRawJdbcUrl(((EncryptDataSource)ds).getDataSource()));
        } else if (ds instanceof DruidDataSource) {
            sj.add(getRawJdbcUrl(ds));
        }

        return sj.toString();

    }

    private static String getRawJdbcUrl(DataSource dataSource) {
        if (dataSource != null && dataSource instanceof DruidDataSource) {
            return ((DruidDataSource) dataSource).getRawJdbcUrl();
        }
        return "";
    }

}

