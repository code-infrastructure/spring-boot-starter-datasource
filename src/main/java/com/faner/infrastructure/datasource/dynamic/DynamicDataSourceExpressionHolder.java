package com.faner.infrastructure.datasource.dynamic;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 动态数据源表达式解析。
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:37
 */
public class DynamicDataSourceExpressionHolder {

    /**
     * 表达式上下文。
     */
    private static final ThreadLocal<StandardEvaluationContext> DYNAMIC_DATASOURCE_EXPRESSION_CONTEXT = new ThreadLocal<StandardEvaluationContext>(){
        @Override
        protected StandardEvaluationContext initialValue() {
            return new StandardEvaluationContext();
        }
    };

    /**
     * 设置表达式值.
     * @param exprKey
     * @param exprValue
     */
    public static void put(String exprKey, String exprValue) {
        DYNAMIC_DATASOURCE_EXPRESSION_CONTEXT.get().setVariable(exprKey, exprValue);
    }

    /**
     * 清除表达式值.
     * @param exprKey
     */
    public static void clear(String exprKey) {
        DYNAMIC_DATASOURCE_EXPRESSION_CONTEXT.get().setVariable(exprKey, null);
    }

    public static StandardEvaluationContext getExpressionContext(){
        return DYNAMIC_DATASOURCE_EXPRESSION_CONTEXT.get();
    }


}
