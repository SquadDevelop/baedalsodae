package com.project.baedalsodae.store.fixture;

import static com.project.baedalsodae.store.fixture.StoreHoursTestConstants.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreHours;
import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import com.project.baedalsodae.store.repository.StoreRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StoreHoursMockFixture {

    private StoreHoursMockFixture() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Store createMockStoreWithRepository(
            StoreRepository storeRepository, UUID storeId) {
        Store store = mock(Store.class);
        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        return store;
    }

    public static List<StoreHours> createMockStoreHoursList() {
        return Arrays.stream(DayOfWeek.values())
                .map(
                        day -> {
                            StoreHours storeHours = mock(StoreHours.class);
                            given(storeHours.getId()).willReturn(UUID.randomUUID());
                            given(storeHours.getDayOfWeek()).willReturn(day);
                            given(storeHours.getOpenTime()).willReturn(DEFAULT_OPEN_TIME);
                            given(storeHours.getCloseTime()).willReturn(DEFAULT_CLOSE_TIME);
                            given(storeHours.getBreakStart()).willReturn(DEFAULT_BREAK_START);
                            given(storeHours.getBreakEnd()).willReturn(DEFAULT_BREAK_END);
                            given(storeHours.isOpen()).willReturn(true);
                            return storeHours;
                        })
                .toList();
    }
}
