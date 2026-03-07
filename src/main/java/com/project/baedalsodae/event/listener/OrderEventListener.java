package com.project.baedalsodae.event.listener;

import com.project.baedalsodae.event.dto.PaymentCreatedEvent;
import com.project.baedalsodae.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderService orderService;

    @EventListener
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        // TODO: 결제 완료 후 주문 상태 업데이트 로직 구현
//        orderService.updateOrderStatus(event.orderId(), "PAYMENT_COMPLETED");
    }
}
