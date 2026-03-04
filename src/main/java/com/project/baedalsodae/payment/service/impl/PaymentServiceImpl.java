package com.project.baedalsodae.payment.service.impl;


import com.project.baedalsodae.order.dto.event.OrderCreatedEvent;
import com.project.baedalsodae.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	public void requestPayment(OrderCreatedEvent event) {
		// TODO: 결제 도메인 담당자분 구현
	}

}
