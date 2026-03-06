package com.project.baedalsodae.menu.service;

import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.*;
import static com.project.baedalsodae.menu.fixture.MenuCategoryRequestFixture.*;
import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.service.impl.MenuCategoryServiceImpl;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class MenuCategoryServiceImplTest {

    @Mock private MenuCategoryRepository menuCategoryRepository;
    @Mock private StoreRepository storeRepository;

    @InjectMocks private MenuCategoryServiceImpl menuCategoryService;

    private UUID storeId;
    private UUID menuCategoryId;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        menuCategoryId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("메뉴 카테고리 생성")
    class CreateMenuCategory {

        private final MenuCategoryPostRequestDto request = createDefaultPostRequest();

        @Test
        @DisplayName("실패: 가게가 존재하지 않으면 예외가 발생한다")
        void createMenuCategory_fail_storeNotFound() {
            // given
            given(storeRepository.findByIdAndIsDeletedIsFalse(storeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuCategoryService.createMenuCategory(storeId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 같은 가게에 동일한 이름의 카테고리가 있으면 예외가 발생한다")
        void createMenuCategory_fail_duplicateName() {
            // given
            createMockStoreWithRepository(storeRepository, storeId);
            given(
                            menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_CATEGORY_NAME))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> menuCategoryService.createMenuCategory(storeId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.DUPLICATE_MENU_CATEGORY_NAME);
        }

        @Test
        @DisplayName("실패: 순서 저장 중 충돌이 발생하면 예외가 발생한다")
        void createMenuCategory_fail_orderConflict() {
            // given
            createMockStoreWithRepository(storeRepository, storeId);
            given(
                            menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_CATEGORY_NAME))
                    .willReturn(false);
            given(menuCategoryRepository.findMaxOrderNoByStoreIdAndDeletedIsFalse(storeId))
                    .willReturn(Optional.of(0));
            given(menuCategoryRepository.save(any(MenuCategory.class)))
                    .willThrow(new DataIntegrityViolationException("order conflict"));

            // when & then
            assertThatThrownBy(() -> menuCategoryService.createMenuCategory(storeId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.MENU_CATEGORY_ORDER_CONFLICT);
        }

        @Test
        @DisplayName("성공: 가게에 카테고리가 없으면 orderNo는 1이 된다")
        void createMenuCategory_success_firstCategory() {
            // given
            createMockStoreWithRepository(storeRepository, storeId);
            given(
                            menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_CATEGORY_NAME))
                    .willReturn(false);
            given(menuCategoryRepository.findMaxOrderNoByStoreIdAndDeletedIsFalse(storeId))
                    .willReturn(Optional.empty());
            given(menuCategoryRepository.save(any(MenuCategory.class)))
                    .willAnswer(i -> i.getArgument(0));

            // when
            MenuCategoryResponseDto result =
                    menuCategoryService.createMenuCategory(storeId, request);

            // then
            assertThat(result.orderNo()).isEqualTo(FIRST_ORDER_NUMBER);
        }

        @Test
        @DisplayName("성공: 카테고리 생성 시 orderNo는 기존 최대값 + 1이 된다")
        void createMenuCategory_success() {
            // given
            createMockStoreWithRepository(storeRepository, storeId);
            given(
                            menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_CATEGORY_NAME))
                    .willReturn(false);
            given(menuCategoryRepository.findMaxOrderNoByStoreIdAndDeletedIsFalse(storeId))
                    .willReturn(Optional.of(EXISTING_MAX_ORDER_NUMBER));

            given(menuCategoryRepository.save(any(MenuCategory.class)))
                    .willAnswer(i -> i.getArgument(0));

            // when
            MenuCategoryResponseDto result =
                    menuCategoryService.createMenuCategory(storeId, request);

            // then
            assertThat(result.name()).isEqualTo(DEFAULT_CATEGORY_NAME);
            assertThat(result.orderNo()).isEqualTo(EXISTING_MAX_ORDER_NUMBER + 1);
        }
    }

    @Nested
    @DisplayName("메뉴 카테고리 전체 수정")
    class UpdateMenuCategory {

        @Test
        @DisplayName("실패: 카테고리가 존재하지 않으면 예외가 발생한다")
        void updateMenuCategory_fail_notFound() {
            // given
            MenuCategoryPutRequestDto request = createPutRequestWithName(NEW_CATEGORY_NAME);
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(menuCategoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () -> menuCategoryService.updateMenuCategory(menuCategoryId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.MENU_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 변경하려는 이름이 이미 존재하면 예외가 발생한다")
        void updateMenuCategory_fail_duplicateName() {
            // given
            MenuCategoryPutRequestDto request = createPutRequestWithName(DUPLICATE_CATEGORY_NAME);
            createMockCategoryWithRepository(menuCategoryRepository, menuCategoryId, storeId);
            given(
                            menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DUPLICATE_CATEGORY_NAME))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(
                            () -> menuCategoryService.updateMenuCategory(menuCategoryId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.DUPLICATE_MENU_CATEGORY_NAME);
        }

        @Test
        @DisplayName("성공: 이름이 같으면 중복 체크 없이 수정된다")
        void updateMenuCategory_success_sameName() {
            // given
            MenuCategoryPutRequestDto request = createDefaultPutRequest();
            MenuCategory category =
                    createMockCategoryWithRepository(
                            menuCategoryRepository, menuCategoryId, storeId);
            given(category.getOrderNo()).willReturn(FIRST_ORDER_NUMBER);

            // when
            menuCategoryService.updateMenuCategory(menuCategoryId, request);

            // then
            verify(menuCategoryRepository, never())
                    .existsByStoreIdAndNameAndDeletedIsFalse(any(), any());
        }

        @Test
        @DisplayName("성공: 카테고리 이름을 변경한다")
        void updateMenuCategory_success() {
            // given
            MenuCategoryPutRequestDto request = createPutRequestWithName(NEW_CATEGORY_NAME);
            MenuCategory category =
                    createMockCategoryWithRepository(
                            menuCategoryRepository, menuCategoryId, storeId);
            given(
                            menuCategoryRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, NEW_CATEGORY_NAME))
                    .willReturn(false);
            given(category.getOrderNo()).willReturn(FIRST_ORDER_NUMBER);

            // when
            menuCategoryService.updateMenuCategory(menuCategoryId, request);

            // then
            verify(category).changeMenuCategoryName(NEW_CATEGORY_NAME);
        }
    }

}
