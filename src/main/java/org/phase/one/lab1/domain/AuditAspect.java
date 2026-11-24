package org.phase.one.lab1.domain;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    public static final ScopedValue<AuditableAction> CURRENT_ACTION = ScopedValue.newInstance();
    
    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long start = System.nanoTime();
        AuditableAction actionName = auditable.action();
        try {
            return ScopedValue.where(CURRENT_ACTION, actionName)
                    .call(joinPoint::proceed);
        } catch (Throwable throwable) {
            log.error("Error occurred while executing {}", actionName, throwable);
            throw throwable;
        }

    }
    
}
