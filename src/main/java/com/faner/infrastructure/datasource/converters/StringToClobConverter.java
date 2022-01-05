package com.faner.infrastructure.datasource.converters;

import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import javax.sql.rowset.serial.SerialClob;
import java.sql.Clob;

/**
 * Clob转换器.
 *
 * @作者 Faner
 * @创建时间 2021/12/31 20:46
 */
public class StringToClobConverter implements Converter<String, Clob> {

    @SneakyThrows
    @Override
    public Clob convert(String s) {

        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return new SerialClob(s.toCharArray());
    }
}