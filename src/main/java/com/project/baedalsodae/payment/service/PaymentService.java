package com.project.baedalsodae.payment.service;

import com.project.baedalsodae.order.dto.event.OrderCreatedEvent;

public interface PaymentService {
	void requestPayment(OrderCreatedEvent event);
}
