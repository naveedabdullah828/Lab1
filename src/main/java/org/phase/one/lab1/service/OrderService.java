package org.phase.one.lab1.service;

import jakarta.annotation.PostConstruct;
import org.phase.one.lab1.domain.Auditable;
import org.phase.one.lab1.domain.AuditableAction;
import org.phase.one.lab1.domain.AuditAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import static org.phase.one.lab1.domain.AuditableAction.NO_ACTION;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final ObjectProvider<OrderService> self;

    public OrderService(ObjectProvider<OrderService> self) {
        this.self = self;
    }

    @PostConstruct
    void init() {
        log.info("bean [2] orderService in PostConstruct has been initialized for class {}", this.getClass().getName());
    }

    @Auditable(action = AuditableAction.PROCESS_ORDER)
    public void placeOrder(String item) {
        log.info("Action [{}] Placing order for: {}", currentAction(), item);
        self.getObject().processPayment(item);
        log.info("Action [{}] Placed order for: {}", currentAction(), item);
    }

    @Auditable(action = AuditableAction.PROCESS_PAYMENT)
    public void processPayment(String item) {
        log.info("Action [{}] Processing payment for: {}", currentAction(), item);
    }

    private AuditableAction currentAction() {
        return AuditAspect.CURRENT_ACTION.orElse(NO_ACTION);
    }
}
