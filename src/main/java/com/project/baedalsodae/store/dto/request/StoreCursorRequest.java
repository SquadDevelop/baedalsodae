package com.project.baedalsodae.store.dto.request;

import com.project.baedalsodae.store.entity.enums.SortType;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;

public record StoreCursorRequest(
        UUID lastId,
        Instant lastCreatedAt,
        Double lastRating,
        Integer lastReviewCount,
        @Min(1) Integer size) {
    public StoreCursorRequest normalize(SortType sortType) {
        if (lastId != null) return this; // 첫 페이지 아니면 그대로

        return switch (sortType) {
            case LATEST -> new StoreCursorRequest(null, Instant.now(), null, null, size);
            case RATING -> new StoreCursorRequest(null, null, Double.MAX_VALUE, null, size);
            case REVIEW -> new StoreCursorRequest(null, null, null, Integer.MAX_VALUE, size);
        };
    }

    public int getSize() {
        return size == null ? 10 : size;
    }
}
