package com.project.baedalsodae.allowedRegion.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import com.project.baedalsodae.allowedRegion.repository.AllowedRegionRepository;
import com.project.baedalsodae.allowedRegion.service.impl.AllowedRegionServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AllowedRegionServiceImplTest {

    @Mock private AllowedRegionRepository allowedRegionRepository;

    @InjectMocks private AllowedRegionServiceImpl allowedRegionService;

    private static final String SIDO_CODE = "11";
    private static final String SIDO_NAME = "서울특별시";
    private static final String SIGUNGU_CODE = "11010";
    private static final String SIGUNGU_NAME = "종로구";

    @Nested
    @DisplayName("isAllowedByCode")
    class IsAllowedByCode {

        @Test
        @DisplayName("지역이 존재하고 활성화 상태이면 true를 반환한다")
        void returnsTrueWhenActiveRegionExists() {
            AllowedRegion region = AllowedRegion.create(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);
            given(allowedRegionRepository.findBySigunguCodeAndIsDeletedIsFalse(SIGUNGU_CODE))
                    .willReturn(Optional.of(region));

            boolean result = allowedRegionService.isAllowedByCode(SIGUNGU_CODE);

            assertTrue(result);
        }

        @Test
        @DisplayName("지역이 존재하지만 비활성화 상태이면 false를 반환한다")
        void returnsFalseWhenInactiveRegionExists() {
            AllowedRegion region = AllowedRegion.create(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);
            region.deactivate();
            given(allowedRegionRepository.findBySigunguCodeAndIsDeletedIsFalse(SIGUNGU_CODE))
                    .willReturn(Optional.of(region));

            boolean result = allowedRegionService.isAllowedByCode(SIGUNGU_CODE);

            assertFalse(result);
        }

        @Test
        @DisplayName("지역이 존재하지 않으면 false를 반환한다")
        void returnsFalseWhenRegionNotFound() {
            given(allowedRegionRepository.findBySigunguCodeAndIsDeletedIsFalse(SIGUNGU_CODE))
                    .willReturn(Optional.empty());

            boolean result = allowedRegionService.isAllowedByCode(SIGUNGU_CODE);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("isAllowedByName")
    class IsAllowedByName {

        @Test
        @DisplayName("지역이 존재하고 활성화 상태이면 true를 반환한다")
        void returnsTrueWhenActiveRegionExists() {
            AllowedRegion region = AllowedRegion.create(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);
            given(allowedRegionRepository.findBySigunguNameAndIsDeletedIsFalse(SIGUNGU_NAME))
                    .willReturn(Optional.of(region));

            boolean result = allowedRegionService.isAllowedByName(SIGUNGU_NAME);

            assertTrue(result);
        }

        @Test
        @DisplayName("지역이 존재하지만 비활성화 상태이면 false를 반환한다")
        void returnsFalseWhenInactiveRegionExists() {
            AllowedRegion region = AllowedRegion.create(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);
            region.deactivate();
            given(allowedRegionRepository.findBySigunguNameAndIsDeletedIsFalse(SIGUNGU_NAME))
                    .willReturn(Optional.of(region));

            boolean result = allowedRegionService.isAllowedByName(SIGUNGU_NAME);

            assertFalse(result);
        }

        @Test
        @DisplayName("지역이 존재하지 않으면 false를 반환한다")
        void returnsFalseWhenRegionNotFound() {
            given(allowedRegionRepository.findBySigunguNameAndIsDeletedIsFalse(SIGUNGU_NAME))
                    .willReturn(Optional.empty());

            boolean result = allowedRegionService.isAllowedByName(SIGUNGU_NAME);

            assertFalse(result);
        }
    }
}
