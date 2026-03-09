package com.project.baedalsodae.store.dto.response.store;

import com.project.baedalsodae.review.dto.query.ReviewSummary;
import com.project.baedalsodae.review.dto.response.ReviewDetailResponse;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreReviewResponse {
    private List<ReviewDetailResponse> reviews;
    private ReviewSummary reviewSummary;

    public static StoreReviewResponse of(
            List<ReviewDetailResponse> reviews, ReviewSummary reviewSummary) {
        return StoreReviewResponse.builder().reviews(reviews).reviewSummary(reviewSummary).build();
    }
}
