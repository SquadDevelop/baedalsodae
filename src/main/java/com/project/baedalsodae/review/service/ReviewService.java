package com.project.baedalsodae.review.service;

import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface ReviewService {
        TimeCursorPage<ReviewResponse> getReviews();
        ReviewResponse getReviewDetail(UUID reviewId);
        ReviewResponse createReview(ReviewRequest request);
        ReviewResponse updateReview(UUID reviewId, ReviewRequest request);
        ReviewResponse deleteReview(UUID reviewId);
}
