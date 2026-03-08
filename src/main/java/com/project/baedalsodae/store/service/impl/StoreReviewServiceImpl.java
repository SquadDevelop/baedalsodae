package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
public class StoreReviewServiceImpl implements StoreReviewService {
    private final StoreRepository storeRepository;
    private final OrderService orderService;

    @Override
    @Transactional
    public void calculateReviewCreated(final UUID orderId, final double rating) {
            Order order = orderService.findById(orderId);
            Store store = storeRepository.findById(order.getStoreId()).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
            store.addRating(rating);
    }

    @Override
    @Transactional
    public void calculateReviewUpdated(final UUID reviewId, final UUID orderId, final double oldRating, final double newRating) {
        Order order = orderService.findById(orderId);
        Store store = storeRepository.findById(order.getStoreId()).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        store.updateRating(oldRating, newRating);
    }

    @Override
    @Transactional
    public void calculateReviewDeleted(final UUID reviewId,final UUID orderId, final double oldRating) {
        Order order = orderService.findById(orderId);
        Store store = storeRepository.findById(order.getStoreId()).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        store.deleteRating(oldRating);
    }
}
