package com.project.baedalsodae.store.dto.request;

import com.project.baedalsodae.store.entity.enums.SortType;
import jakarta.validation.constraints.Min;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

public record StoreCursorRequest(
        UUID lastId,
        Instant lastCreatedAt,
        Double lastRating,
        Integer lastReviewCount,
        @Min(1) Integer size
) {
    public void validate(SortType sortType) {
        switch (sortType) {
            case LATEST -> Assert.notNull(lastCreatedAt, "최신순 정렬엔 lastCreatedAt 필요");
            case RATING -> Assert.notNull(lastRating, "별점순 정렬엔 lastRating 필요");
            case REVIEW -> Assert.notNull(lastReviewCount, "리뷰순 정렬엔 lastReviewCount 필요");
        }
    }

    public int getSize() {
        return size == null ? 10 : size;
    }
}