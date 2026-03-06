package com.project.baedalsodae.order.service.impl;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.repository.OrderStatusHistoryRepository;
import com.project.baedalsodae.order.service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createForCustomerOrderStatusHistory(UUID userId, Order savedOrder) {
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.createForCustomer(savedOrder, userId);
        orderStatusHistoryRepository.save(orderStatusHistory);
    }
}
