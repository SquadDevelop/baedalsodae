package com.project.baedalsodae.review.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import com.project.baedalsodae.review.entity.Review;
import com.project.baedalsodae.review.repository.ReviewRepository;
import com.project.baedalsodae.review.service.ReviewService;
import com.project.baedalsodae.store.service.StoreReviewService;
import com.project.baedalsodae.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final OrderService orderService;
    private final StoreReviewService storeReviewService;

    @Override
    public TimeCursorPage<List<ReviewResponse>> getReviewsByUser(
            final UUID userId, final Instant cursor, final int size) {
        userService.getUser(userId);
        List<Review> foundReviews =
                reviewRepository.findNextPageWithUserId(userId, cursor, size + 1);
        boolean hasNext = foundReviews.size() > size;

        List<Review> reviews = hasNext ? foundReviews.subList(0, size) : foundReviews;

        LocalDateTime nextCursor =
                hasNext ? LocalDateTime.from(reviews.get(reviews.size() - 1).getCreatedAt()) : null;

        List<ReviewResponse> content = reviews.stream().map(ReviewResponse::from).toList();
        if (hasNext) {
            content = content.subList(0, content.size() - 1);
        }
        return TimeCursorPage.of(content, hasNext, nextCursor);
    }

    @Override
    public ReviewResponse getReviewDetail(final UUID userId, final UUID reviewId) {
        Review foundReview =
                reviewRepository
                        .findByIdAndUserId(userId, reviewId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(foundReview);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(
            final UUID userId, final UUID orderId, final ReviewRequest request) {
        if (!orderService.isOrderDelivered(orderId))
            throw new BusinessException(ErrorCode.REVIEW_BEFORE_DELIVERY_NOT_ALLOWED);
        Review savedReview =
                reviewRepository.save(
                        Review.create(userId, orderId, request.rating(), request.comment()));
        storeReviewService.calculateReviewCreated(orderId, request.rating());
        return ReviewResponse.from(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(
            final UUID userId, final UUID reviewId, final ReviewRequest request) {
        Review foundReview =
                reviewRepository
                        .findByIdAndUserId(userId, reviewId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (foundReview.getUserId() != userId) {
            throw new BusinessException(ErrorCode.REVIEW_UNAUTHORIZED);
        }
        double oldRating = foundReview.getRating();
        foundReview.update(request.rating(), request.comment());
        storeReviewService.calculateReviewUpdated(reviewId, foundReview.getOrderId(),oldRating, request.rating());
        return ReviewResponse.from(foundReview);
    }

    @Override
    @Transactional
    public ReviewResponse deleteReview(final UUID userId, final UUID reviewId) {
        Review foundReview =
                reviewRepository
                        .findByIdAndUserId(userId, reviewId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (foundReview.getUserId() != userId) {
            throw new BusinessException(ErrorCode.REVIEW_UNAUTHORIZED);
        }
        foundReview.softDelete(userId);
        storeReviewService.calculateReviewDeleted(reviewId, foundReview.getOrderId(), foundReview.getRating());
        return ReviewResponse.from(foundReview);
    }
}
