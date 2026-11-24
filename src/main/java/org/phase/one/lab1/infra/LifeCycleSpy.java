package org.phase.one.lab1.infra;

import org.jspecify.annotations.NonNull;
import org.phase.one.lab1.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
class LifeCycleSpy implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(LifeCycleSpy.class);

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) {
        if (bean instanceof OrderService) {
            log.info("bean [1] {} in postProcessBeforeInitialization has been initialized for class {}", beanName, bean.getClass().getName());    
        }
        return bean;
    }
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
        if (bean instanceof OrderService) {
            log.info("bean [3] {} in postProcessAfterInitialization has been initialized for class {}", beanName, bean.getClass().getName());    
        }
        return  bean;
    }
}
