package com.faner.infrastructure.datasource.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源配置
 *
 * @作者 Faner
 * @创建时间 2021/12/31 16:09
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DS {

    /**
     * 绑定数据源
     */
    String dataSource() default "";

    /**
     * 是否强制走主
     */
    boolean forceMaster() default false;

}