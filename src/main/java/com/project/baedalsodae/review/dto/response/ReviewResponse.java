package com.project.baedalsodae.review.dto.response;

import com.project.baedalsodae.review.entity.Review;
import java.util.UUID;

public record ReviewResponse(UUID reviewId, UUID orderId, UUID userId, double rating, String comment) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrderId(),
                review.getUserId(),
                review.getRating(),
                review.getContent());
    }
}
