package com.faner.infrastructure.datasource.properties;

import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import lombok.Getter;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;

import java.util.Properties;

/**
 * Support properties from environment.
 * @作者 Faner
 * @创建时间 2021/12/31 21:00
 */
public class EnvironmentAwareProperties extends Properties {

    @Getter
    private final Properties properties;

    @Getter
    private final String prefix;

    public EnvironmentAwareProperties(String prefix, Properties properties) {
        this.prefix = prefix;
        this.properties = properties;
    }

    @Override
    public Object get(Object key) {

        if (isEnvironmentSupport(key)) {
            Object propertyValue = DataSourceUtils.getApplicationContext().getEnvironment()
                    .getProperty(this.prefix + key);
            if (propertyValue != null) {
                return propertyValue;
            }
        }

        return super.get(key);
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (isEnvironmentSupport(key)) {
            Object propertyValue = DataSourceUtils.getApplicationContext().getEnvironment()
                    .getProperty(this.prefix + key,
                            defaultValue != null ? defaultValue.toString() : null);
            if (propertyValue != null) {
                return propertyValue;
            }
        }
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public String getProperty(String key) {
        if (isEnvironmentSupport(key)) {
            String propertyValue = DataSourceUtils.getApplicationContext().getEnvironment()
                    .getProperty(this.prefix + key);
            if (propertyValue != null) {
                return propertyValue;
            }
        }

        return super.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        if (isEnvironmentSupport(key)) {
            String propertyValue = DataSourceUtils.getApplicationContext().getEnvironment()
                    .getProperty(this.prefix + key, defaultValue);
            if (propertyValue != null) {
                return propertyValue;
            }
        }
        return super.getProperty(key, defaultValue);
    }

    /**
     * Check if the key is configurable in environment.
     *
     * @param key the property key.
     * @return true if supported, or false.
     */
    private boolean isEnvironmentSupport(Object key) {
        for (ConfigurationPropertyKey each : ConfigurationPropertyKey.values()) {
            if (each.getKey().equals(key) || (each.getKey()+"-refresh").equals(key)) {
                return true;
            }
        }
        return false;
    }

}
