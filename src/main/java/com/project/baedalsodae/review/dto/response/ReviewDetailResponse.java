package com.project.baedalsodae.review.dto.response;

import com.project.baedalsodae.review.entity.Review;
import java.util.UUID;

public record ReviewDetailResponse(
        UUID reviewId, UUID orderId, UUID userId, int rating, String comment, Boolean isOwner) {
    public static ReviewDetailResponse of(Review review, Boolean isOwner) {
        return new ReviewDetailResponse(
                review.getId(),
                review.getOrderId(),
                review.getUserId(),
                review.getRating(),
                review.getContent(),
                isOwner);
    }
}
