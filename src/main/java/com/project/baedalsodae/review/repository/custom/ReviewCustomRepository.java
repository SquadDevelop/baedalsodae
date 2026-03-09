package com.project.baedalsodae.review.repository.custom;

import com.project.baedalsodae.review.dto.query.ReviewSummary;
import com.project.baedalsodae.review.dto.response.ReviewDetailResponse;
import java.util.List;
import java.util.UUID;

public interface ReviewCustomRepository {
    List<ReviewDetailResponse> findByStoreId(UUID storeId, UUID currentUserId);

    ReviewSummary getSummary(UUID storeId);
}
