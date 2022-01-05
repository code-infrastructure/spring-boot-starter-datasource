package com.faner.infrastructure.datasource.transaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 数据源事务拦截器.
 * @作者 Faner
 * @创建时间 2021/12/31 21:50
 */
public class DataSourceTransactionalAnnotationInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        Optional<Transactional> transactional = Optional
                .ofNullable(AnnotationUtils.findAnnotation(method, Transactional.class));
        if (!transactional.isPresent()) {
            transactional = Optional
                    .ofNullable(AnnotationUtils.findAnnotation(invocation.getThis().getClass(), Transactional.class));
        }

        boolean previousMasterOnlyValue = HintManager.isMasterRouteOnly();
        if (transactional.isPresent() && !transactional.get().readOnly()) {
            HintManager.clear();
            HintManager hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
        }

        try {
            return invocation.proceed();
        }finally {
            HintManager.clear();
            if (previousMasterOnlyValue) {
                HintManager hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
            }
        }

    }

}
