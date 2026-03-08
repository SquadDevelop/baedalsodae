package com.project.baedalsodae.store.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface StoreReviewService {
        void calculateReviewCreated(UUID orderId, double rating);

        void calculateReviewUpdated(UUID reviewId, UUID orderId,double oldRating, double newRating);

        void calculateReviewDeleted(UUID reviewId, UUID orderId, double oldRating);
}
