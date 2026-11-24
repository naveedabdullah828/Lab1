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
