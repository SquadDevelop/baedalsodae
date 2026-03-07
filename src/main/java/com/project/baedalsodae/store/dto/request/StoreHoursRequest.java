package com.project.baedalsodae.store.dto.request;

import com.project.baedalsodae.store.entity.enums.DayOfWeek;
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
}
