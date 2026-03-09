package com.project.baedalsodae.review.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewSummary {
    private Long totalReviews;
    private Double averageRating;
}
