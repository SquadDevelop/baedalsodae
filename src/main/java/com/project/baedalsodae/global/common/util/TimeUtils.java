package com.project.baedalsodae.global.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtils {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    private TimeUtils() {
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return localDateTime.atZone(ASIA_SEOUL).toInstant();
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ASIA_SEOUL);
    }

    public static Instant toStartOfDayInstant(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ASIA_SEOUL).toInstant();
    }

    public static Instant toStartOfNextDayInstant(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay(ASIA_SEOUL).toInstant();
    }
}