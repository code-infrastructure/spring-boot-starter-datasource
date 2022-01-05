package com.faner.infrastructure.datasource.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 打印一次错误消息并且停止更多的Metric新增
 * @作者 Faner
 * @创建时间 2021/12/31 21:21
 */
public class OnlyOnceLoggingDenyMeterFilter implements MeterFilter {
    private static final Log logger = LogFactory.getLog(OnlyOnceLoggingDenyMeterFilter.class);
    private final AtomicBoolean alreadyWarned = new AtomicBoolean(false);
    private final Supplier<String> message;

    public OnlyOnceLoggingDenyMeterFilter(Supplier<String> message) {
        Assert.notNull(message, "Message must not be null");
        this.message = message;
    }

    @Override
    public MeterFilterReply accept(Meter.Id id) {
        if (logger.isWarnEnabled() && this.alreadyWarned.compareAndSet(false, true)) {
            logger.warn(this.message.get());
        }

        return MeterFilterReply.DENY;
    }
}

