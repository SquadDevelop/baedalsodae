package com.project.baedalsodae.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import com.project.baedalsodae.menu.repository.custom.MenuCategoryCustomRepository;
import com.project.baedalsodae.store.dto.request.store.StoreCursorRequest;
import com.project.baedalsodae.store.dto.response.store.OwnerStoreResponse;
import com.project.baedalsodae.store.dto.response.store.StoreDetailResponse;
import com.project.baedalsodae.store.dto.response.store.StorePageResponse;
import com.project.baedalsodae.store.dto.response.store.StoreSearchPageResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.repository.custom.StoreCustomRepository;
import com.project.baedalsodae.store.service.impl.StoreQueryServiceImpl;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreQueryServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private StoreCategoryRepository storeCategoryRepository;
    @Mock private StoreCustomRepository storeCustomRepository;
    @Mock private MenuCategoryCustomRepository menuCategoryCustomRepository;
    @Mock private AllowedRegionService allowedRegionService;
    @Mock private UserAddressService userAddressService;

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
            UserAddress userAddress = mock(UserAddress.class);
            given(userAddress.getAddress())
                    .willReturn(
                            Address.createAddress(
                                    "11", "서울", "110", "강남", "1101", "역삼", "도로명", "상세"));

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(menuCategoryCustomRepository.getStoreCategoryItems(storeId))
                    .willReturn(List.of(mock(MenuCategoryItemsResponse.class)));
            given(userAddressService.getMainUserAddress(userId)).willReturn(userAddress);
            given(allowedRegionService.isAllowedByCode(anyString())).willReturn(true);

            // when
            StoreDetailResponse response = storeQueryService.getStoreDetail(storeId, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isAllowedRegion()).isTrue();
            assertThat(response.isDeliverableToUser()).isTrue();
        }

        @Test
        @DisplayName("성공: 유저 주소가 배달 불가 지역이면 isDeliverableToUser=false를 반환한다.")
        void getStoreDetail_storeValid() {
            // given
            UserAddress userAddress = mock(UserAddress.class);
            given(userAddress.getAddress())
                    .willReturn(
                            Address.createAddress(
                                    "11", "서울", "110", "강남", "1101", "역삼", "도로명", "상세"));

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(menuCategoryCustomRepository.getStoreCategoryItems(storeId))
                    .willReturn(List.of());
            given(userAddressService.getMainUserAddress(userId)).willReturn(userAddress);
            given(allowedRegionService.isAllowedByCode(anyString()))
                    .willReturn(false)
                    .willReturn(true);

            // when
            StoreDetailResponse response = storeQueryService.getStoreDetail(storeId, userId);

            // then
            assertThat(response.isDeliverableToUser()).isFalse();
        }

        @Test
        @DisplayName("성공: 가게 주소가 배달 불가 지역이면 isAllowedRegion=false를 반환한다.")
        void getStoreDetail_notDeliverable() {
            // given
            UserAddress userAddress = mock(UserAddress.class);
            given(userAddress.getAddress())
                    .willReturn(
                            Address.createAddress(
                                    "11", "서울", "110", "강남", "1101", "역삼", "도로명", "상세"));

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(menuCategoryCustomRepository.getStoreCategoryItems(storeId))
                    .willReturn(List.of());
            given(userAddressService.getMainUserAddress(userId)).willReturn(userAddress);
            given(allowedRegionService.isAllowedByCode(anyString()))
                    .willReturn(false)
                    .willReturn(true);

            // when
            StoreDetailResponse response = storeQueryService.getStoreDetail(storeId, userId);

            // then
            assertThat(response.isAllowedRegion()).isFalse();
        }

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다.")
        void getStoreDetail_storeNotFound() {
            given(storeRepository.findById(storeId)).willReturn(Optional.empty());

            assertThrows(
                    BusinessException.class,
                    () -> storeQueryService.getStoreDetail(storeId, userId));
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
    @DisplayName("가게 목록 조회")
    class GetStorePage {
        @Test
        @DisplayName("성공: 카테고리별 가게 목록을 슬라이스 형태로 반환한다.")
        void getStorePage_success() {
            SortType sortType = SortType.LATEST;
            StoreCursorRequest cursorRequest =
                    new StoreCursorRequest(null, sortType, null, null, null, 10);

            given(storeCategoryRepository.findById(storeCategoryId))
                    .willReturn(Optional.of(storeCategory));

            Slice<Store> mockSlice = new SliceImpl<>(List.of(store));

            // any()를 사용하거나 실제 생성한 객체를 맞춰줍니다.
            given(
                            storeCustomRepository.findStoresByCursor(
                                    eq(storeCategoryId),
                                    any(StoreCursorRequest.class),
                                    eq(sortType)))
                    .willReturn(mockSlice);

            // when
            StorePageResponse response =
                    storeQueryService.getStorePage(storeCategoryId, cursorRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStoreCategoryId()).isEqualTo(storeCategoryId);

            verify(storeCustomRepository)
                    .findStoresByCursor(eq(storeCategoryId), any(), eq(sortType));
        }
    }

    @Nested
    @DisplayName("가게 키워드 검색")
    class getStoreByKeyword {

        @Test
        @DisplayName("성공: 키워드 검색 시 첫 페이지라면 totalCount를 포함한 응답을 반환한다.")
        void getStoreByKeyword_success_firstPage() {
            // given
            String keyword = "치킨";
            SortType sortType = SortType.LATEST;
            Pageable pageable = PageRequest.of(0, 10);

            List<Store> mockContent = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                mockContent.add(store);
            }

            given(
                            storeCustomRepository.searchStoreByKeyword(
                                    eq(keyword), any(Pageable.class), eq(sortType)))
                    .willReturn(mockContent);

            // 첫 페이지(0)이므로 count 쿼리 호출 모킹
            given(storeCustomRepository.countStoresByKeyword(keyword)).willReturn(100L);

            // when
            StoreSearchPageResponse response =
                    storeQueryService.getStoreByKeyword(keyword, pageable, sortType);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStores()).hasSize(10); // 로직에 의해 1개 제거됨
            assertThat(response.getTotalCount()).isEqualTo(100L);
            assertThat(response.getCurrentPage()).isEqualTo(1); // 0 + 1
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getHasPrevious()).isFalse();

            verify(storeCustomRepository).countStoresByKeyword(keyword);
        }

        @Test
        @DisplayName("성공: 두 번째 페이지 이후라면 totalCount는 null이며 hasPrevious는 true이다.")
        void getStoreByKeyword_success_secondPage() {
            // given
            String keyword = "치킨";
            SortType sortType = SortType.LATEST;
            Pageable pageable = PageRequest.of(1, 10); // 두 번째 페이지

            List<Store> mockContent = new ArrayList<>(List.of(store));

            given(
                            storeCustomRepository.searchStoreByKeyword(
                                    eq(keyword), any(Pageable.class), eq(sortType)))
                    .willReturn(mockContent);

            // when
            StoreSearchPageResponse response =
                    storeQueryService.getStoreByKeyword(keyword, pageable, sortType);

            // then
            assertThat(response.getTotalCount()).isNull(); // 두 번째 페이지는 count 쿼리 안 함
            assertThat(response.getCurrentPage()).isEqualTo(2); // 1 + 1
            assertThat(response.getHasPrevious()).isTrue();

            // 첫 페이지가 아니므로 count 쿼리가 호출되지 않았는지 검증
            verify(storeCustomRepository, never()).countStoresByKeyword(anyString());
        }
    }
}
