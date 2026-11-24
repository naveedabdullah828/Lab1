package org.phase.one.lab1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.phase.one.lab1.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class Lab1ApplicationTests {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Given an order item, when placeOrder is invoked, then outer and nested auditable actions are logged in order")
    void shouldPropagateAndRestoreAuditableActionAcrossNestedCalls(CapturedOutput output) {
        orderService.placeOrder("USB-C Monitor");

        assertThat(output.getOut()).containsSubsequence(
                "Action [PROCESS_ORDER] Placing order for: USB-C Monitor",
                "Action [PROCESS_PAYMENT] Processing payment for: USB-C Monitor",
                "Action [PROCESS_ORDER] Placed order for: USB-C Monitor"
        );
    }

}
