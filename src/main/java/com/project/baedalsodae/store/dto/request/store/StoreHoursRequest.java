package com.project.baedalsodae.store.dto.request.store;

import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreHoursRequest {

    @NotNull(message = "요일은 필수입니다.")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "오픈 시간은 필수입니다.")
    private LocalTime openTime;

    @NotNull(message = "마감 시간은 필수입니다.")
    private LocalTime closeTime;

    private LocalTime breakStart;

    private LocalTime breakEnd;

    private boolean isOpen = true;

    @AssertTrue(message = "영업 종료 시간은 시작 시간보다 늦어야 합니다.")
    public boolean isValidOpenCloseTime() {
        if (openTime == null || closeTime == null) {
            return true;
        }
        return closeTime.isAfter(openTime);
    }

    @AssertTrue(message = "브레이크 타임이 올바르지 않습니다. 시작 시간은 종료 시간보다 빨라야 하며, 영업시간 내에 있어야 합니다.")
    public boolean isValidBreakTime() {
        if (breakStart == null && breakEnd == null) {
            return true;
        }

        if (breakStart == null || breakEnd == null) {
            return false;
        }

        if (openTime == null || closeTime == null) {
            return true;
        }

        return breakEnd.isAfter(breakStart)
                && breakStart.isAfter(openTime)
                && breakEnd.isBefore(closeTime);
    }
}
