package org.phase.one.lab1;

import org.phase.one.lab1.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class Lab1Application {

    private static final Logger log = LoggerFactory.getLogger(Lab1Application.class);

    static void main(String[] args) {
        SpringApplication.run(Lab1Application.class, args);
    }

    @Bean
    CommandLineRunner labRunner(OrderService orderService) {
        return _ -> {

            log.info("--- Test 1: External call through Spring proxy ---");
            orderService.processPayment("Mechanical Keyboard");

            log.info("--- Test 2: Internal self-invocation (this.processPayment) ---");
            orderService.placeOrder("USB-C Monitor");
        };
    }
}
