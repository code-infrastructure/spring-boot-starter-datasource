package com.faner.infrastructure.datasource.consts;

/**
 * 数据源常量
 *
 * @作者 Faner
 * @创建时间 2021/12/31 20:43
 */
public interface DataSourceConst {

    String DATA_SOURCE_PREFIX = "datasource";

    String DATA_SOURCE_RULES_PREFIX = DATA_SOURCE_PREFIX + ".rules";

    String DATA_SOURCE_RULES_ENCRYPT_PREFIX = DATA_SOURCE_RULES_PREFIX + ".encrypt";

    String DATA_SOURCE_RULES_MASTER_SLAVE_PREFIX = DATA_SOURCE_RULES_PREFIX + ".masterslave";

    String DATA_SOURCE_RULES_SHARDING_PREFIX = DATA_SOURCE_RULES_PREFIX + ".sharding";

    String DATA_SOURCE_POOL_PREFIX = DATA_SOURCE_PREFIX + ".pool";

    String DATA_SOURCE_NAME = "dataSource";

    String DATA_SOURCE_NAME_SUFFIX = "DataSource";

    String DEFAULT_DATA_SOURCE_NAME = "default";

    String BEAN_NAME_ENCRYPT_DATA_SOURCE = "encrypt" + DATA_SOURCE_NAME_SUFFIX;

    String BEAN_NAME_MASTER_SLAVE_DATA_SOURCE = "masterSlave" + DATA_SOURCE_NAME_SUFFIX;

    String BEAN_NAME_SHARDING_DATA_SOURCE = "sharding" + DATA_SOURCE_NAME_SUFFIX;

    String BEAN_NAME_SINGLE_DATA_SOURCE = "single" + DATA_SOURCE_NAME_SUFFIX;

    String BEAN_NAME_DEFAULT_DATA_SOURCE = DEFAULT_DATA_SOURCE_NAME + DATA_SOURCE_NAME_SUFFIX;

    String BEAN_NAME_DEFAULT_DATA_SOURCE_FACTORY = "__defaultDataSourceFactory";
}
