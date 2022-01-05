package com.faner.infrastructure.datasource.dynamic;

import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import java.util.Stack;

/**
 * 数据源HOLDER
 * @作者 Faner
 * @创建时间 2021/12/31 21:32
 */
public class DynamicDataSourceHolder {

    private static final ThreadLocal<Stack<String>> HOLDER = new ThreadLocal<Stack<String>>(){
        @Override
        protected Stack<String> initialValue() {
            return new Stack<>();
        }
    };

    public static void setDataSource(String dataSource){
        HOLDER.get().push(dataSource);
    }

    public static String getDataSource(){
        if(!HOLDER.get().isEmpty()) {
            return HOLDER.get().peek();
        }
        return DataSourceUtils.getDynamicDefaultDataSource();
    }

    public static void clearDataSource() {
        if(!HOLDER.get().isEmpty()) {
            HOLDER.get().pop();

            if(HOLDER.get().isEmpty()){
                HOLDER.remove();
            }
        }
    }

}
