# Lab 1: Audit Context Propagation Challenge

## System Requirements
- **Java**: JDK 25
- **Build Tool**: Maven 3.9+ (or use the included `./mvnw` wrapper)
- **Framework**: Spring Boot 4.2.0

## Overview
This service tracks execution context across business operations using a custom `@Auditable` annotation, an AspectJ `@Around` aspect (`AuditAspect`), and Java's `ScopedValue` (`AuditAspect.CURRENT_ACTION`).

When an annotated method executes, `AuditAspect` binds the configured `AuditableAction` to `AuditAspect.CURRENT_ACTION` for the duration of that method call.

## Problem Statement
When `OrderService.placeOrder("USB-C Monitor")` is invoked during `Test 2`, the application currently logs `NO_ACTION` for every step of the order flow:

### Current Output
```text
--- Test 2: Internal self-invocation (this.processPayment) ---
Action [NO_ACTION] Placing order for: USB-C Monitor
Action [NO_ACTION] Processing payment for: USB-C Monitor
Action [NO_ACTION] Placed order for: USB-C Monitor
```

### Expected Output
Placing an order should execute under the `PROCESS_ORDER` audit action, transition to `PROCESS_PAYMENT` while processing the payment, and restore `PROCESS_ORDER` once payment processing completes:

```text
--- Test 2: Internal self-invocation (this.processPayment) ---
Action [PROCESS_ORDER] Placing order for: USB-C Monitor
Action [PROCESS_PAYMENT] Processing payment for: USB-C Monitor
Action [PROCESS_ORDER] Placed order for: USB-C Monitor
```

## Task & Constraints
1. Identify why the audit context is neither bound nor switched as expected during `placeOrder`.
2. Fix the implementation so that `placeOrder` produces the expected output.
3. Preserve dynamic context resolution via `AuditAspect.CURRENT_ACTION` (do not hardcode `AuditableAction` values in log statements).
4. Ensure all tests pass, including the architectural rules in `ArchitectureTest`:
   - Classes in `..service..` must not depend on `..infra..`.
   - Packages must be free of cyclic dependencies.
   - `@Service` classes must remain stateless with `final` fields only.

## Running the Project

Run the application:
```bash
./mvnw spring-boot:run
```

Run the test suite (functional + ArchUnit tests):
```bash
./mvnw test
```

## Root Cause Analysis
Two issues cause the broken output in `Test 2`:
1. **Missing outer annotation**: `OrderService.placeOrder` is not annotated with `@Auditable(action = AuditableAction.PROCESS_ORDER)`, so `AuditAspect` never binds `PROCESS_ORDER` to `AuditAspect.CURRENT_ACTION`.
2. **Spring AOP self-invocation trap**: Inside `placeOrder`, calling `this.processPayment(item)` invokes the method directly on the raw `OrderService` target instance (`this`) rather than routing through the Spring CGLIB proxy (`OrderService$$SpringCGLIB$$0`). Because the proxy is bypassed, `AuditAspect` never intercepts `processPayment`, and the `ScopedValue` context remains unchanged.

## Solutions (Compliant with `ArchitectureTest`)
Because `ArchitectureTest` requires all `@Service` fields to be `final` and packages to be free of cycles, mutable field self-injection (`@Autowired private OrderService self;`) is rejected. Below are three valid ways to fix the issue:

### Approach 1: Using `ObjectProvider<OrderService>`
Inject `ObjectProvider<OrderService>` via constructor and resolve the proxied bean when invoking `processPayment`. This avoids eager circular bean initialization while keeping both methods in `OrderService`.

```java
@Service
public class OrderService {

    private final ObjectProvider<OrderService> self;

    public OrderService(ObjectProvider<OrderService> self) {
        this.self = self;
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
}
```

### Approach 2: Extract `PaymentService` (Best for Single Responsibility)
Move `processPayment` into a dedicated `PaymentService` collaborator and inject it into `OrderService` via constructor. Calling `paymentService.processPayment(item)` crosses a bean boundary, so it naturally routes through the `PaymentService` CGLIB proxy.

```java
@Service
public class PaymentService {

    @Auditable(action = AuditableAction.PROCESS_PAYMENT)
    public void processPayment(String item) {
        AuditableAction action = AuditAspect.CURRENT_ACTION.orElse(NO_ACTION);
        log.info("Action [{}] Processing payment for: {}", action, item);
    }
}
```

```java
@Service
public class OrderService {

    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Auditable(action = AuditableAction.PROCESS_ORDER)
    public void placeOrder(String item) {
        log.info("Action [{}] Placing order for: {}", currentAction(), item);
        paymentService.processPayment(item);
        log.info("Action [{}] Placed order for: {}", currentAction(), item);
    }
}
```

### Approach 3: Constructor Self-Injection with `@Lazy` (Not Recommended)
Inject `OrderService` into its own constructor with `@Lazy`. While this satisfies the `final` field rule in `ArchitectureTest` and routes `self.processPayment(item)` through the proxy, it introduces a circular self-reference and masks a service decomposition issue.

```java
@Service
public class OrderService {

    private final OrderService self;

    public OrderService(@Lazy OrderService self) {
        this.self = self;
    }

    @Auditable(action = AuditableAction.PROCESS_ORDER)
    public void placeOrder(String item) {
        log.info("Action [{}] Placing order for: {}", currentAction(), item);
        self.processPayment(item);
        log.info("Action [{}] Placed order for: {}", currentAction(), item);
    }

    @Auditable(action = AuditableAction.PROCESS_PAYMENT)
    public void processPayment(String item) {
        log.info("Action [{}] Processing payment for: {}", currentAction(), item);
    }
}
```

