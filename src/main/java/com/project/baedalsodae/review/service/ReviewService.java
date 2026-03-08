package com.project.baedalsodae.review.service;

import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    TimeCursorPage<List<ReviewResponse>> getReviewsByUser(UUID userId, Instant cursor, int size);

    ReviewResponse getReviewDetail(UUID userId, UUID reviewId);

    ReviewResponse createReview(UUID userId, UUID orderId, ReviewRequest request);

    ReviewResponse updateReview(UUID userId, UUID reviewId, ReviewRequest request);

    ReviewResponse deleteReview(UUID userId, UUID reviewId);
}
