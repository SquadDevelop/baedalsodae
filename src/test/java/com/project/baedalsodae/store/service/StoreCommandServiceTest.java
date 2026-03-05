package com.project.baedalsodae.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.dto.AddressRequest;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.store.dto.request.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.service.impl.StoreCommandServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreCommandServiceTest {

    @Mock private StoreRepository storeRepository;

    @Mock private StoreCategoryRepository storeCategoryRepository;

    @InjectMocks private StoreCommandServiceImpl storeCommandService;

    private UUID userId;
    private UUID storeId;
    private UUID categoryId;
    private StoreCategory category;
    private AddressRequest addressRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        category = mock(StoreCategory.class); // 카테고리 객체 모킹

        addressRequest =
                new AddressRequest("11", "서울특별시", "110", "강남구", "11010", "역삼동", "테헤란로 427", "위워크");
    }

    @Nested
    @DisplayName("가게 생성")
    class CreateStore {

        @Test
        @DisplayName("성공: 새로운 가게를 등록한다.")
        void createStore_success() {
            CreateStoreRequest request =
                    new CreateStoreRequest(
                            categoryId,
                            "교촌치킨",
                            "123-45-67890",
                            "02-123-4567",
                            addressRequest,
                            "맛있는 치킨");

            given(storeRepository.existsByBusinessNumber(request.getBusinessNumber()))
                    .willReturn(false);
            given(storeCategoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            storeCommandService.createStore(request, userId);

            verify(storeRepository, times(1)).save(any(Store.class));
        }

        @Test
        @DisplayName("실패: 중복된 사업자 번호가 존재하면 예외가 발생한다.")
        void createStore_fail_duplicateBusinessNumber() {
            CreateStoreRequest request =
                    new CreateStoreRequest(
                            categoryId,
                            "교촌치킨",
                            "123-45-67890",
                            "02-123-4567",
                            addressRequest,
                            "맛있는 치킨");
            given(storeRepository.existsByBusinessNumber(request.getBusinessNumber()))
                    .willReturn(true);

            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> storeCommandService.createStore(request, userId));
            assertEquals(ErrorCode.STORE_DUPLICATED_BUSINESS_NUMBER, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("가게 정보 수정")
    class UpdateStore {

        @Test
        @DisplayName("성공: 본인 소유의 가게 정보를 수정한다.")
        void updateStore_success() {
            UpdateStoreRequest request =
                    new UpdateStoreRequest(
                            categoryId, "BHC치킨", "02-987-6543", addressRequest, "수정된 설명");

            Store store = mock(Store.class);
            given(store.getUserId()).willReturn(userId);

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(storeCategoryRepository.findById(categoryId)).willReturn(Optional.of(category));

            storeCommandService.updateStore(request, storeId, userId);

            verify(store)
                    .updateStore(
                            eq(category),
                            eq("BHC치킨"),
                            eq("02-987-6543"),
                            any(Address.class),
                            eq("수정된 설명"));
        }

        @Test
        @DisplayName("실패: 가게 주인이 아니면 수정할 수 없다.")
        void updateStore_fail_forbidden() {
            UpdateStoreRequest request =
                    new UpdateStoreRequest(categoryId, "BHC", "02-000-0000", addressRequest, "설명");
            Store store = mock(Store.class);
            given(store.getUserId()).willReturn(UUID.randomUUID()); // 다른 유저 ID
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> storeCommandService.updateStore(request, storeId, userId));
            assertEquals(ErrorCode.STORE_FORBIDDEN, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("영업 상태 변경")
    class UpdateStatus {

        @Test
        @DisplayName("성공: 사장님이 영업 상태를 OPEN/CLOSED로 변경한다.")
        void updateStatus_success() {
            // given
            Store store = mock(Store.class);
            given(store.getUserId()).willReturn(userId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // when
            storeCommandService.updateStoreOpened(storeId, StoreStatus.OPEN, userId);

            // then
            verify(store).patchStoreOpened(StoreStatus.OPEN);
        }

        @ParameterizedTest
        @EnumSource(
                value = StoreStatus.class,
                names = {"SUSPENDED", "PENDING_APPROVAL"})
        @DisplayName("실패: 권한이 없는 상태값으로 변경 시 예외 발생")
        void updateStatus_fail_forbiddenStatus(StoreStatus forbiddenStatus) {
            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () ->
                                    storeCommandService.updateStoreOpened(
                                            storeId, forbiddenStatus, userId));
            assertEquals(ErrorCode.STORE_STATUS_CHANGE_FORBIDDEN, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("가게 삭제")
    class DeleteStore {
        @Test
        @DisplayName("성공: 소프트 삭제를 수행한다.")
        void deleteStore_success() {
            Store store = mock(Store.class);
            given(store.getUserId()).willReturn(userId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            storeCommandService.deleteStore(storeId, userId);

            verify(store).softDelete(userId);
        }
    }
}
