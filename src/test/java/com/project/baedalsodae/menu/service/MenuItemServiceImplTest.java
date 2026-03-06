package com.project.baedalsodae.menu.service;

import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.CategoryAndStoreFixture;
import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.createCategoryAndStoreFixture;
import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.createMockCategory;
import static com.project.baedalsodae.menu.fixture.MenuItemMockFixture.createMockMenuItem;
import static com.project.baedalsodae.menu.fixture.MenuItemRequestFixture.*;
import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.requestDto.item.*;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.menu.service.impl.MenuItemServiceImpl;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.tag.service.TagMappingService;
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
class MenuItemServiceImplTest {

    public static final String ERROR_CODE = "errorCode";
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private MenuCategoryRepository menuCategoryRepository;
    @Mock private TagMappingService tagMappingService;

    @InjectMocks private MenuItemServiceImpl menuItemService;

    private UUID menuCategoryId;
    private UUID menuItemId;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        menuCategoryId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    private void givenCategoryAndStoreExist() {
        createCategoryAndStoreFixture(menuCategoryRepository, menuCategoryId, storeId);
    }

    private void givenMenuItemFields(MenuItem item, MenuCategory category, int orderNo) {
        lenient().when(item.getId()).thenReturn(menuItemId);
        lenient().when(item.getName()).thenReturn(DEFAULT_MENU_ITEM_NAME);
        lenient().when(item.getDescription()).thenReturn(DEFAULT_ITEM_DESCRIPTION);
        lenient().when(item.getPrice()).thenReturn(DEFAULT_ITEM_PRICE);
        lenient().when(item.getOrderNo()).thenReturn(orderNo);
        lenient().when(item.isPopular()).thenReturn(false);
        lenient().when(item.getMenuStatus()).thenReturn(MenuStatus.AVAILABLE);
        lenient().when(item.getMenuCategory()).thenReturn(category);
    }

    @Nested
    @DisplayName("메뉴 아이템 생성")
    class CreateMenuItem {

        private MenuItemPostRequestDto request;
        private MenuCategory category;
        private Store store;

        @BeforeEach
        void setUp() {
            request = createDefaultPostRequest();
        }

        private void givenCategoryExists() {
            CategoryAndStoreFixture fixture =
                    createCategoryAndStoreFixture(menuCategoryRepository, menuCategoryId, storeId);
            category = fixture.category();
            store = fixture.store();
        }

        @Test
        @DisplayName("실패: 카테고리가 존재하지 않으면 예외가 발생한다")
        void createMenuItem_fail_categoryNotFound() {
            // given
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(menuCategoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuItemService.createMenuItem(menuCategoryId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 같은 가게에 동일한 이름의 메뉴가 있으면 예외가 발생한다")
        void createMenuItem_fail_duplicateName() {
            // given
            givenCategoryAndStoreExist();
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_MENU_ITEM_NAME))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> menuItemService.createMenuItem(menuCategoryId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.DUPLICATE_MENU_ITEM_NAME);
        }

        @Test
        @DisplayName("실패: 순서 저장 중 충돌이 발생하면 예외가 발생한다")
        void createMenuItem_fail_orderConflict() {
            // given
            givenCategoryAndStoreExist();
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_MENU_ITEM_NAME))
                    .willReturn(false);
            given(menuItemRepository.findMaxOrderNoByMenuCategoryId(menuCategoryId))
                    .willReturn(Optional.of(0));
            given(menuItemRepository.save(any(MenuItem.class)))
                    .willThrow(new DataIntegrityViolationException("order conflict"));

            // when & then
            assertThatThrownBy(() -> menuItemService.createMenuItem(menuCategoryId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_ITEM_ORDER_CONFLICT);
        }

        @Test
        @DisplayName("성공: 카테고리에 메뉴가 없으면 orderNo는 1이 된다")
        void createMenuItem_success_firstItem() {
            // given
            givenCategoryExists();
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_MENU_ITEM_NAME))
                    .willReturn(false);
            given(menuItemRepository.findMaxOrderNoByMenuCategoryId(menuCategoryId))
                    .willReturn(Optional.empty());
            given(menuItemRepository.save(any(MenuItem.class))).willAnswer(i -> i.getArgument(0));

            // when
            MenuItemResponseDto result = menuItemService.createMenuItem(menuCategoryId, request);

            // then
            assertThat(result.orderNo()).isEqualTo(FIRST_ORDER_NUMBER);
        }

        @Test
        @DisplayName("성공: 메뉴 아이템을 생성하고 orderNo는 기존 최대값 + 1이 된다")
        void createMenuItem_success() {
            // given
            givenCategoryExists();
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_MENU_ITEM_NAME))
                    .willReturn(false);
            given(menuItemRepository.findMaxOrderNoByMenuCategoryId(menuCategoryId))
                    .willReturn(Optional.of(EXISTING_MAX_ORDER_NUMBER));
            given(menuItemRepository.save(any(MenuItem.class))).willAnswer(i -> i.getArgument(0));

            // when
            MenuItemResponseDto result = menuItemService.createMenuItem(menuCategoryId, request);

            // then
            assertThat(result.name()).isEqualTo(DEFAULT_MENU_ITEM_NAME);
            assertThat(result.orderNo()).isEqualTo(EXISTING_MAX_ORDER_NUMBER + 1);
            assertThat(result.menuStatus()).isEqualTo(MenuStatus.AVAILABLE);
            verify(menuItemRepository).save(any(MenuItem.class));
            verify(tagMappingService)
                    .createTagMappings(any(MenuItem.class), eq(List.of("치킨", "바삭")));
        }
    }

    @Nested
    @DisplayName("메뉴 아이템 전체 수정")
    class UpdateMenuItem {

        private MenuItemPutRequestDto request;
        private MenuItem item;
        private MenuCategory newCategory;

        @BeforeEach
        void setUp() {
            UUID newCategoryId = UUID.randomUUID();
            request = aPutRequest().withCategoryId(newCategoryId).build();
        }

        private void givenItemAndNewCategoryExist(UUID newCategoryId) {
            item = mock(MenuItem.class);
            newCategory = mock(MenuCategory.class);
            Store newStore = mock(Store.class);
            UUID newStoreId = UUID.randomUUID();
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(newCategoryId))
                    .willReturn(Optional.of(newCategory));
            given(newCategory.getStore()).willReturn(newStore);
            given(newStore.getId()).willReturn(newStoreId);
        }

        @Test
        @DisplayName("실패: 메뉴 아이템이 없으면 예외가 발생한다")
        void updateMenuItem_fail_notFound() {
            // given
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuItemService.updateMenuItem(menuItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 카테고리가 없으면 예외가 발생한다")
        void updateMenuItem_fail_categoryNotFound() {
            // given
            UUID newCategoryId = UUID.randomUUID();
            MenuItemPutRequestDto testRequest = aPutRequest().withCategoryId(newCategoryId).build();
            item = mock(MenuItem.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(newCategoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuItemService.updateMenuItem(menuItemId, testRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 변경하려는 이름이 같은 가게에 이미 존재하면 예외가 발생한다")
        void updateMenuItem_fail_duplicateName() {
            // given
            UUID newCategoryId = request.categoryId();
            UUID newStoreId = UUID.randomUUID();
            givenItemAndNewCategoryExist(newCategoryId);
            given(item.getName()).willReturn(DEFAULT_MENU_ITEM_NAME);
            given(newCategory.getStore().getId()).willReturn(newStoreId);
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    newStoreId, ALTERNATIVE_MENU_ITEM_NAME))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> menuItemService.updateMenuItem(menuItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.DUPLICATE_MENU_ITEM_NAME);
        }

        @Test
        @DisplayName("성공: 이름이 동일한 경우 중복 체크 없이 수정된다")
        void updateMenuItem_success_sameName() {
            // given
            UUID newCategoryId = UUID.randomUUID();
            MenuItemPutRequestDto sameNameRequest =
                    aPutRequest()
                            .withName(DEFAULT_MENU_ITEM_NAME)
                            .withDescription("새로운 설명")
                            .withPrice(20000)
                            .withIsPopular(false)
                            .withCategoryId(newCategoryId)
                            .withMenuStatus(MenuStatus.SOLD_OUT)
                            .withTagNames(List.of())
                            .build();
            givenItemAndNewCategoryExist(newCategoryId);
            given(newCategory.getId()).willReturn(newCategoryId);
            given(newCategory.getName()).willReturn(DEFAULT_CATEGORY_NAME);
            givenMenuItemFields(item, newCategory, FIRST_ORDER_NUMBER);

            // when
            menuItemService.updateMenuItem(menuItemId, sameNameRequest);

            // then
            verify(menuItemRepository, never())
                    .existsByStoreIdAndNameAndDeletedIsFalse(any(), any());
        }

        @Test
        @DisplayName("성공: 메뉴 아이템 정보를 전체 수정하고 태그를 갱신한다")
        void updateMenuItem_success() {
            // given
            UUID newCategoryId = request.categoryId();
            UUID newStoreId = UUID.randomUUID();
            givenItemAndNewCategoryExist(newCategoryId);
            given(newCategory.getStore().getId()).willReturn(newStoreId);
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    newStoreId, ALTERNATIVE_MENU_ITEM_NAME))
                    .willReturn(false);
            given(newCategory.getId()).willReturn(newCategoryId);
            given(newCategory.getName()).willReturn(DEFAULT_CATEGORY_NAME);
            givenMenuItemFields(item, newCategory, FIRST_ORDER_NUMBER);

            // when
            menuItemService.updateMenuItem(menuItemId, request);

            // then
            verify(item)
                    .changeMenuInfo(
                            ALTERNATIVE_MENU_ITEM_NAME,
                            ALTERNATIVE_ITEM_DESCRIPTION,
                            ALTERNATIVE_ITEM_PRICE,
                            MenuStatus.AVAILABLE,
                            newCategory,
                            true);
            verify(tagMappingService).deleteAllTagMappingByMenuItemId(menuItemId);
            verify(tagMappingService).createTagMappings(item, List.of("치킨"));
        }
    }

    @Nested
    @DisplayName("메뉴 아이템 부분 수정")
    class PatchMenuItem {

        private MenuItem item;
        private MenuCategory category;

        private void givenItemAndCategoryExist() {
            item = mock(MenuItem.class);
            category = mock(MenuCategory.class);
            Store store = mock(Store.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(item.getMenuCategory()).willReturn(category);
            given(category.getId()).willReturn(menuCategoryId);
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(menuCategoryId))
                    .willReturn(Optional.of(category));
            given(category.getStore()).willReturn(store);
            given(store.getId()).willReturn(storeId);
        }

        @Test
        @DisplayName("실패: 메뉴 아이템이 없으면 예외가 발생한다")
        void patchMenuItem_fail_notFound() {
            // given
            MenuItemPatchRequestDto request = createPatchRequestWithName("새이름");
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuItemService.patchMenuItem(menuItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 변경하려는 이름이 이미 존재하면 예외가 발생한다")
        void patchMenuItem_fail_duplicateName() {
            // given
            MenuItemPatchRequestDto request = createPatchRequestWithName("중복이름");
            givenItemAndCategoryExist();
            given(item.getName()).willReturn(DEFAULT_MENU_ITEM_NAME);
            given(menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(storeId, "중복이름"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> menuItemService.patchMenuItem(menuItemId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.DUPLICATE_MENU_ITEM_NAME);
        }

        @Test
        @DisplayName("성공: null 필드는 변경하지 않는다")
        void patchMenuItem_success_nullFieldsSkipped() {
            // given
            MenuItemPatchRequestDto request = createEmptyPatchRequest();
            givenItemAndCategoryExist();
            givenMenuItemFields(item, category, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request);

            // then
            verify(item, never()).changeName(any());
            verify(item, never()).changeDescription(any());
            verify(item, never()).changePrice(any());
            verify(item, never()).changeIsPopular(any());
            verify(item, never()).changeMenuStatus(any());
            verify(item, never()).changeMenuCategory(any());
            verifyNoInteractions(tagMappingService);
        }

        @Test
        @DisplayName("성공: 태그를 수정하면 기존 태그를 삭제하고 새로 생성한다")
        void patchMenuItem_success_updateTags() {
            // given
            MenuItemPatchRequestDto request = createPatchRequestWithTags(List.of("매운맛", "인기"));
            givenItemAndCategoryExist();
            givenMenuItemFields(item, category, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request);

            // then
            verify(tagMappingService).deleteAllTagMappingByMenuItemId(menuItemId);
            verify(tagMappingService).createTagMappings(item, List.of("매운맛", "인기"));
        }

        @Test
        @DisplayName("성공: 카테고리를 변경한다")
        void patchMenuItem_success_changeCategory() {
            // given
            MenuItemPatchRequestDto request = createPatchRequestWithCategory(UUID.randomUUID());
            MenuCategory newCategory = mock(MenuCategory.class);
            givenItemAndCategoryExist();
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(request.categoryId()))
                    .willReturn(Optional.of(newCategory));
            givenMenuItemFields(item, category, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request);

            // then
            verify(item).changeMenuCategory(newCategory);
        }

        @Test
        @DisplayName("성공: 이름만 변경한다")
        void patchMenuItem_success_changeName() {
            // given
            MenuItemPatchRequestDto request = createPatchRequestWithName("새이름");
            givenItemAndCategoryExist();
            given(menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(storeId, "새이름"))
                    .willReturn(false);
            givenMenuItemFields(item, category, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request);

            // then
            verify(item).changeName("새이름");
        }
    }

    @Nested
    @DisplayName("메뉴 아이템 삭제")
    class DeleteMenuItem {

        private MenuItem item1;
        private MenuItem targetItem;
        private MenuItem item3;

        private void givenItemsForDeletion() {
            MenuCategory category = mock(MenuCategory.class);
            given(category.getId()).willReturn(menuCategoryId);

            item1 = mock(MenuItem.class);
            given(item1.getOrderNo()).willReturn(FIRST_ORDER_NUMBER);

            targetItem = mock(MenuItem.class);
            given(targetItem.getMenuCategory()).willReturn(category);
            given(targetItem.getOrderNo()).willReturn(SECOND_ORDER_NUMBER);

            item3 = mock(MenuItem.class);
            given(item3.getOrderNo()).willReturn(THIRD_ORDER_NUMBER);

            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(targetItem));
            given(
                            menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalseWithLock(
                                    menuCategoryId))
                    .willReturn(List.of(item1, targetItem, item3));
        }

        @Test
        @DisplayName("실패: 메뉴 아이템이 없으면 예외가 발생한다")
        void deleteMenuItem_fail_notFound() {
            // given
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> menuItemService.deleteMenuItem(menuItemId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("성공: 메뉴 아이템을 소프트 삭제하고 뒤의 순서를 앞으로 당긴다")
        void deleteMenuItem_success() {
            // given
            givenItemsForDeletion();

            // when
            menuItemService.deleteMenuItem(menuItemId);

            // then
            verify(targetItem).softDelete(null);
            verify(item3).changeOrderNo(SECOND_ORDER_NUMBER);
            verify(item1, never()).changeOrderNo(any());
        }
    }

    @Nested
    @DisplayName("메뉴 아이템 순서 변경")
    class UpdateMenuItemOrder {

        private MenuItem item;

        private void givenItemExists(int orderNo) {
            item = mock(MenuItem.class);
            MenuCategory category = mock(MenuCategory.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            givenMenuItemFields(item, category, orderNo);
            given(category.getId()).willReturn(menuCategoryId);
            given(category.getName()).willReturn(DEFAULT_CATEGORY_NAME);
        }

        @Test
        @DisplayName("실패: 메뉴 아이템이 없으면 예외가 발생한다")
        void updateMenuItemOrder_fail_notFound() {
            // given
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItemOrder(
                                            menuItemId, THIRD_ORDER_NUMBER))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: orderNo가 null이면 예외가 발생한다")
        void updateMenuItemOrder_fail_invalidOrder() {
            // given
            item = mock(MenuItem.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(item.getOrderNo()).willReturn(null);

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItemOrder(
                                            menuItemId, THIRD_ORDER_NUMBER))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE, ErrorCode.INVALID_MENU_ITEM_ORDER);
        }

        @Test
        @DisplayName("성공: 현재 순서와 동일하면 목록 조회 없이 바로 반환한다")
        void updateMenuItemOrder_success_sameOrder() {
            // given
            givenItemExists(SECOND_ORDER_NUMBER);

            // when
            menuItemService.updateMenuItemOrder(menuItemId, SECOND_ORDER_NUMBER);

            // then
            verify(menuItemRepository, never())
                    .findAllByMenuCategoryIdAndIsDeletedIsFalseWithLock(any());
        }

        @Test
        @DisplayName("성공: 1번에서 3번으로 이동 시 사이 순서가 앞으로 당겨진다")
        void updateMenuItemOrder_success_moveDown() {
            // given
            MenuItem item2 = mock(MenuItem.class);
            MenuItem item3 = mock(MenuItem.class);
            givenItemExists(FIRST_ORDER_NUMBER);
            given(item2.getOrderNo()).willReturn(SECOND_ORDER_NUMBER);
            given(item3.getOrderNo()).willReturn(THIRD_ORDER_NUMBER);
            given(
                            menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalseWithLock(
                                    menuCategoryId))
                    .willReturn(List.of(item, item2, item3));

            // when
            menuItemService.updateMenuItemOrder(menuItemId, THIRD_ORDER_NUMBER);

            // then
            verify(item).changeOrderNo(THIRD_ORDER_NUMBER);
            verify(item2).changeOrderNo(FIRST_ORDER_NUMBER);
            verify(item3).changeOrderNo(SECOND_ORDER_NUMBER);
        }
    }

    @Nested
    @DisplayName("메뉴 아이템 목록 조회")
    class GetMenuItem {

        @Test
        @DisplayName("성공: 카테고리에 메뉴가 없으면 빈 목록을 반환한다")
        void getMenuItem_success_empty() {
            // given
            given(menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId))
                    .willReturn(List.of());

            // when
            List<MenuItemResponseDto> result = menuItemService.getMenuItem(menuCategoryId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: 카테고리에 속한 메뉴 아이템 목록을 orderNo 순으로 반환한다")
        void getMenuItem_success() {
            // given
            MenuCategory category = createMockCategory(menuCategoryId, DEFAULT_CATEGORY_NAME);
            MenuItem item1 =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            DEFAULT_MENU_ITEM_NAME,
                            DEFAULT_ITEM_DESCRIPTION,
                            DEFAULT_ITEM_PRICE,
                            FIRST_ORDER_NUMBER,
                            true,
                            MenuStatus.AVAILABLE,
                            category);
            MenuItem item2 =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            ALTERNATIVE_MENU_ITEM_NAME,
                            "달콤",
                            ALTERNATIVE_ITEM_PRICE,
                            SECOND_ORDER_NUMBER,
                            false,
                            MenuStatus.AVAILABLE,
                            category);
            given(menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId))
                    .willReturn(List.of(item1, item2));

            // when
            List<MenuItemResponseDto> result = menuItemService.getMenuItem(menuCategoryId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo(DEFAULT_MENU_ITEM_NAME);
            assertThat(result.get(1).name()).isEqualTo(ALTERNATIVE_MENU_ITEM_NAME);
        }
    }

    @Nested
    @DisplayName("메뉴 아이템 이름 중복 확인")
    class IsDuplicateMenuItemName {

        @Test
        @DisplayName("중복 이름이 없으면 false를 반환한다")
        void isDuplicateMenuItemName_false() {
            // given
            given(menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(storeId, "신메뉴"))
                    .willReturn(false);

            // when
            boolean result = menuItemService.isDuplicateMenuItemName(storeId, "신메뉴");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("중복 이름이 있으면 true를 반환한다")
        void isDuplicateMenuItemName_true() {
            // given
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    storeId, DEFAULT_MENU_ITEM_NAME))
                    .willReturn(true);

            // when
            boolean result =
                    menuItemService.isDuplicateMenuItemName(storeId, DEFAULT_MENU_ITEM_NAME);

            // then
            assertThat(result).isTrue();
        }
    }
}
