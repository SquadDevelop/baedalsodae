package com.project.baedalsodae.review.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import com.project.baedalsodae.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping()
    public ResponseEntity<ApiResponse<TimeCursorPage<List<ReviewResponse>>>> getReviews(UUID userId, @RequestParam(required = false) Instant cursor, @RequestParam(defaultValue = "10") int size) {
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_LIST_FOUND, reviewService.getReviewsByUser(userId, cursor, size)));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewDetail(UUID userId, @PathVariable UUID reviewId) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_DETAIL_FOUND, reviewService.getReviewDetail(userId, reviewId)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(UUID userId, @PathVariable UUID reviewId, @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_UPDATED, reviewService.updateReview(userId, reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> deleteReview(UUID userId, @PathVariable UUID reviewId) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_DELETED, reviewService.deleteReview(userId, reviewId)));
    }
}
