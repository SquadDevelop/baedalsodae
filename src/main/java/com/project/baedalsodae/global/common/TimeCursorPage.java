package com.project.baedalsodae.global.common;

import java.time.LocalDateTime;

public record TimeCursorPage<T>(T content, boolean hasNext, LocalDateTime nextCursor) {
    public static <T> TimeCursorPage<T> of(T content, boolean hasNext, LocalDateTime nextCursor) {
        return new TimeCursorPage<>(content, hasNext, nextCursor);
    }
}
