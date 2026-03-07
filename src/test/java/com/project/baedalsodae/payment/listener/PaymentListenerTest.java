package com.project.baedalsodae.payment.listener;

import static org.mockito.BDDMockito.then;

import com.project.baedalsodae.order.dto.event.OrderCreatedEvent;
import com.project.baedalsodae.payment.service.PaymentService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentListenerTest {

    @InjectMocks private PaymentListener paymentListener;

    @Mock private PaymentService paymentService;

    @Test
    @DisplayName("성공 - OrderCreatedEvent를 받으면 paymentService.processPayment()가 호출됨")
    void handleOrderCreated_callsProcessPayment() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int finalAmount = 18000;
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, finalAmount);

        // when
        paymentListener.handleOrderCreated(event);

        // then
        then(paymentService).should().processPayment(orderId, userId, finalAmount);
    }

    @Test
    @DisplayName("성공 - processPayment에 올바른 orderId, userId, finalAmount 값이 전달됨")
    void handleOrderCreated_passesCorrectArguments() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int finalAmount = 32000;
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, finalAmount);

        // when
        paymentListener.handleOrderCreated(event);

        // then
        then(paymentService).should().processPayment(orderId, userId, finalAmount);
        // 다른 인자로는 호출되지 않았는지 확인
        then(paymentService).shouldHaveNoMoreInteractions();
    }
}
