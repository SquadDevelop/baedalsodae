package com.project.baedalsodae.allowedRegion.dto;

import com.project.baedalsodae.allowedRegion.entity.enums.AllowedRegionSortType;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;

public record AllowedRegionCursorRequest(
        UUID lastId,
        AllowedRegionSortType sortType,
        Instant lastCreatedAt,
        String lastSidoName,
        String lastSigunguName,
        Boolean lastIsActive,
        String sidoCode,
        Boolean activeFilter,
        @Min(1) Integer size) {
    public AllowedRegionCursorRequest initCursor(AllowedRegionSortType sortType) {
        if (lastId != null) return this;

        AllowedRegionSortType type = sortType == null ? AllowedRegionSortType.ACTIVE : sortType;

        return switch (type) {
            case LATEST ->
                    new AllowedRegionCursorRequest(
                            null,
                            type,
                            Instant.now(),
                            null,
                            null,
                            null,
                            sidoCode,
                            activeFilter,
                            getSize());
            case ACTIVE ->
                    new AllowedRegionCursorRequest(
                            null,
                            type,
                            Instant.now(),
                            null,
                            null,
                            true,
                            sidoCode,
                            activeFilter,
                            getSize());
            case SIDO_NAME ->
                    new AllowedRegionCursorRequest(
                            null,
                            type,
                            null,
                            "\uFFFF",
                            "\uFFFF",
                            null,
                            sidoCode,
                            activeFilter,
                            getSize());
        };
    }

    public int getSize() {
        return size == null ? 10 : size;
    }
}
