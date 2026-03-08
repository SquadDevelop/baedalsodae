package com.project.baedalsodae.store.service;

import static com.project.baedalsodae.store.fixture.StoreHoursMockFixture.*;
import static com.project.baedalsodae.store.fixture.StoreHoursRequestFixture.*;
import static com.project.baedalsodae.store.fixture.StoreHoursTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.dto.response.StoreHoursResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreHours;
import com.project.baedalsodae.store.repository.StoreHoursRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.service.impl.StoreHoursServiceImpl;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreHoursServiceImplTest {

    @Mock private StoreHoursRepository storeHoursRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks private StoreHoursServiceImpl storeHoursService;

    private UUID storeId;
    private UUID ownerId;
    private UserDetailsImpl managerUserDetails;
    private UserDetailsImpl ownerUserDetails;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        managerUserDetails = createManagerUserDetails();
        ownerUserDetails = createOwnerUserDetails(ownerId);
    }

    @Nested
    @DisplayName("영업시간 등록")
    class CreateStoreHours {

        @Test
        @DisplayName("실패: 요청 목록이 비어있으면 예외가 발생한다")
        void createStoreHours_fail_emptyRequest() {
            assertThatThrownBy(
                            () ->
                                    storeHoursService.createStoreHours(
                                            storeId, managerUserDetails, List.of()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }

        @Test
        @DisplayName("실패: 7일 미만으로 요청하면 예외가 발생한다")
        void createStoreHours_fail_insufficientDays() {
            assertThatThrownBy(
                            () ->
                                    storeHoursService.createStoreHours(
                                            storeId,
                                            managerUserDetails,
                                            createRequestsWithInsufficientDays()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }

        @Test
        @DisplayName("실패: 중복된 요일이 있으면 예외가 발생한다")
        void createStoreHours_fail_duplicateDay() {
            assertThatThrownBy(
                            () ->
                                    storeHoursService.createStoreHours(
                                            storeId,
                                            managerUserDetails,
                                            createRequestsWithDuplicateDay()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void createStoreHours_fail_storeNotFound() {
            given(storeRepository.findByIdAndIsDeletedIsFalse(storeId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    storeHoursService.createStoreHours(
                                            storeId, managerUserDetails, createDefaultRequests()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void createStoreHours_fail_forbidden() {
            Store store = createMockStoreWithRepository(storeRepository, storeId);
            given(store.getUserId()).willReturn(UUID.randomUUID());

            assertThatThrownBy(
                            () ->
                                    storeHoursService.createStoreHours(
                                            storeId, ownerUserDetails, createDefaultRequests()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패: 이미 영업시간이 등록되어 있으면 예외가 발생한다")
        void createStoreHours_fail_alreadyExists() {
            createMockStoreWithRepository(storeRepository, storeId);
            given(storeHoursRepository.existsByStoreId(storeId)).willReturn(true);

            assertThatThrownBy(
                            () ->
                                    storeHoursService.createStoreHours(
                                            storeId, managerUserDetails, createDefaultRequests()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.STORE_HOURS_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("성공: 7일치 영업시간이 저장된다")
        void createStoreHours_success() {
            createMockStoreWithRepository(storeRepository, storeId);
            given(storeHoursRepository.existsByStoreId(storeId)).willReturn(false);

            storeHoursService.createStoreHours(
                    storeId, managerUserDetails, createDefaultRequests());

            verify(storeHoursRepository).saveAll(argThat(list -> ((List<?>) list).size() == 7));
        }
    }

    @Nested
    @DisplayName("영업시간 조회")
    class GetStoreHours {

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void getStoreHours_fail_storeNotFound() {
            given(storeRepository.existsByIdAndIsDeletedIsFalse(storeId)).willReturn(false);

            assertThatThrownBy(() -> storeHoursService.getStoreHours(storeId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("성공: 등록된 영업시간이 없으면 빈 목록을 반환한다")
        void getStoreHours_success_empty() {
            given(storeRepository.existsByIdAndIsDeletedIsFalse(storeId)).willReturn(true);
            given(storeHoursRepository.findAllByStoreId(storeId)).willReturn(List.of());

            StoreHoursResponse.StoreHoursInfo result = storeHoursService.getStoreHours(storeId);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getStoreHours()).isEmpty();
        }

        @Test
        @DisplayName("성공: 등록된 7일치 영업시간을 반환한다")
        void getStoreHours_success() {
            List<StoreHours> mockHoursList = createMockStoreHoursList();
            given(storeRepository.existsByIdAndIsDeletedIsFalse(storeId)).willReturn(true);
            given(storeHoursRepository.findAllByStoreId(storeId)).willReturn(mockHoursList);

            StoreHoursResponse.StoreHoursInfo result = storeHoursService.getStoreHours(storeId);

            assertThat(result.getStoreId()).isEqualTo(storeId);
            assertThat(result.getStoreHours()).hasSize(7);
        }
    }

    @Nested
    @DisplayName("영업시간 수정")
    class UpdateStoreHours {

        @Test
        @DisplayName("실패: 7일 미만으로 요청하면 예외가 발생한다")
        void updateStoreHours_fail_insufficientDays() {
            assertThatThrownBy(
                            () ->
                                    storeHoursService.updateStoreHours(
                                            storeId,
                                            managerUserDetails,
                                            createRequestsWithInsufficientDays()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void updateStoreHours_fail_storeNotFound() {
            given(storeRepository.findByIdAndIsDeletedIsFalse(storeId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(
                            () ->
                                    storeHoursService.updateStoreHours(
                                            storeId, managerUserDetails, createDefaultRequests()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void updateStoreHours_fail_forbidden() {
            Store store = createMockStoreWithRepository(storeRepository, storeId);
            given(store.getUserId()).willReturn(UUID.randomUUID());

            assertThatThrownBy(
                            () ->
                                    storeHoursService.updateStoreHours(
                                            storeId, ownerUserDetails, createDefaultRequests()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패: 등록된 영업시간이 없으면 예외가 발생한다")
        void updateStoreHours_fail_notFound() {
            createMockStoreWithRepository(storeRepository, storeId);
            given(storeHoursRepository.existsByStoreId(storeId)).willReturn(false);

            assertThatThrownBy(
                            () ->
                                    storeHoursService.updateStoreHours(
                                            storeId, managerUserDetails, createDefaultRequests()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_HOURS_NOT_FOUND);
        }

        @Test
        @DisplayName("성공: 기존 영업시간을 삭제하고 새로운 7일치를 저장한다")
        void updateStoreHours_success() {
            createMockStoreWithRepository(storeRepository, storeId);
            given(storeHoursRepository.existsByStoreId(storeId)).willReturn(true);

            storeHoursService.updateStoreHours(
                    storeId, managerUserDetails, createDefaultRequests());

            verify(storeHoursRepository).deleteAllByStoreId(storeId);
            verify(storeHoursRepository).saveAll(argThat(list -> ((List<?>) list).size() == 7));
        }
    }

    @Nested
    @DisplayName("영업시간 삭제")
    class DeleteStoreHours {

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void deleteStoreHours_fail_storeNotFound() {
            given(storeRepository.findByIdAndIsDeletedIsFalse(storeId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(
                            () -> storeHoursService.deleteStoreHours(storeId, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void deleteStoreHours_fail_forbidden() {
            Store store = createMockStoreWithRepository(storeRepository, storeId);
            given(store.getUserId()).willReturn(UUID.randomUUID());

            assertThatThrownBy(() -> storeHoursService.deleteStoreHours(storeId, ownerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_FORBIDDEN);
        }

        @Test
        @DisplayName("성공: 가게의 모든 영업시간을 삭제한다")
        void deleteStoreHours_success() {
            createMockStoreWithRepository(storeRepository, storeId);

            storeHoursService.deleteStoreHours(storeId, managerUserDetails);

            verify(storeHoursRepository).deleteAllByStoreId(storeId);
        }
    }
}
