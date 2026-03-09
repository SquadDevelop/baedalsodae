package com.project.baedalsodae.order.service.impl;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.repository.OrderStatusHistoryRepository;
import com.project.baedalsodae.order.service.OrderStatusHistoryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createForCustomerOrderStatusHistory(UUID userId, Order savedOrder) {
        OrderStatusHistory orderStatusHistory =
                OrderStatusHistory.createForCustomer(savedOrder, userId);
        orderStatusHistoryRepository.save(orderStatusHistory);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createForCustomerOrderStatusHistory(
            UUID userId, OrderStatus fromStatus, Order savedOrder) {
        OrderStatusHistory orderStatusHistory =
                OrderStatusHistory.createForCustomer(savedOrder, fromStatus, userId);
        orderStatusHistoryRepository.save(orderStatusHistory);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createForOwnerOrderStatusHistory(
            UUID userId, OrderStatus fromStatus, Order savedOrder, String reason) {
        OrderStatusHistory orderStatusHistory =
                OrderStatusHistory.createForOwner(savedOrder, fromStatus, userId, reason);
        orderStatusHistoryRepository.save(orderStatusHistory);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createForSystemOrderStatusHistory(OrderStatus fromStatus, Order savedOrder) {
        OrderStatusHistory orderStatusHistory =
                OrderStatusHistory.createForSystem(savedOrder, fromStatus);
        orderStatusHistoryRepository.save(orderStatusHistory);
    }
}
