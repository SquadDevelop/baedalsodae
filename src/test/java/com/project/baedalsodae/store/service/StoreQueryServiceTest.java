package com.project.baedalsodae.store.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import com.project.baedalsodae.menu.repository.custom.MenuCategoryCustomRepository;
import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.OwnerStoreResponse;
import com.project.baedalsodae.store.dto.response.StoreDetailResponse;
import com.project.baedalsodae.store.dto.response.StorePageResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.repository.custom.StoreCustomRepository;
import com.project.baedalsodae.store.service.impl.StoreQueryServiceImpl;
import com.project.baedalsodae.user.entity.UserRole;
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
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreQueryServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private StoreCategoryRepository storeCategoryRepository;
    @Mock private StoreCustomRepository storeCustomRepository;
    @Mock private MenuCategoryCustomRepository menuCategoryCustomRepository;

    @InjectMocks private StoreQueryServiceImpl storeQueryService;

    private UUID userId;
    private UUID storeId;
    private UUID storeCategoryId;
    private StoreCategory storeCategory;
    private Store store;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        storeCategoryId = UUID.randomUUID();

        storeCategory = StoreCategory.createStoreCategory("치킨", "짱짱 맛있음");

        Address address = Address.createAddress("11", "서울", "110", "강남", "1101", "역삼", "도로명", "상세");
        store =
                Store.createStore(
                        userId,
                        storeCategory,
                        "감자네 치킨",
                        "123-45-67890",
                        "02-1234-5678",
                        address,
                        "맛있는 치킨집");

        // ReflectionTestUtils 사용
        ReflectionTestUtils.setField(store, "id", storeId);
        ReflectionTestUtils.setField(storeCategory, "id", storeCategoryId);
    }

    @Nested
    @DisplayName("가게 상세 조회")
    class GetStoreDetail {
        @Test
        @DisplayName("성공: 가게 기본 정보와 메뉴 카테고리 목록을 함께 반환한다.")
        void getStoreDetail_success() {
            // given
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            List<MenuCategoryItemsResponse> mockMenuList =
                    List.of(mock(MenuCategoryItemsResponse.class));
            given(menuCategoryCustomRepository.getStoreCategoryItems(storeId))
                    .willReturn(mockMenuList);

            // when
            StoreDetailResponse response = storeQueryService.getStoreDetail(storeId);

            // then
            assertThat(response).isNotNull();
            verify(storeRepository).findById(storeId);
            verify(menuCategoryCustomRepository).getStoreCategoryItems(storeId);
        }
    }

    @Nested
    @DisplayName("가게 조회 - 가게 주인")
    class GetStoreForOwner {
        @Test
        @DisplayName("성공: 사장님이 소유한 가게 정보를 조회한다.")
        void getStoreForOwner_success() {
            // given
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            UserRole role = UserRole.OWNER;

            // when
            OwnerStoreResponse response = storeQueryService.getOwnerStore(storeId, userId, role);

            // then
            assertThat(response).isNotNull();
            verify(storeRepository).findById(storeId);
        }

        @Test
        @DisplayName("실패: 유저 ID가 다르면 예외가 발생한다.")
        void getStoreForOwner_fail_forbiddenUser() {
            // given
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            UUID otherUserId = UUID.randomUUID();
            UserRole role = UserRole.OWNER;

            // when & then
            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> storeQueryService.getOwnerStore(storeId, otherUserId, role));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패: OWNER 역할이 아니면 예외가 발생한다.")
        void getStoreForOwner_fail_forbiddenRole() {
            // given
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            UserRole role = UserRole.MANAGER;

            // when & then
            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> storeQueryService.getOwnerStore(storeId, userId, role));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("가게 목록 조회 (Pagination)")
    class GetStorePage {
        @Test
        @DisplayName("성공: 카테고리별 가게 목록을 슬라이스 형태로 반환한다.")
        void getStorePage_success() {
            // given
            StoreCursorRequest cursorRequest = mock(StoreCursorRequest.class);
            SortType sortType = SortType.LATEST;

            given(storeCategoryRepository.findById(storeCategoryId))
                    .willReturn(Optional.of(storeCategory));

            Slice<Store> mockSlice = new SliceImpl<>(List.of(store));
            given(
                            storeCustomRepository.findStoresByCursor(
                                    eq(storeCategoryId), any(), eq(sortType)))
                    .willReturn(mockSlice);

            // when
            StorePageResponse response =
                    storeQueryService.getStorePage(storeCategoryId, cursorRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStoreCategoryId()).isEqualTo(storeCategoryId);
            verify(cursorRequest).initCursor(sortType);
            verify(storeCustomRepository)
                    .findStoresByCursor(eq(storeCategoryId), any(), eq(sortType));
        }
    }
}
