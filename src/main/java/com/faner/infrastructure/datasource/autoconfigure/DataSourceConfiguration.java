package com.faner.infrastructure.datasource.autoconfigure;


import com.alibaba.druid.pool.DruidDataSource;
import com.faner.infrastructure.datasource.callback.DynamicDataSourceCallback;
import com.faner.infrastructure.datasource.consts.DataSourceConst;
import com.faner.infrastructure.datasource.druid.DruidDataSourceWrapper;
import com.faner.infrastructure.datasource.dynamic.DynamicDataSourceAnnotationAdvisor;
import com.faner.infrastructure.datasource.dynamic.DynamicDataSourceAnnotationInterceptor;
import com.faner.infrastructure.datasource.dynamic.DynamicDataSourceHolder;
import com.faner.infrastructure.datasource.factory.DataSourceFactory;
import com.faner.infrastructure.datasource.factory.DefaultDataSourceFactory;
import com.faner.infrastructure.datasource.factory.DynamicDataSourceFactory;
import com.faner.infrastructure.datasource.properties.DataSourceRuleProperties;
import com.faner.infrastructure.datasource.transaction.DataSourceTransactionalAnnotationAdvisor;
import com.faner.infrastructure.datasource.transaction.DataSourceTransactionalAnnotationInterceptor;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据源配置。
 *
 * @作者 Faner
 * @创建时间 2021/12/31 20:40
 */
@Slf4j
@Configuration
@AutoConfigureAfter({
        DataSourcePoolConfiguration.class,
        DataSourceRuleConfiguration.class
})
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import({DataSourceConfiguration.DruidDataSourceBeanPostProcessor.class,
        DataSourceConfiguration.DataSourceRegistrar.class})
public class DataSourceConfiguration {

    /**
     * 注册数据源
     *
     * @author trang
     */
    static class DataSourceRegistrar implements EnvironmentAware, BeanFactoryPostProcessor,
            Ordered {

        private Environment environment = null;

        private AtomicBoolean registered = new AtomicBoolean(false);

        @Override
        public void postProcessBeanFactory(
                ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

            if (configurableListableBeanFactory instanceof BeanDefinitionRegistry && !registered
                    .get()) {
                registerBeanDefinitions((BeanDefinitionRegistry) configurableListableBeanFactory);
                registered.set(true);
            }
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        private void registerBeanDefinitions(BeanDefinitionRegistry registry) {
            Boolean autoSetPrimary = environment
                    .getProperty("datasource.auto-set-primary", Boolean.class, Boolean.TRUE);
            Boolean dynamicDataSource = environment
                    .getProperty("datasource.use-dynamic", Boolean.class, Boolean.FALSE);
            String primaryDataSource = environment
                    .getProperty("datasource.primary", String.class);

            // set 'druid.mysql.usePingMethod' value(default false
            // to avoid 'Communications link failure' when access mysql by proxy)
            Boolean druidKeepAliveUsePing = environment
                    .getProperty("datasource.druid-keep-alive-use-ping", Boolean.class, Boolean.FALSE);
            System.getProperties()
                    .setProperty("druid.mysql.usePingMethod", String.valueOf(druidKeepAliveUsePing));


            Map<String, Boolean> dependencyRoots = Maps.newHashMap();
            Map<String, Object> dataSourcePools = DataSourceUtils
                    .getDataSourcePoolBindings(environment);
            Map<String, DataSourceRuleProperties> dataSourceRules = DataSourceUtils
                    .getDataSourceRuleBindings(environment);
            dataSourcePools.keySet().forEach(key -> {
                val dsName = DataSourceUtils.normalizeDataSourceName(key);
                val builder = BeanDefinitionBuilder
                        .genericBeanDefinition(DruidDataSourceWrapper.class)
                        .setInitMethodName("init")
                        .setDestroyMethodName("close");

                AbstractBeanDefinition bd = builder
                        .getBeanDefinition();

                // 默认是根
                dependencyRoots.put(key, Boolean.TRUE);
                if (Boolean.TRUE.equals(autoSetPrimary) && key.equals(primaryDataSource)) {
                    bd.setPrimary(true);
                }

                registry.registerBeanDefinition(dsName, bd);

                log.info("LoadDataSource: {}", dsName);
            });

            // 根据Rules设置注册bean
            for (Entry<String, DataSourceRuleProperties> rule : dataSourceRules.entrySet()) {

                String dataSourceFactoryBeanName = null;
                Class<? extends DataSourceFactory> dataSourceFactoryBeanClass = null;

                if (rule.getValue().getShardingRule() != null) {
                    dataSourceFactoryBeanClass = com.faner.infrastructure.datasource.factory.ShardingDataSourceFactory.class;
                } else if (rule.getValue().getMasterSlaveRule() != null) {
                    dataSourceFactoryBeanClass = com.faner.infrastructure.datasource.factory.MasterSlaveDataSourceFactory.class;
                } else if (rule.getValue().getSingleRule() != null) {
                    dataSourceFactoryBeanClass = com.faner.infrastructure.datasource.factory.SingleDataSourceFactory.class;
                } else if (rule.getValue().getEncryptRule() != null) {
                    log.warn(
                            "Should NOT enter this condition for rule:{}, please check your configuration",
                            rule.getKey());
                    dataSourceFactoryBeanClass = com.faner.infrastructure.datasource.factory.EncryptDataSourceFactory.class;
                }

                dataSourceFactoryBeanName =
                        rule.getKey() + dataSourceFactoryBeanClass.getSimpleName();
                BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition(dataSourceFactoryBeanClass);
                factoryBuilder.addConstructorArgValue(rule);
                registry.registerBeanDefinition(dataSourceFactoryBeanName,
                        factoryBuilder.getBeanDefinition());

                BeanDefinition bd = new GenericBeanDefinition();
                bd.setFactoryBeanName(dataSourceFactoryBeanName);
                bd.setFactoryMethodName("getDataSource");

                String dataSourceName = DataSourceUtils.normalizeDataSourceName(rule.getKey());
                registry.registerBeanDefinition(dataSourceName, bd);

                dependencyRoots.put(rule.getKey(), Boolean.TRUE);

                rectifyDependencyRoots(rule, dependencyRoots);

                if (Boolean.TRUE.equals(autoSetPrimary) && rule.getKey()
                        .equals(primaryDataSource)) {
                    bd.setPrimary(true);
                }
            }

            if (registry.containsBeanDefinition(DataSourceConst.DATA_SOURCE_NAME)) {
                /*
                 * org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration会自动初始化顺序
                 * 会在此之前,会优先将Hikari注册为数据源,导致组件默认数据源无法加载。
                 *
                 * 目前没有好的方式将Hikari加载方式去除, 并且由于要加载apollo配置,注册dataSource的生命周期无法提前,
                 * 这里把Hikari的BeanDefinition删除。
                 */
                BeanDefinition bd = registry
                        .getBeanDefinition(DataSourceConst.DATA_SOURCE_NAME);
                if (bd.toString().contains("Hikari")) {
                    registry.removeBeanDefinition(DataSourceConst.DATA_SOURCE_NAME);

                    if (registry.containsBeanDefinition("scopedTarget.dataSource")) {
                        registry.removeBeanDefinition("scopedTarget.dataSource");
                    }
                }
            }

            if (dynamicDataSource) {

                // default datasource should b configured if use dynamic datasource.
                String defaultDataSource = environment
                        .getProperty("datasource.dynamic-default", String.class);
                Preconditions.checkState(!StringUtils.isEmpty(defaultDataSource),
                        "Property 'datasource.dynamic-default' should be configured!!!");

                String dataSourceFactoryBeanName =
                        primaryDataSource + DynamicDataSourceFactory.class.getSimpleName();
                BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition(DynamicDataSourceFactory.class);
                factoryBuilder.addConstructorArgValue(dependencyRoots);
                factoryBuilder.addConstructorArgValue(defaultDataSource);
                registry.registerBeanDefinition(dataSourceFactoryBeanName,
                        factoryBuilder.getBeanDefinition());

                BeanDefinition bd = new GenericBeanDefinition();
                bd.setFactoryBeanName(dataSourceFactoryBeanName);
                bd.setFactoryMethodName("getDataSource");
                registry.registerBeanDefinition(DataSourceConst.DATA_SOURCE_NAME, bd);

            } else {
                // 如果只有一个,则默认为DataSourceConst.DATA_SOURCE_NAME
                if (dependencyRoots.size() == 1) {

                    if (!registry.containsBeanDefinition(DataSourceConst.DATA_SOURCE_NAME)) {
                        for (Entry<String, Boolean> entry : dependencyRoots.entrySet()) {
                            String dataSourceFactoryBeanName =
                                    entry.getKey() + DefaultDataSourceFactory.class.getSimpleName();
                            BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder
                                    .genericBeanDefinition(DefaultDataSourceFactory.class);
                            factoryBuilder.addConstructorArgValue(entry.getKey());
                            registry.registerBeanDefinition(dataSourceFactoryBeanName,
                                    factoryBuilder.getBeanDefinition());

                            BeanDefinition bd = new GenericBeanDefinition();
                            bd.setFactoryBeanName(dataSourceFactoryBeanName);
                            bd.setFactoryMethodName("getDataSource");
                            registry.registerBeanDefinition(DataSourceConst.DATA_SOURCE_NAME, bd);
                        }
                    }
                } else if (!StringUtils.isEmpty(primaryDataSource)) {

                    String dataSourceFactoryBeanName =
                            primaryDataSource + DefaultDataSourceFactory.class.getSimpleName();
                    BeanDefinitionBuilder factoryBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(DefaultDataSourceFactory.class);
                    factoryBuilder.addConstructorArgValue(primaryDataSource);
                    registry.registerBeanDefinition(dataSourceFactoryBeanName,
                            factoryBuilder.getBeanDefinition());

                    BeanDefinition bd = new GenericBeanDefinition();
                    bd.setFactoryBeanName(dataSourceFactoryBeanName);
                    bd.setFactoryMethodName("getDataSource");
                    registry.registerBeanDefinition(DataSourceConst.DATA_SOURCE_NAME, bd);
                }
            }
        }

        private void rectifyDependencyRoots(Entry<String, DataSourceRuleProperties> rule,
                                            final Map<String, Boolean> dependencyRoots) {
            if (rule.getValue().getSingleRule() != null) {
                dependencyRoots.remove(rule.getValue().getSingleRule().getDataSourceRef());
            }

            if (rule.getValue().getMasterSlaveRule() != null) {
                dependencyRoots
                        .remove(rule.getValue().getMasterSlaveRule().getMasterDataSourceName());
                rule.getValue().getMasterSlaveRule().getSlaveDataSourceNames()
                        .forEach(slaveDataSource -> dependencyRoots.remove(slaveDataSource));
            }

            if (rule.getValue().getShardingRule() != null) {
                if (rule.getValue().getShardingRule().getMasterSlaveRules() != null) {
                    for (Entry<String, YamlMasterSlaveRuleConfiguration> entry : rule.getValue()
                            .getShardingRule().getMasterSlaveRules().entrySet()) {
                        dependencyRoots.remove(entry.getValue().getMasterDataSourceName());
                        entry.getValue().getSlaveDataSourceNames()
                                .forEach(slaveDataSource -> dependencyRoots.remove(slaveDataSource));
                    }
                }
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * DruidDataSource 的 Bean 处理器，将各数据源的自定义配置绑定到 Bean
     *
     * @author trang
     */
    static class DruidDataSourceBeanPostProcessor implements EnvironmentAware, BeanPostProcessor,
            Ordered {

        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName)
                throws BeansException {
            if (bean instanceof DruidDataSourceWrapper) {
                // 设置 Druid 名称
                val ds = (DruidDataSource) bean;
                ds.setName(beanName);

                Map<String, Object> dataSources = DataSourceUtils
                        .getDataSourcePoolBindings(environment);
                ;

                if (!dataSources.isEmpty() && dataSources.containsKey(
                        DataSourceUtils.parseDataSourceKey(beanName))) {
                    Binder.get(environment)
                            .bind(DataSourceConst.DATA_SOURCE_POOL_PREFIX + "." + DataSourceUtils
                                            .parseDataSourceKey(beanName),
                                    Bindable.ofInstance(ds));
                }

                log.info("ConfigDataSource: {}, url={}", beanName, ds.getUrl());
            }

            return bean;
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    @Value("${datasource.dynamic.interceptor.order:0x7ffffffe}")
    private int dynamicDataSourceAnnotationInterceptorOrder = Ordered.LOWEST_PRECEDENCE - 1;

    @Bean
    @ConditionalOnProperty(name = "datasource.aspect.routing.enable", havingValue = "true", matchIfMissing = true)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor(DynamicDataSourceCallback dynamicDataSourceCallback) {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(dynamicDataSourceCallback);
        interceptor.setOrder(dynamicDataSourceAnnotationInterceptorOrder);
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(
                interceptor);
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceCallback dynamicDataSourceCallback() {
        return new DynamicDataSourceCallback() {
            @Override
            public void setDefaultDataSource() {
                if (!DataSourceUtils.useCustomizedDataSource()) {
                    DynamicDataSourceHolder
                            .setDataSource(DataSourceUtils.getDynamicDefaultDataSource());
                }
            }
        };
    }


    @Bean
    @ConditionalOnProperty(name = "datasource.aspect.tx.enable", havingValue = "true", matchIfMissing = true)
    @Order(Ordered.LOWEST_PRECEDENCE)
    public DataSourceTransactionalAnnotationAdvisor dataSourceTransactionalAnnotationAdvisor() {
        DataSourceTransactionalAnnotationInterceptor interceptor = new DataSourceTransactionalAnnotationInterceptor();
        DataSourceTransactionalAnnotationAdvisor advisor = new DataSourceTransactionalAnnotationAdvisor(
                interceptor);
        return advisor;
    }

    /**
     * 配置默认transactionManager.
     *
     * @param datasource
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({PlatformTransactionManager.class})
    @ConditionalOnProperty(name = "datasource.tx-manager.enable", havingValue = "true", matchIfMissing = true)
    public DataSourceTransactionManager transactionManager(
            @Qualifier("dataSource") DataSource datasource,
            ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(
                datasource);
        if (transactionManagerCustomizers != null
                && transactionManagerCustomizers instanceof TransactionManagerCustomizers) {
            TransactionManagerCustomizers txCustomizers = (TransactionManagerCustomizers) transactionManagerCustomizers
                    .getIfAvailable();
            if (txCustomizers != null) {
                txCustomizers.customize(transactionManager);
            }
        }
        return transactionManager;
    }

    /**
     * 覆盖initializer,正常情况下不需要这个组件.
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "datasource.override.initializer.enable", havingValue = "true", matchIfMissing = true)
    public DataSourceInitializerPostProcessor dataSourceInitializerPostProcessor() {
        return new DataSourceInitializerPostProcessor();
    }

    class DataSourceInitializerPostProcessor implements BeanPostProcessor, Ordered {

        @Autowired
        private BeanFactory beanFactory;

        DataSourceInitializerPostProcessor() {
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName)
                throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName)
                throws BeansException {
            return bean;
        }
    }
}

