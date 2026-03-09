package com.project.baedalsodae.allowedRegion.service;

import static com.project.baedalsodae.allowedRegion.service.fixture.AllowedRegionFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionPageResponse;
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

    @Nested
    @DisplayName("isAllowedByCode")
    class IsAllowedByCode {

        @Test
        @DisplayName("지역이 존재하고 활성화 상태이면 true를 반환한다")
        void returnsTrueWhenActiveRegionExists() {
            given(allowedRegionRepository.findBySigunguCodeAndIsDeletedIsFalse(SIGUNGU_CODE))
                    .willReturn(Optional.of(createActiveRegion()));

            boolean result = allowedRegionService.isAllowedByCode(SIGUNGU_CODE);

            assertTrue(result);
        }

        @Test
        @DisplayName("지역이 존재하지만 비활성화 상태이면 false를 반환한다")
        void returnsFalseWhenInactiveRegionExists() {
            given(allowedRegionRepository.findBySigunguCodeAndIsDeletedIsFalse(SIGUNGU_CODE))
                    .willReturn(Optional.of(createInactiveRegion()));

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
            given(allowedRegionRepository.findBySigunguNameAndIsDeletedIsFalse(SIGUNGU_NAME))
                    .willReturn(Optional.of(createActiveRegion()));

            boolean result = allowedRegionService.isAllowedByName(SIGUNGU_NAME);

            assertTrue(result);
        }

        @Test
        @DisplayName("지역이 존재하지만 비활성화 상태이면 false를 반환한다")
        void returnsFalseWhenInactiveRegionExists() {
            given(allowedRegionRepository.findBySigunguNameAndIsDeletedIsFalse(SIGUNGU_NAME))
                    .willReturn(Optional.of(createInactiveRegion()));

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

    @Nested
    @DisplayName("getAllowedRegions")
    class GetAllowedRegions {

        @Test
        @DisplayName("지역 목록을 반환한다")
        void returnsRegionList() {
            given(allowedRegionRepository.findAllowedRegionsByCursor(any()))
                    .willReturn(createSingleRegionSlice());

            AllowedRegionPageResponse response =
                    allowedRegionService.getAllowedRegions(createDefaultCursorRequest());

            assertThat(response.getAllowedRegions()).hasSize(1);
            verify(allowedRegionRepository).findAllowedRegionsByCursor(any());
        }

        @Test
        @DisplayName("결과가 비어있으면 빈 목록과 hasNext=false를 반환한다")
        void returnsEmptyResponseWhenNoRegions() {
            given(allowedRegionRepository.findAllowedRegionsByCursor(any()))
                    .willReturn(createEmptySlice());

            AllowedRegionPageResponse response =
                    allowedRegionService.getAllowedRegions(createDefaultCursorRequest());

            assertThat(response.getAllowedRegions()).isEmpty();
            assertThat(response.isHasNext()).isFalse();
            assertThat(response.getAllowedRegionCount()).isZero();
        }

        @Test
        @DisplayName("다음 페이지가 있으면 hasNext=true를 반환한다")
        void returnsHasNextTrueWhenMorePagesExist() {
            given(allowedRegionRepository.findAllowedRegionsByCursor(any()))
                    .willReturn(createSliceWithHasNext());

            AllowedRegionPageResponse response =
                    allowedRegionService.getAllowedRegions(createDefaultCursorRequest());

            assertThat(response.isHasNext()).isTrue();
        }

        @Test
        @DisplayName("SIDO_NAME 정렬 요청으로 조회된다")
        void callsRepositoryWithSidoNameSortRequest() {
            given(allowedRegionRepository.findAllowedRegionsByCursor(any()))
                    .willReturn(createSingleRegionSlice());

            AllowedRegionPageResponse response =
                    allowedRegionService.getAllowedRegions(createCursorRequestWithSidoName());

            assertThat(response.getAllowedRegions()).hasSize(1);
            verify(allowedRegionRepository).findAllowedRegionsByCursor(any());
        }

        @Test
        @DisplayName("sidoCode 필터 요청으로 조회된다")
        void callsRepositoryWithSidoCodeFilterRequest() {
            given(allowedRegionRepository.findAllowedRegionsByCursor(any()))
                    .willReturn(createEmptySlice());

            allowedRegionService.getAllowedRegions(createCursorRequestWithSidoCode());

            verify(allowedRegionRepository).findAllowedRegionsByCursor(any());
        }

        @Test
        @DisplayName("activeFilter 요청으로 조회된다")
        void callsRepositoryWithActiveFilterRequest() {
            given(allowedRegionRepository.findAllowedRegionsByCursor(any()))
                    .willReturn(createEmptySlice());

            allowedRegionService.getAllowedRegions(createCursorRequestWithActiveFilter());

            verify(allowedRegionRepository).findAllowedRegionsByCursor(any());
        }
    }
}
