package com.faner.infrastructure.datasource.metrics;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.JdbcSqlStat;
import com.alibaba.druid.stat.JdbcStatementStat;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * 将Druid的监控信息导出到监控指标中
 * @作者 Faner
 * @创建时间 2021/12/31 21:04
 */
public class DruidDataSourceMetrics implements MeterBinder {

    private final DruidDataSource dataSource;
    private final Iterable<Tag> tags;
    private final Integer maxSqlTags = 200;
    private final Set<String> registeredSqlTags = new ConcurrentSkipListSet<>();
    private final DruidProperties druidProperties;

    public DruidDataSourceMetrics(DruidDataSource dataSource, String name, DruidProperties druidProperties) {
        this(dataSource, name, emptyList(), druidProperties);
    }

    public DruidDataSourceMetrics(DruidDataSource dataSource, String name, Iterable<Tag> tags,
                                  DruidProperties druidProperties) {
        this.dataSource = dataSource;
        this.tags = Tags.concat(tags, "name", name);
        this.druidProperties = druidProperties;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        registry.gauge("druid.wait.thread.count", tags, dataSource, DruidDataSource::getWaitThreadCount);
        registry.gauge("druid.not.empty.wait.count", tags, dataSource, DruidDataSource::getNotEmptyWaitCount);
        registry.gauge("druid.not.empty.wait.millis", tags, dataSource, DruidDataSource::getNotEmptyWaitMillis);
        registry.gauge("druid.pooling.count", tags, dataSource, DruidDataSource::getPoolingCount);
        registry.gauge("druid.pooling.peak", tags, dataSource, DruidDataSource::getPoolingPeak);
        registry.gauge("druid.active.count", tags, dataSource, DruidDataSource::getActiveCount);
        registry.gauge("druid.active.peak", tags, dataSource, DruidDataSource::getActivePeak);
        registry.gauge("druid.initial.size", tags, dataSource, DruidDataSource::getInitialSize);
        registry.gauge("druid.min.idle", tags, dataSource, DruidDataSource::getMinIdle);
        registry.gauge("druid.max.active", tags, dataSource, DruidDataSource::getMaxActive);
        registry.gauge("druid.query.timeout", tags, dataSource, DruidDataSource::getQueryTimeout);
        registry.gauge("druid.transaction.query.timeout", tags, dataSource,
                DruidDataSource::getTransactionQueryTimeout);
        registry.gauge("druid.login.timeout", tags, dataSource, DruidDataSource::getLoginTimeout);
        registry.gauge("druid.logic.connect.count", tags, dataSource, DruidDataSource::getConnectCount);
        registry.gauge("druid.logic.close.count", tags, dataSource, DruidDataSource::getCloseCount);
        registry.gauge("druid.logic.connect.error.count", tags, dataSource, DruidDataSource::getConnectErrorCount);
        registry.gauge("druid.physical.connect.count", tags, dataSource, DruidDataSource::getCreateCount);
        registry.gauge("druid.physical.close.count", tags, dataSource, DruidDataSource::getDestroyCount);
        registry.gauge("druid.physical.connect.error.count", tags, dataSource, DruidDataSource::getCreateErrorCount);
        registry.gauge("druid.execute.count", tags, dataSource, DruidDataSource::getExecuteCount);
        registry.gauge("druid.execute.update.count", tags, dataSource, DruidDataSource::getExecuteUpdateCount);
        registry.gauge("druid.execute.query.count", tags, dataSource, DruidDataSource::getExecuteQueryCount);
        registry.gauge("druid.execute.batch.count", tags, dataSource, DruidDataSource::getExecuteBatchCount);
        registry.gauge("druid.error.count", tags, dataSource, DruidDataSource::getErrorCount);
        registry.gauge("druid.commit.count", tags, dataSource, DruidDataSource::getCommitCount);
        registry.gauge("druid.rollback.count", tags, dataSource, DruidDataSource::getRollbackCount);
        registry.gauge("druid.ps.open.count", tags, dataSource, DruidDataSource::getPreparedStatementCount);
        registry.gauge("druid.ps.close.count", tags, dataSource, DruidDataSource::getClosedPreparedStatementCount);
        registry.gauge("druid.ps.cache.access.count", tags, dataSource,
                DruidDataSource::getCachedPreparedStatementAccessCount);
        registry.gauge("druid.ps.cache.hit.count", tags, dataSource,
                DruidDataSource::getCachedPreparedStatementHitCount);
        registry.gauge("druid.ps.cache.miss.count", tags, dataSource,
                DruidDataSource::getCachedPreparedStatementMissCount);
        registry.gauge("druid.start.transaction.count", tags, dataSource, DruidDataSource::getStartTransactionCount);
        registry.gauge("druid.clob.open.count", tags, dataSource, ds -> ds.getDataSourceStat().getClobOpenCount());
        registry.gauge("druid.blob.open.count", tags, dataSource, ds -> ds.getDataSourceStat().getBlobOpenCount());
        registry.gauge("druid.keep.alive.check.count", tags, dataSource,
                ds -> ds.getDataSourceStat().getKeepAliveCheckCount());
        registry.gauge("druid.max.wait", tags, dataSource, DruidDataSource::getMaxWait);
        registry.gauge("druid.max.wait.thread.count", tags, dataSource, DruidDataSource::getMaxWaitThreadCount);
        registry.gauge("druid.max.pool.ps.per.connection.size", tags, dataSource,
                DruidDataSource::getMaxPoolPreparedStatementPerConnectionSize);
        registry.gauge("druid.min.evictable.idle.time.millis", tags, dataSource,
                DruidDataSource::getMinEvictableIdleTimeMillis);
        registry.gauge("druid.max.evictable.idle.time.millis", tags, dataSource,
                DruidDataSource::getMaxEvictableIdleTimeMillis);
        registry.gauge("druid.recycle.error.count", tags, dataSource, DruidDataSource::getRecycleErrorCount);
        // Transaction Histogram
        for (int i = 0; i < dataSource.getTransactionHistogram().getRanges().length; i++) {
            val histogram = dataSource.getTransactionHistogram().getRanges()[i];
            val indexPos = i;
            registry.gauge("druid.transaction.histogram", Tags.concat(tags, "le", String.valueOf(histogram / 1000.0)),
                    dataSource, ds -> ds.getTransactionHistogram().get(indexPos));
        }
        // Connection Hold Histogram
        for (int i = 0; i < dataSource.getDataSourceStat().getConnectionHoldHistogram().getRanges().length; i++) {
            val histogram = dataSource.getDataSourceStat().getConnectionHoldHistogram().getRanges()[i];
            val indexPos = i;
            registry.gauge("druid.connection.hold.time.histogram", Tags.concat(tags, "le",
                    String.valueOf(histogram / 1000.0)), dataSource,
                    ds -> ds.getDataSourceStat().getConnectionHoldHistogram().get(indexPos));
        }

        if (druidProperties == null || !druidProperties.getSql().isEnable()) {
            JdbcStatementStat statementStat = dataSource.getDataSourceStat().getStatementStat();
            Gauge.builder("druid.sql.execute.count", statementStat, JdbcStatementStat::getExecuteCount).tags(tags).register(registry);
            Gauge.builder("druid.sql.execute.error.count", statementStat, JdbcStatementStat::getErrorCount).tags(tags).register(registry);
            Gauge.builder("druid.sql.execute.millis.sum", statementStat, JdbcStatementStat::getExecuteMillisTotal).tags(tags).register(registry);
        } else {
            // SQL stats
            val sqlExecuteCountGauge = MultiGauge.builder("druid.sql.execute.count").tags(tags).register(registry);
            val sqlExecuteErrorCountGauge =
                    MultiGauge.builder("druid.sql.execute.error.count").tags(tags).register(registry);
            val sqlExecuteTimeGauge = MultiGauge.builder("druid.sql.execute.millis.sum").tags(tags).register(registry);
            val sqlExecuteTimeMaxGauge =
                    MultiGauge.builder("druid.sql.execute.millis.max").tags(tags).register(registry);
            // 由于SQL查询发生后，统计Map中才有相应的指标项，所以每隔1s遍历统计Map以追加各SQL采集指标项
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    val sqlStatMap = dataSource.getDataSourceStat().getSqlStatMap();
                    if (sqlStatMap != null && !sqlStatMap.isEmpty()) {
                        // 将所有的SQL加入到已经注册采集的SQL列表里面，保证已注册的SQL数量不超过maxSqlTags定义的SQL条数上限
                        for (Map.Entry<String, JdbcSqlStat> entry : sqlStatMap.entrySet()) {
                            if (registeredSqlTags.size() >= maxSqlTags) {
                                break;
                            }
                            registeredSqlTags.add(entry.getKey());
                        }

                        // 使用已注册的SQL列表来获取待采集的指标
                        val filteredSqlStatMap = new HashMap<String, JdbcSqlStat>(registeredSqlTags.size());
                        for (val sql : registeredSqlTags) {
                            val stat = sqlStatMap.get(sql);
                            if (stat != null) {
                                // 为了避免超大SQL采集时出现问题，需要对SQL进行格式化
                                filteredSqlStatMap.put(formatSQLTag(sql), stat);
                            }
                        }

                        // 注册采集指定的指标
                        sqlExecuteCountGauge.register(filteredSqlStatMap.entrySet().stream()
                                .map(entry -> MultiGauge.Row.of(Tags.of("sql", entry.getKey()), entry.getValue(),
                                        JdbcSqlStat::getExecuteCount))
                                .collect(Collectors.toList()));
                        sqlExecuteErrorCountGauge.register(filteredSqlStatMap.entrySet().stream()
                                .map(entry -> MultiGauge.Row.of(Tags.of("sql", entry.getKey()), entry.getValue(),
                                        JdbcSqlStat::getErrorCount))
                                .collect(Collectors.toList()));
                        sqlExecuteTimeGauge.register(filteredSqlStatMap.entrySet().stream()
                                .map(entry -> MultiGauge.Row.of(Tags.of("sql", entry.getKey()), entry.getValue(),
                                        JdbcSqlStat::getExecuteMillisTotal))
                                .collect(Collectors.toList()));
                        sqlExecuteTimeMaxGauge.register(filteredSqlStatMap.entrySet().stream()
                                .map(entry -> MultiGauge.Row.of(Tags.of("sql", entry.getKey()), entry.getValue(),
                                        JdbcSqlStat::getExecuteMillisMax))
                                .collect(Collectors.toList()));
                    }

                }
            }, 1000, 10000);
        }
    }

    /**
     * 格式化SQLTag
     *
     * @param sql 传入SQL
     * @return 格式化的SQL Tag
     */
    public static String formatSQLTag(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return "Unknown";
        }

        boolean isTruncated = false;
        int sqlHashCode = 0;
        if (sql.length() > 2048) {
            isTruncated = true;
            sqlHashCode = sql.hashCode();
            sql = sql.substring(0, 2047);
        }

        val sqlBuilder = new StringBuilder();
        for (val stmt : sql.split("\n")) {
            if (!StringUtils.isEmpty(stmt)) {
                sqlBuilder.append(stmt.trim()).append(" ");
            }
        }

        if (isTruncated) {
            sqlBuilder.append(String.format("...[truncated,hash=%d]", sqlHashCode));
        }

        return sqlBuilder.toString().trim();
    }
}
