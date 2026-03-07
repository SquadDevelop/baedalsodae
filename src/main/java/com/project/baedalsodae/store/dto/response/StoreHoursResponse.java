package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.store.entity.StoreHours;
import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class StoreHoursResponse {

    @Getter
    @AllArgsConstructor
    public static class StoreHoursInfo {
        private UUID storeId;
        private List<StoreHourDto> storeHours;

        public boolean isEmpty() {
            return storeHours == null || storeHours.isEmpty();
        }
    }

    @Getter
    @Builder
    public static class StoreHourDto {
        private UUID storeHoursId;
        private DayOfWeek dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private LocalTime breakStart;
        private LocalTime breakEnd;
        private boolean isOpen;

        public static StoreHourDto fromEntity(StoreHours storeHours) {
            return StoreHourDto.builder()
                    .storeHoursId(storeHours.getId())
                    .dayOfWeek(storeHours.getDayOfWeek())
                    .openTime(storeHours.getOpenTime())
                    .closeTime(storeHours.getCloseTime())
                    .breakStart(storeHours.getBreakStart())
                    .breakEnd(storeHours.getBreakEnd())
                    .isOpen(storeHours.isOpen())
                    .build();
        }

        public static List<StoreHourDto> fromEntityList(List<StoreHours> storeHoursList) {
            return storeHoursList.stream().map(StoreHourDto::fromEntity).toList();
        }
    }
}
