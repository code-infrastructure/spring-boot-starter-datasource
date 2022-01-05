package com.faner.infrastructure.datasource.factory;

import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import com.faner.infrastructure.datasource.properties.EnvironmentAwareProperties;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * 数据源工厂基类.
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:38
 */
@Getter
@Setter
public abstract class BaseDataSourceFactory implements DataSourceFactory {

    private ApplicationContext applicationContext;

    private Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
        DataSourceUtils.setApplicationContext(applicationContext);
    }


    protected Map<String, DataSource> getDataSourceMap(Map.Entry<String, DataSourceRuleProperties> rule) {
        Map<String, DataSource> dataSourceMap = Maps.newLinkedHashMap();
        DataSourceUtils.getDataSourcePoolBindings(this.getEnvironment()).keySet().stream()
                .forEach(dataSourceKey -> {

                    DataSource dataSource = this.getApplicationContext()
                            .getBean(DataSourceUtils.normalizeDataSourceName(dataSourceKey),
                                    DataSource.class);

                    if (rule.getValue().getEncryptRule() != null
                            && !(dataSource instanceof EncryptDataSource)) {
                        DataSource encryptDataSource = null;
                        try {
                            encryptDataSource = EncryptDataSourceFactory
                                    .createDataSource(dataSource,
                                            new EncryptRuleConfigurationYamlSwapper()
                                                    .swap(rule.getValue().getEncryptRule()),
                                            new EnvironmentAwareProperties(
                                                    DataSourceUtils.envAwarePropertyPrefix(rule.getKey()),
                                                    rule.getValue().getProps()));
                            dataSourceMap.put(dataSourceKey, encryptDataSource);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        dataSourceMap.put(dataSourceKey, dataSource);
                    }
                });
        return dataSourceMap;
    }

}

