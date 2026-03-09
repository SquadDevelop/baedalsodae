package com.project.baedalsodae.store.fixture;

import static com.project.baedalsodae.store.fixture.StoreHoursTestConstants.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.dto.request.StoreHoursRequest;
import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import java.util.Arrays;
import java.util.List;

public class StoreHoursRequestFixture {

    private StoreHoursRequestFixture() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static List<StoreHoursRequest> createDefaultRequests() {
        return Arrays.stream(DayOfWeek.values())
                .map(
                        day ->
                                new StoreHoursRequest(
                                        day,
                                        DEFAULT_OPEN_TIME,
                                        DEFAULT_CLOSE_TIME,
                                        DEFAULT_BREAK_START,
                                        DEFAULT_BREAK_END,
                                        true))
                .toList();
    }

    public static List<StoreHoursRequest> createRequestsWithDuplicateDay() {
        return List.of(
                new StoreHoursRequest(
                        DayOfWeek.MON, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.MON, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.TUE, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.WED, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.THU, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.FRI, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.SAT, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true));
    }

    public static List<StoreHoursRequest> createRequestsWithInsufficientDays() {
        return List.of(
                new StoreHoursRequest(
                        DayOfWeek.MON, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true),
                new StoreHoursRequest(
                        DayOfWeek.TUE, DEFAULT_OPEN_TIME, DEFAULT_CLOSE_TIME, null, null, true));
    }
}
