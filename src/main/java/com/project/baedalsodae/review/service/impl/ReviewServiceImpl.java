package com.project.baedalsodae.review.service.impl;

import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import com.project.baedalsodae.review.entity.Review;
import com.project.baedalsodae.review.repository.ReviewRepository;
import com.project.baedalsodae.review.service.ReviewService;
import com.project.baedalsodae.user.service.UserService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @Override
    public TimeCursorPage<List<ReviewResponse>> getReviewsByUser(final UUID userId, final Instant cursor, final int size) {
        userService.getUser(userId);
        List<Review> foundReviews = reviewRepository.findNextPageWithUserId(userId, cursor, size + 1);
        boolean hasNext = foundReviews.size() > size;

        List<Review> reviews = hasNext ? foundReviews.subList(0, size) : foundReviews;

        LocalDateTime nextCursor =
            hasNext
                ? LocalDateTime.from(
                reviews.get(reviews.size() - 1).getCreatedAt())
                : null;

        List<ReviewResponse> content = reviews.stream().map(ReviewResponse::from).toList();
        if (hasNext) {
            content = content.subList(0, content.size() - 1);
        }
        return TimeCursorPage.of(content, hasNext, nextCursor);
    }

    @Override
    public ReviewResponse getReviewDetail(final UUID userId, final UUID reviewId) {
        return null;
    }

    @Override
    public ReviewResponse createReview(final UUID userId, final ReviewRequest request) {
        return null;
    }

    @Override
    public ReviewResponse updateReview(final UUID userId, final UUID reviewId, final ReviewRequest request) {
        return null;
    }

    @Override
    public ReviewResponse deleteReview(final UUID userId, final UUID reviewId) {
        return null;
    }
}
