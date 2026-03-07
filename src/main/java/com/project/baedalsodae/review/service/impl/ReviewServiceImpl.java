package com.project.baedalsodae.review.service.impl;

import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import com.project.baedalsodae.review.repository.ReviewRepository;
import com.project.baedalsodae.review.service.ReviewService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    @Override
    public TimeCursorPage<ReviewResponse> getReviews() {
        return null;
    }

    @Override
    public ReviewResponse getReviewDetail(final UUID reviewId) {
        return null;
    }

    @Override
    public ReviewResponse createReview(final ReviewRequest request) {
        return null;
    }

    @Override
    public ReviewResponse updateReview(final UUID reviewId, final ReviewRequest request) {
        return null;
    }

    @Override
    public ReviewResponse deleteReview(final UUID reviewId) {
        return null;
    }
}
