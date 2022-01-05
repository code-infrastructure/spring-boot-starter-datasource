package com.faner.infrastructure.datasource.converters;

import org.springframework.core.convert.converter.Converter;

/**
 * 不转换Convertor
 *
 * @作者 Faner
 * @创建时间 2021/12/31 20:47
 */
public class StringToNoneConvertor implements Converter<String,String>{

    @Override
    public String convert(String s) {
        return s;
    }
}
