package com.faner.infrastructure.datasource.dynamic;

import com.faner.infrastructure.datasource.annotation.DS;
import com.faner.infrastructure.datasource.callback.DynamicDataSourceCallback;
import com.faner.infrastructure.datasource.utils.DataSourceUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 动态数据源拦截器.
 *
 * @作者 Faner
 * @创建时间 2021/12/31 21:35
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor, Ordered {

    private static final DataSourceClassResolver RESOLVER = new DataSourceClassResolver();

    private DynamicDataSourceCallback callback = null;

    private int order;

    public DynamicDataSourceAnnotationInterceptor(DynamicDataSourceCallback callback) {
        this.callback = callback;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        {

            Optional<DS> dsOnMethod = dsOnMethod(invocation);
            Optional<DS> dsOnClass = dsOnClass(invocation);

            String dataSource =
                    dsOnMethod.isPresent() && !StringUtils.isEmpty(dsOnMethod.get().dataSource())
                            ? dsOnMethod.get().dataSource()
                            : dsOnClass.isPresent() ? dsOnClass.get().dataSource()
                            : dsOnMethod.get().dataSource();

            // 处理表达式
            if (dataSource.contains("#")) {
                ExpressionParser parser = new SpelExpressionParser();
                dataSource = parser.parseExpression(dataSource)
                        .getValue(DynamicDataSourceExpressionHolder.getExpressionContext(), String.class);
            }

            boolean forceMaster =
                    dsOnMethod.isPresent() ? dsOnMethod.get().forceMaster()
                            : dsOnClass.get().forceMaster();

            boolean previousMasterOnlyValue = HintManager.isMasterRouteOnly();

            try {
                if (!StringUtils.isEmpty(dataSource)) {
                    DynamicDataSourceHolder.setDataSource(dataSource);
                } else {
                    if (this.callback != null) {
                        this.callback.setDefaultDataSource();
                    } else {
                        DynamicDataSourceHolder
                                .setDataSource(DataSourceUtils.getDynamicDefaultDataSource());
                    }
                }

                if (forceMaster) {
                    HintManager.clear();
                    HintManager hintManger = HintManager.getInstance();
                    if (!previousMasterOnlyValue) {
                        hintManger.setMasterRouteOnly();
                    }
                }

                return invocation.proceed();
            } finally {
                if (!StringUtils.isEmpty(dataSource)) {
                    DynamicDataSourceHolder.clearDataSource();
                }
                if (forceMaster && !previousMasterOnlyValue) {
                    HintManager.clear();
                }
            }
        }
    }

    private Optional<DS> dsOnMethod(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Optional<DS> optional = Optional
                .ofNullable(AnnotationUtils.findAnnotation(method, DS.class));
        return optional;
    }


    private Optional<DS> dsOnClass(MethodInvocation invocation) throws Throwable {
        Optional<DS> optional = Optional
                .ofNullable(AnnotationUtils.findAnnotation(RESOLVER.targetClass(invocation), DS.class));
        return optional;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
