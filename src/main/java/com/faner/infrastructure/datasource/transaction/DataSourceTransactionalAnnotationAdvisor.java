package com.faner.infrastructure.datasource.transaction;

import lombok.NonNull;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据库事务Advisor.
 *
 * http://wiki.lianjia.com/pages/viewpage.action?pageId=395720107
 *
 */
public class DataSourceTransactionalAnnotationAdvisor extends AbstractPointcutAdvisor implements
        BeanFactoryAware {

    private Advice advice;

    private Pointcut pointcut;

    public DataSourceTransactionalAnnotationAdvisor(
            @NonNull DataSourceTransactionalAnnotationInterceptor dataSourceTransactionalAnnotationInterceptor) {
        this.advice = dataSourceTransactionalAnnotationInterceptor;
        this.pointcut = buildPointcut();
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    private Pointcut buildPointcut() {
        Pointcut cpc = new AnnotationMatchingPointcut(Transactional.class, true);
        Pointcut mpc = AnnotationMatchingPointcut.forMethodAnnotation(Transactional.class);
        return new ComposablePointcut(cpc).union(mpc);
    }
}

