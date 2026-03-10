package com.project.baedalsodae.store.dto.request.store;

import com.project.baedalsodae.store.enums.SortType;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;

public record StoreCursorRequest(
        UUID lastId,
        SortType sortType,
        Instant lastCreatedAt,
        Double lastRating,
        Integer lastReviewCount,
        @Min(1) Integer size) {
    public StoreCursorRequest initCursor(SortType sortType) {
        if (lastId != null) return this;

        SortType type = sortType == null ? SortType.LATEST : sortType;

        return switch (type) {
            case LATEST -> new StoreCursorRequest(null, type, Instant.now(), null, null, getSize());
            case RATING ->
                    new StoreCursorRequest(null, type, null, Double.MAX_VALUE, null, getSize());
            case REVIEW ->
                    new StoreCursorRequest(null, type, null, null, Integer.MAX_VALUE, getSize());
        };
    }

    public int getSize() {
        return size == null ? 10 : size;
    }
}
