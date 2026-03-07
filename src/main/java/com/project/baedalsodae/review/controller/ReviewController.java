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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<TimeCursorPage<ReviewResponse>>> getReviews() {
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_LIST_FOUND, reviewService.getReviews()));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewDetail(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_DETAIL_FOUND, reviewService.getReviewDetail(reviewId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_CREATED,reviewService.createReview(request)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(@PathVariable UUID reviewId, @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_UPDATED, reviewService.updateReview(reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> deleteReview(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.REVIEW_DELETED, reviewService.deleteReview(reviewId)));
    }
}
