# spring-boot-starter-datasource
## 数据源访问Datasource
提供了数据源配置的能力。DataSource组件集成了Druid连接池，并内置了连接池参数配置的最佳实践。

### 组件能力
- 选用Alibaba Druid数据库连接池
- 支持多数据源配置，无需手工定义数据源Bean
- 启用slf4j日志输出，记录慢查询日志
- 启用stat，与Metrics组件配合支持数据源监控
- 启用wall，过滤不良SQL，防止潜在问题发生
- 默认配置源自用户服务的连接池调优参数，最佳实践开箱即用
- 支持读写分离
- 支持分库分表
- 支持数据脱敏
- 支持apollo配置

> 注意：1. 本组件是基于druid及sharding-jdbc，对于其他数据源混用比如Hikari，会有不可预知的问题，从统一组件的角度，不建议使用其他组件

### 引入组件
#### Maven

```
<dependency>
  <groupId>com.faner.infrastructure</groupId>
  <artifactId>spring-boot-starter-datasource</artifactId>
</dependency>
```

### 数据源配置
#### 定义数据源
在**application.yml**中增加数据源配置，例如：

```
datasource:
  pool:
    default:
      url: jdbc:mysql://localhost:3306/test?connectTimeout=1000&socketTimeout=60000&useUnicode=true&characterEncoding=utf-8&useSSL=false
      username: username
      password: password
```
这样就定义了一个名为defaultDataSource的数据源，并且可以使用此名称在Spring容器中获取数据源Bean。名字为default的数据源，将会作为默认的数据源Bean使用。

#### 注意：

> 1. 数据源配置里的beanName需要加上DataSource后缀，配置文件中直接引用名称即可，比如配置里用default,   引用bean的代码里用defaultDataSource,   但配置文件里，比如rule的配置部分还是使用default
> 
> 2. 数据组件在早期版本上做了升级，以支持一些复杂场景，对于老的版本默认会将default作为主数据源，在后续的版本中配置多数据源场景中，需要使用datasource.primary显式指定一下主数据源， eg. datasource.primary: default
> 
> 3.  rules部分是为了增加复杂能力，同时兼容老的配置，rules引用pool下的配置。rules部分和pool下的名字都是可以作为数据源名字，相互之间不能重复，使用时避免定义重复的名字

#### 在配置中心配置数据源参数
与Apollo配置中心结合，可在配置中心统一配置，在Apollo中的application的namespace，类型为properties，配置项例如：

```
datasource.pool.default.url=jdbc:mysql://localhost:3306/test?connectTimeout=1000&socketTimeout=60000&useUnicode=true&characterEncoding=utf-8&useSSL=false
datasource.pool.default.username=username
datasource.pool.default.password=password
```

注意：在配置中心定义的数据源配置，将会覆盖在application.yml中的配置参数，因此可以通过配置中心，来为不同环境的服务配置不同的数据源参数

注意：数据源必须先在application.yml中定义，才能通过配置中心动态配置，这是因为application.yml中的数据源定义项将会用来创建数据源的Spring Bean，不要移除application.yml中的数据源定义

### 数据源使用
#### 单数据源配置
单个数据源，默认不需要自己写注入数据源的代码，

注：主从配置不算多数据源。

#### 多数据源配置
当使用多个数据源时，可以指定使用动态数据配置及注解的方式，简化开发。

#### 配置文件
注意：使用动态数据源作为多数据源的时候，不用指定datasource.primary，否则实际sessionfactory不会绑定到动态数据源上

以前在使用多个数据源时需要自己写配置类，datasourc组件增加了动态数据源，默认并不会启用。

```
# 启用动态数据源：
datasource.use-dynamic=true  

# 指定默认数据源，会优先根据@DS注解定位数据源，
# 在未指定@DS注解的情况下，会默认走这个注解，方便历史迁移和兼容，简化开发
datasource.dynamic-default=ms1 
```

### 注解使用
代码中通过注解指定数据源和强制走主，注解可放到类或者方法上，方法优先。

com.faner.infrastructure.datasource.annotation.DS

```
@Transactional
@DS(dataSource = "ms1", forceMaster = true)
public void method2(){
   dataSourceService2.method2();
}
```

其中dataSource为数据源名称(同配置文件中保持一致)，forceMaster为是是否强制走主。

注意：上面ms1只是示例，根据需要换成自己的数据源名称
 
### [读写分离](https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/configuration/config-yaml/#%E8%AF%BB%E5%86%99%E5%88%86%E7%A6%BB)

```
datasource:
    pool:
      master:
        url: jdbc:mysql://localhost:3306/test?connectTimeout=1000&socketTimeout=60000&useUnicode=true&characterEncoding=utf-8&useSSL=false
        username: test
        password: password
      slave:
        url: jdbc:mysql://localhost:3306/test?connectTimeout=1000&socketTimeout=60000&useUnicode=true&characterEncoding=utf-8&useSSL=false
        username: test
        password: password
    rules:
        ms1:
            master-slave-rule:
                master-data-source-name: master
                name: ms_ds
                slave-data-source-names: slave # 可以指定多个slave0,slave1
            props:
                sql:
                    show: true
```
或

```
datasource.rules.ms1.master-slave-rule.name=ms_ds
datasource.rules.ms1.master-slave-rule.master-data-source-name= master
datasource.rules.ms1.master-slave-rule.slave-data-source-names= slave0,slave1
datasource.rules.ms1.props.sql.show=true

```

### [分库分表](https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/configuration/config-yaml/#%E6%95%B0%E6%8D%AE%E5%88%86%E7%89%87)

```
datasource:
    rules:
        sh1:
            props:
                query:
                    with:
                        cipher:
                            column: true
                sql:
                    show: true
            sharding-rule:
                default-data-source-name: master0
                default-database-strategy:
                    inline:
                        algorithm-expression: ds_${Math.floorMod(order_id.longValue(), 2L)}
                        sharding-column: order_id
                default-key-generator:
                    column: order_id
                    type: SNOWFLAKE
                master-slave-rules:
                    ds_0:
                        masterDataSourceName: master0
                        slaveDataSourceNames: slave0
                    ds_1:
                        masterDataSourceName: master1
                        slaveDataSourceNames: slave1
                tables:
                    t_order:
                        actual-data-nodes: ds_${0..1}.t_order_${0..1}
                        key-generator:
                            column: order_id
                            type: SNOWFLAKE
                        table-strategy:
                            inline:
                                algorithm-expression: t_order_${Math.floorMod(Math.floorDiv(order_id.longValue(), 2L), 2L)}
                                sharding-column: order_id
                    t_order_item:
                        actual-data-nodes: ds_${0..1}.t_order_item_${0..1}
                        database-strategy:
                            inline:
                                algorithm-expression: ds_${Math.floorMod(order_item_id.longValue(), 2L)}
                                sharding-column: order_item_id
                        key-generator:
                            column: order_item_id
                            type: SNOWFLAKE
                        table-strategy:
                            inline:
                                algorithm-expression: t_order_item_${Math.floorMod(Math.floorDiv(order_item_id.longValue(), 2L), 2L)}
                                sharding-column: order_item_id
```
或

```
datasource.rules.sh1.sharding-rule.default-key-generator.type=SNOWFLAKE
datasource.rules.sh1.sharding-rule.default-key-generator.column=order_id
datasource.rules.sh1.sharding-rule.default-data-source-name=master0
datasource.rules.sh1.sharding-rule.default-database-strategy.inline.sharding-column=order_id
datasource.rules.sh1.sharding-rule.default-database-strategy.inline.algorithm-expression=ds_${Math.floorMod(order_id.longValue(), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order.actual-data-nodes=ds_${0..1}.t_order_${0..1}
datasource.rules.sh1.sharding-rule.tables.t_order.table-strategy.inline.sharding-column=order_id
datasource.rules.sh1.sharding-rule.tables.t_order.table-strategy.inline.algorithm-expression=t_order_${Math.floorMod(Math.floorDiv(order_id.longValue(), 2L), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order.key-generator.type=SNOWFLAKE
datasource.rules.sh1.sharding-rule.tables.t_order.key-generator.column=order_id
datasource.rules.sh1.sharding-rule.tables.t_order_item.actual-data-nodes=ds_${0..1}.t_order_item_${0..1}
datasource.rules.sh1.sharding-rule.tables.t_order_item.database-strategy.inline.sharding-column=order_item_id
datasource.rules.sh1.sharding-rule.tables.t_order_item.database-strategy.inline.algorithm-expression=ds_${Math.floorMod(order_item_id.longValue(), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order_item.table-strategy.inline.sharding-column=order_item_id
datasource.rules.sh1.sharding-rule.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_${Math.floorMod(Math.floorDiv(order_item_id.longValue(), 2L), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order_item.key-generator.type=SNOWFLAKE
datasource.rules.sh1.sharding-rule.tables.t_order_item.key-generator.column=order_item_id
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_0.masterDataSourceName=master0
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_0.slaveDataSourceNames=slave0
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_1.masterDataSourceName=master1
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_1.slaveDataSourceNames=slave1
datasource.rules.sh1.props.query.with.cipher.column=true
datasource.rules.sh1.props.sql.show=true

```
#### 注意事项：

1、由于sharding-jdbc版本的问题，分表条件需要将字段写在前面，否则会导致SQL解析不到谓词导致分表逻辑失效

![图片](/api/project/5327834/files/26389664/imagePreview)

### 数据脱敏
#### 配置结构

![图片](/api/project/5327834/files/26389667/imagePreview)
***注：

0）对于历史迁移请阅读数据脱敏方案

![图片](/api/project/5327834/files/26390054/imagePreview)

1）如果是新项目只需要配置cipher-column(无需配置plain-column，assisted-query-column根据是否有等值查询按需配置 ), query.with.cipher.column=true

历史数据迁移后，在apollo上可以不用重启服务动态切换这个属性，具体操作方法：

a) 更新query.with.cipher.column的值

b) 启动刷新query.with.cipher.column-refresh=true
![图片](/api/project/5327834/files/26390055/imagePreview)
c) 关闭刷新query.with.cipher.column-refresh=false (这时更新后的属性值会在sharding-jdbc的缓存中，不必每次读取解析)

d) 历史数据迁移完成，需要将plain-column去掉，这是如果plain-column数据库配置非空，

则可以1）先将plain-column可空(注意选择低峰操作，可能锁表)，2）去掉plain-column配置，3）将库里plain-column字段清空或者drop掉；

```
 这里需要说明一下：对一部分字段脱敏，并将明文字段删除后，又发现有新的字段需要脱密，query.with.cipher.column配置是在数据源层面生效的，第一批脱敏字段明文字段已经不存在了，query.with.cipher.column=true已经不能改回去了，为了避免这种情况，建议先观察一段时间并确定是否所有字段都脱敏，之后再删除明文字段。否则，需要找低峰期进行操作，尽量避免对用户的影响。
```
- 老项目升级历史数据迁移完后，需要配置同1）并重启服务。
- 类型转换：默认是字符串，非字符串可以在encryptor的props配置kms.cipher.data-type，值可以为boolean,byte,short,integer,long,bigint,float,double,bigdecimal
4）cipher.resource-id与cipher.field是等效的，选一种配置即可
5）对于有辅助查询字段的，需要将加密类型设置为kms；不包含辅助查询字段的，需要将类型设置为kms0
示例：
带有辅助字段配置

```
datasource.rules.ms1.encrypt-rule.encryptors.encryptor_kms.type=kms
```
无辅助字段配置

```
datasource.rules.ms1.encrypt-rule.encryptors.encryptor_kms.type=kms0
```
1. 可以通过kms.cipher.data-type属性为字段加密器指定数据类型，避免类型转换问题
目前支持的类型有string,boolean,byte,short,integer,long,biginteger,float,double,bigdecimal,clob，默认为string
示例如下：
![图片](/api/project/5327834/files/26390057/imagePreview)
7) 新的版本里resourceId需要到安全组进行申请，参考文档

#### 单库

```
datasource:
    
    rules:
        ds0:
            encrypt-rule:
                encryptors:
                    encryptor_kms:
                        props:
                            kms:
                                cipher:
                                    type: aes
                        type: kms
                    encryptor_kms_user_id:
                        props:
                            kms:
                                cipher:
                                    resource-id: PRI|ffffffff-ffff-ffff-ffff-ffffffffffff
                                    type: aes
                        type: kms
          
                tables:
                    t_order:
                        columns:
                            user_id:
                                assisted-query-column: user_assist
                                cipher-column: user_cipher
                                encryptor: encryptor_kms_user_id
                                plain-column: user_id
            props:
                query:
                    with:
                        cipher:
                            column: false
                sql:
                    show: true
            single-rule:
                data-source-ref: default

```
或

```
datasource.rules.ds0.single-rule.data-source-ref=default
datasource.rules.ds0.encrypt-rule.tables.t_order.columns.user_id.plain-column=user_id
datasource.rules.ds0.encrypt-rule.tables.t_order.columns.user_id.cipher-column=user_cipher
datasource.rules.ds0.encrypt-rule.tables.t_order.columns.user_id.assisted-query-column=user_assist
datasource.rules.ds0.encrypt-rule.tables.t_order.columns.user_id.encryptor=encryptor_kms_user_id
datasource.rules.ds0.encrypt-rule.encryptors.encryptor_kms.type=kms
datasource.rules.ds0.encrypt-rule.encryptors.encryptor_kms.props.kms.cipher.type=aes
datasource.rules.ds0.encrypt-rule.encryptors.encryptor_kms_user_id.type=kms
datasource.rules.ds0.encrypt-rule.encryptors.encryptor_kms_user_id.props.kms.cipher.type=aes
datasource.rules.ds0.encrypt-rule.encryptors.encryptor_kms_user_id.props.kms.cipher.resource-id=PRI|ffffffff-ffff-ffff-ffff-ffffffffffff
datasource.rules.ds0.props.query.with.cipher.column=false
datasource.rules.ds0.props.sql.show=true

```
#### 读写分离(主从)

```
datasource:
    primary: ms1
    rules:
        ms1:
            encrypt-rule:
                encryptors:
                    encryptor_kms:
                        props:
                            kms:
                                cipher:
                                    resource-id: PRI|ffffffff-ffff-ffff-ffff-ffffffffffff
                                    type: aes
                        type: kms
                tables:
                    t_order:
                        columns:
                            user_id:
                                assisted-query-column: user_assist
                                cipher-column: user_cipher
                                encryptor: encryptor_kms
                                plain-column: user_id
            master-slave-rule:
                load-balance-algorithm-type: round_robin
                master-data-source-name: master
                name: ms_ds
                props:
                    sql:
                        show: true
                slave-data-source-names: slave0,slave1
            props:
                query:
                    with:
                        cipher:
                            column: true
                sql:
                    show: true

```
或者

```
datasource.rules.ms1.master-slave-rule.name=ms_ds
datasource.rules.ms1.master-slave-rule.load-balance-algorithm-type= round_robin
datasource.rules.ms1.master-slave-rule.master-data-source-name= master
datasource.rules.ms1.master-slave-rule.slave-data-source-names= slave0,slave1
datasource.rules.ms1.master-slave-rule.props.sql.show=true


datasource.rules.ms1.encrypt-rule.tables.t_order.columns.user_id.plain-column=user_id
datasource.rules.ms1.encrypt-rule.tables.t_order.columns.user_id.cipher-column=user_cipher
datasource.rules.ms1.encrypt-rule.tables.t_order.columns.user_id.assisted-query-column=user_assist
datasource.rules.ms1.encrypt-rule.tables.t_order.columns.user_id.encryptor=encryptor_kms
datasource.rules.ms1.encrypt-rule.encryptors.encryptor_kms.type=kms
datasource.rules.ms1.encrypt-rule.encryptors.encryptor_kms.props.kms.cipher.type=aes
datasource.rules.ms1.encrypt-rule.encryptors.encryptor_kms.props.kms.cipher.resource-id=PRI|ffffffff-ffff-ffff-ffff-ffffffffffff
datasource.rules.ms1.props.query.with.cipher.column=true
datasource.rules.ms1.props.sql.show=true

```
#### 分库分表

```
datasource:
    rules:
        sh1:
            props:
                query:
                    with:
                        cipher:
                            column: true
                sql:
                    show: true
            sharding-rule:
                default-data-source-name: master0
                default-database-strategy:
                    inline:
                        algorithm-expression: ds_${Math.floorMod(order_id.longValue(), 2L)}
                        sharding-column: order_id
                default-key-generator:
                    column: order_id
                    type: SNOWFLAKE
                encrypt-rule:
                    encryptors:
                        encryptor_kms:
                            props:
                                kms:
                                    cipher:
                                        resource-id: PRI|ffffffff-ffff-ffff-ffff-ffffffffffff
                                        type: aes
                            type: kms
                    tables:
                        t_order:
                            columns:
                                user_id:
                                    assisted-query-column: user_assist
                                    cipher-column: user_cipher
                                    encryptor: encryptor_kms
                                    plain-column: user_id
                master-slave-rules:
                    ds_0:
                        masterDataSourceName: master0
                        slaveDataSourceNames: slave0
                    ds_1:
                        masterDataSourceName: master1
                        slaveDataSourceNames: slave1
                tables:
                    t_order:
                        actual-data-nodes: ds_${0..1}.t_order_${0..1}
                        key-generator:
                            column: order_id
                            type: SNOWFLAKE
                        table-strategy:
                            inline:
                                algorithm-expression: t_order_${Math.floorMod(Math.floorDiv(order_id.longValue(), 2L), 2L)}
                                sharding-column: order_id
                    t_order_item:
                        actual-data-nodes: ds_${0..1}.t_order_item_${0..1}
                        database-strategy:
                            inline:
                                algorithm-expression: ds_${Math.floorMod(order_item_id.longValue(), 2L)}
                                sharding-column: order_item_id
                        key-generator:
                            column: order_item_id
                            type: SNOWFLAKE
                        table-strategy:
                            inline:
                                algorithm-expression: t_order_item_${Math.floorMod(Math.floorDiv(order_item_id.longValue(), 2L), 2L)}
                                sharding-column: order_item_id


```
或者

```
datasource.rules.sh1.sharding-rule.default-key-generator.type=SNOWFLAKE
datasource.rules.sh1.sharding-rule.default-key-generator.column=order_id
datasource.rules.sh1.sharding-rule.default-data-source-name=master0
datasource.rules.sh1.sharding-rule.default-database-strategy.inline.sharding-column=order_id
datasource.rules.sh1.sharding-rule.default-database-strategy.inline.algorithm-expression=ds_${Math.floorMod(order_id.longValue(), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order.actual-data-nodes=ds_${0..1}.t_order_${0..1}
datasource.rules.sh1.sharding-rule.tables.t_order.table-strategy.inline.sharding-column=order_id
datasource.rules.sh1.sharding-rule.tables.t_order.table-strategy.inline.algorithm-expression=t_order_${Math.floorMod(Math.floorDiv(order_id.longValue(), 2L), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order.key-generator.type=SNOWFLAKE
datasource.rules.sh1.sharding-rule.tables.t_order.key-generator.column=order_id
datasource.rules.sh1.sharding-rule.tables.t_order_item.actual-data-nodes=ds_${0..1}.t_order_item_${0..1}
datasource.rules.sh1.sharding-rule.tables.t_order_item.database-strategy.inline.sharding-column=order_item_id
datasource.rules.sh1.sharding-rule.tables.t_order_item.database-strategy.inline.algorithm-expression=ds_${Math.floorMod(order_item_id.longValue(), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order_item.table-strategy.inline.sharding-column=order_item_id
datasource.rules.sh1.sharding-rule.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item_${Math.floorMod(Math.floorDiv(order_item_id.longValue(), 2L), 2L)}
datasource.rules.sh1.sharding-rule.tables.t_order_item.key-generator.type=SNOWFLAKE
datasource.rules.sh1.sharding-rule.tables.t_order_item.key-generator.column=order_item_id
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_0.masterDataSourceName=master0
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_0.slaveDataSourceNames=slave0
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_1.masterDataSourceName=master1
datasource.rules.sh1.sharding-rule.master-slave-rules.ds_1.slaveDataSourceNames=slave1


datasource.rules.sh1.sharding-rule.encrypt-rule.tables.t_order.columns.user_id.plain-column=user_id
datasource.rules.sh1.sharding-rule.encrypt-rule.tables.t_order.columns.user_id.cipher-column=user_cipher
datasource.rules.sh1.sharding-rule.encrypt-rule.tables.t_order.columns.user_id.assisted-query-column=user_assist
datasource.rules.sh1.sharding-rule.encrypt-rule.tables.t_order.columns.user_id.encryptor=encryptor_kms
datasource.rules.sh1.sharding-rule.encrypt-rule.encryptors.encryptor_kms.type=kms
datasource.rules.sh1.sharding-rule.encrypt-rule.encryptors.encryptor_kms.props.kms.cipher.type=aes
datasource.rules.sh1.sharding-rule.encrypt-rule.encryptors.encryptor_kms.props.kms.cipher.resource-id=PRI|ffffffff-ffff-ffff-ffff-ffffffffffff
datasource.rules.sh1.props.query.with.cipher.column=true
datasource.rules.sh1.props.sql.show=true
```

### 显式引用数据源

一般情况下不需要自己引用数据源，如果默认情况不能满足需求，比如要配置多个数据源，可以自己实现注入数据代码，比如一些老项目升级的场景，有些胶水代码中依赖数据源Bean,可以单独配置。

在需要使用数据源的Spring Bean中，可随时通过依赖注入使用数据源：


```
public class BusinessService {
    @Autowired
    private DataSource dataSource;
    
    //...
}
```

注入指定名称的数据源，例如otherDataSource：

```
public class BusinessService {
    @Autowired
    @Qualifier("otherDataSource")
    private DataSource dataSource;
    
    //...
}
```
### 配置参数

#### 全局配置参数
提示：默认情况下数据源连接池已被调整为满足大多数服务最佳状态的参数，除非你清楚地了解每一项参数的作用和工作原理，否则不建议自定义是设置。

配置项	类型	说明	默认值

datasource.pool.<poolName>.url	字符串	JDBC数据库连接字符串	null

datasource.pool.<poolName>.username	字符串	数据库连接用户名	null

datasource.pool.<poolName>.password	字符串	数据库连接密码	null

datasource.pool.<poolName>.driver-class-name	字符串	JDBC驱动程序类名	null

datasource.pool.<poolName>.initial-size	整型	初始池大小	15

datasource.pool.<poolName>.min-idle	整型	最小空闲连接数	15

datasource.pool.<poolName>.max-active	整型	最大活跃连接数	30

datasource.pool.<poolName>.max-wait	整型	获取连接时最大等待时间	300

datasource.pool.<poolName>.test-while-idle	布尔型	是否启动空闲连接检查	true

datasource.pool.<poolName>.test-on-borrow	布尔型	是否启用获取连接时检查	true

datasource.pool.<poolName>.test-on-return	布尔型	是否启用归还连接时检查	true

datasource.pool.<poolName>.test-keep-alive	布尔型	是否启用Keepalive	true

datasource.pool.<poolName>.min-evictable-idle-time-millis	整型	最小淘汰空闲连接时间	300_000

datasource.pool.<poolName>.time-between-eviction-runs-millis	整型	淘汰连接任务运行时间间隔	60_000

默认值配置值可以查看com.faner.infrastructure.datasource.druid.DruidDataSourceWrapper

#### Wall相关配置参数
可参照datasource组件jar包(在IDE中查看maven依赖可看到)下resources/config/druid-filter.properties 及 META-INF/spring-configuration-metadata.json

#### 相关超时配置
![图片](/api/project/5327834/files/26390058/imagePreview)

- 事务超时

`@Transactional(timeout=1)`

- Statement语句超时

注意这里的单位是秒

**数据源上配置**

```
datasource:
  pool:
    default:
      query-timeout: 1 
```
**ORM上配置**
a) 全局默认值：

```
# default-statement-timeout for mybatis
mybatis.configuration.default-statement-timeout = 1
# default-statement-timeout for mybatis-plus
mybatis-plus.configuration.default-statement-timeout = 1
```
b) 语句级别

```
<select id="getXXXX" parameterType="java.lang.String" resultMap="dataMap" timeout="1">
 
</select>
```
- JDBC驱动超时

jdbc url中可以配置connectionTimeout及socketTimeout

`jdbc:mysql://localhost:3306/test?connectTimeout=1000&socketTimeout=60000&useUnicode=true&characterEncoding=utf-8&useSSL=false`

- 操作系统超时


```
cat /proc/sys/net/ipv4/tcp_keepalive_time(tcp_keepalive_intvl..）

# sysctl \
> net.ipv4.tcp_keepalive_time \
> net.ipv4.tcp_keepalive_intvl \
> net.ipv4.tcp_keepalive_probes
```
### 常见问题FAQ

#### 启动时加载元数据优化
sharding-jdbc启动时会加载元数据，默认加载方式是串行读取每个表，导致初始化时间比较长，通过在props下增加max.connections.size.per.query配置减少元数据加载时间，从而减少服务整体启动时间。配置方式如下：
![图片](/api/project/5327834/files/26390061/imagePreview)
(其中max.connections.size.per.query的具体值可以根据表的数量自己定义)
