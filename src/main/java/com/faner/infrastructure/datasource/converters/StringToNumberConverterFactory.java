package com.faner.infrastructure.datasource.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.NumberUtils;

/**
 * 类型转换类工厂
 * @作者 Faner
 * @创建时间 2021/12/31 20:48
 */
public final class StringToNumberConverterFactory {
    private StringToNumberConverterFactory() {
    }

    public static <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToNumber(targetType);
    }

    private static final class StringToNumber<T extends Number> implements Converter<String, T> {
        private final Class<T> targetType;

        public StringToNumber(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(String source) {
            return source.isEmpty() ? null : NumberUtils.parseNumber(source, this.targetType);
        }
    }
}