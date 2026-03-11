package com.project.baedalsodae.menu.service;

import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.CategoryAndStoreFixture;
import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.createCategoryAndStoreFixture;
import static com.project.baedalsodae.menu.fixture.MenuCategoryMockFixture.createMockCategory;
import static com.project.baedalsodae.menu.fixture.MenuItemMockFixture.createMockItemWithUnrelatedStore;
import static com.project.baedalsodae.menu.fixture.MenuItemMockFixture.createMockMenuItem;
import static com.project.baedalsodae.menu.fixture.MenuItemRequestFixture.*;
import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
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
import com.project.baedalsodae.recommendation.service.MenuEmbeddingService;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.tag.service.TagMappingService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @Mock private MenuItemRepository menuItemRepository;
    @Mock private MenuCategoryRepository menuCategoryRepository;
    @Mock private TagMappingService tagMappingService;
    @Mock private MenuEmbeddingService menuEmbeddingService;

    @InjectMocks private MenuItemServiceImpl menuItemService;

    private UUID menuCategoryId;
    private UUID menuItemId;
    private UUID storeId;
    private UUID ownerId;
    private UserDetailsImpl managerUserDetails;
    private UserDetailsImpl ownerUserDetails;

    @BeforeEach
    void setUp() {
        menuCategoryId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        managerUserDetails = createManagerUserDetails();
        ownerUserDetails = createOwnerUserDetails(ownerId);
    }

    private void givenCategoryAndStoreExist() {
        createCategoryAndStoreFixture(menuCategoryRepository, menuCategoryId, storeId);
    }

    private void givenMenuItemFields(MenuItem item, int orderNo) {
        given(item.getId()).willReturn(menuItemId);
        given(item.getName()).willReturn(DEFAULT_MENU_ITEM_NAME);
        given(item.getDescription()).willReturn(DEFAULT_ITEM_DESCRIPTION);
        given(item.getPrice()).willReturn(DEFAULT_ITEM_PRICE);
        given(item.getOrderNo()).willReturn(orderNo);
        given(item.isPopular()).willReturn(false);
        given(item.getMenuStatus()).willReturn(MenuStatus.AVAILABLE);
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
            given(menuCategoryRepository.findByIdAndDeletedIsFalseWithLock(menuCategoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.createMenuItem(
                                            menuCategoryId, request, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.MENU_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void createMenuItem_fail_forbidden() {
            // given
            MenuCategory category = mock(MenuCategory.class);
            Store store = mock(Store.class);
            given(menuCategoryRepository.findByIdAndDeletedIsFalseWithLock(menuCategoryId))
                    .willReturn(Optional.of(category));
            given(category.getStore()).willReturn(store);
            given(store.getUserId()).willReturn(UUID.randomUUID());

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.createMenuItem(
                                            menuCategoryId, request, ownerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_FORBIDDEN);
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
            assertThatThrownBy(
                            () ->
                                    menuItemService.createMenuItem(
                                            menuCategoryId, request, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.DUPLICATE_MENU_ITEM_NAME);
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
            MenuItemResponseDto result =
                    menuItemService.createMenuItem(menuCategoryId, request, managerUserDetails);

            // then
            assertThat(result.orderNo()).isEqualTo(FIRST_ORDER_NUMBER);
            assertThat(result.category().id()).isEqualTo(menuCategoryId);
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
            MenuItemResponseDto result =
                    menuItemService.createMenuItem(menuCategoryId, request, managerUserDetails);

            // then
            assertThat(result.name()).isEqualTo(DEFAULT_MENU_ITEM_NAME);
            assertThat(result.orderNo()).isEqualTo(EXISTING_MAX_ORDER_NUMBER + 1);
            assertThat(result.menuStatus()).isEqualTo(MenuStatus.AVAILABLE);
            assertThat(result.category().id()).isEqualTo(menuCategoryId);
            assertThat(result.category().name()).isEqualTo(DEFAULT_CATEGORY_NAME);
            verify(menuItemRepository).save(any(MenuItem.class));
            verify(tagMappingService)
                    .createTagMappings(any(MenuItem.class), eq(List.of(TAG_1, TAG_2)));
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
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(newCategoryId))
                    .willReturn(Optional.of(newCategory));
            given(newCategory.getStore()).willReturn(newStore);
        }

        @Test
        @DisplayName("실패: 메뉴 아이템이 없으면 예외가 발생한다")
        void updateMenuItem_fail_notFound() {
            // given
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItem(
                                            menuItemId, request, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void updateMenuItem_fail_forbidden() {
            // given
            item = createMockItemWithUnrelatedStore(menuItemRepository, menuItemId);

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItem(
                                            menuItemId, request, ownerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_FORBIDDEN);
        }

        @Test
        @DisplayName("실패: 카테고리가 없으면 예외가 발생한다")
        void updateMenuItem_fail_categoryNotFound() {
            // given
            UUID newCategoryId = UUID.randomUUID();
            MenuItemPutRequestDto testRequest = aPutRequest().withCategoryId(newCategoryId).build();
            item = mock(MenuItem.class);
            MenuCategory currentCategory = mock(MenuCategory.class);
            Store currentStore = mock(Store.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(item.getMenuCategory()).willReturn(currentCategory);
            given(currentCategory.getStore()).willReturn(currentStore);
            given(menuCategoryRepository.findByIdAndDeletedIsFalse(newCategoryId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItem(
                                            menuItemId, testRequest, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.MENU_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 변경하려는 이름이 같은 가게에 이미 존재하면 예외가 발생한다")
        void updateMenuItem_fail_duplicateName() {
            // given
            UUID newCategoryId = request.categoryId();
            UUID newStoreId = UUID.randomUUID();
            givenItemAndNewCategoryExist(newCategoryId);
            given(item.getMenuCategory()).willReturn(newCategory);
            given(item.getName()).willReturn(DEFAULT_MENU_ITEM_NAME);
            given(newCategory.getStore().getId()).willReturn(newStoreId);
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    newStoreId, ALTERNATIVE_MENU_ITEM_NAME))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItem(
                                            menuItemId, request, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.DUPLICATE_MENU_ITEM_NAME);
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
                            .withPrice(BigDecimal.valueOf(20000))
                            .withIsPopular(false)
                            .withCategoryId(newCategoryId)
                            .withMenuStatus(MenuStatus.SOLD_OUT)
                            .withTagNames(List.of())
                            .build();
            givenItemAndNewCategoryExist(newCategoryId);
            given(item.getMenuCategory()).willReturn(newCategory);
            given(newCategory.getId()).willReturn(newCategoryId);
            given(newCategory.getName()).willReturn(DEFAULT_CATEGORY_NAME);
            givenMenuItemFields(item, FIRST_ORDER_NUMBER);

            // when
            menuItemService.updateMenuItem(menuItemId, sameNameRequest, managerUserDetails);

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
            given(item.getMenuCategory()).willReturn(newCategory);
            given(newCategory.getStore().getId()).willReturn(newStoreId);
            given(
                            menuItemRepository.existsByStoreIdAndNameAndDeletedIsFalse(
                                    newStoreId, ALTERNATIVE_MENU_ITEM_NAME))
                    .willReturn(false);
            given(newCategory.getId()).willReturn(newCategoryId);
            given(newCategory.getName()).willReturn(DEFAULT_CATEGORY_NAME);
            givenMenuItemFields(item, FIRST_ORDER_NUMBER);

            // when
            menuItemService.updateMenuItem(menuItemId, request, managerUserDetails);

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
            verify(tagMappingService).createTagMappings(item, List.of(TAG_1));
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
            assertThatThrownBy(
                            () ->
                                    menuItemService.patchMenuItem(
                                            menuItemId, request, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void patchMenuItem_fail_forbidden() {
            // given
            MenuItemPatchRequestDto request = createPatchRequestWithName("새이름");
            item = createMockItemWithUnrelatedStore(menuItemRepository, menuItemId);

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.patchMenuItem(
                                            menuItemId, request, ownerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_FORBIDDEN);
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
            assertThatThrownBy(
                            () ->
                                    menuItemService.patchMenuItem(
                                            menuItemId, request, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.DUPLICATE_MENU_ITEM_NAME);
        }

        @Test
        @DisplayName("성공: null 필드는 변경하지 않는다")
        void patchMenuItem_success_nullFieldsSkipped() {
            // given
            MenuItemPatchRequestDto request = createEmptyPatchRequest();
            givenItemAndCategoryExist();
            givenMenuItemFields(item, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request, managerUserDetails);

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
            givenMenuItemFields(item, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request, managerUserDetails);

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
            givenMenuItemFields(item, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request, managerUserDetails);

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
            givenMenuItemFields(item, FIRST_ORDER_NUMBER);

            // when
            menuItemService.patchMenuItem(menuItemId, request, managerUserDetails);

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
            Store store = mock(Store.class);
            given(category.getId()).willReturn(menuCategoryId);
            given(category.getStore()).willReturn(store);

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
            assertThatThrownBy(() -> menuItemService.deleteMenuItem(menuItemId, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void deleteMenuItem_fail_forbidden() {
            // given
            createMockItemWithUnrelatedStore(menuItemRepository, menuItemId);

            // when & then
            assertThatThrownBy(() -> menuItemService.deleteMenuItem(menuItemId, ownerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_FORBIDDEN);
        }

        @Test
        @DisplayName("성공: 메뉴 아이템을 소프트 삭제하고 뒤의 순서를 앞으로 당긴다")
        void deleteMenuItem_success() {
            // given
            givenItemsForDeletion();

            // when
            menuItemService.deleteMenuItem(menuItemId, managerUserDetails);

            // then
            verify(targetItem).softDelete(managerUserDetails.getUserId());
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
            Store store = mock(Store.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(item.getMenuCategory()).willReturn(category);
            given(item.getValidOrderNo()).willReturn(orderNo);
            given(category.getId()).willReturn(menuCategoryId);
            given(category.getName()).willReturn(DEFAULT_CATEGORY_NAME);
            given(category.getStore()).willReturn(store);
            givenMenuItemFields(item, orderNo);
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
                                            menuItemId, THIRD_ORDER_NUMBER, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: OWNER가 본인 가게가 아니면 예외가 발생한다")
        void updateMenuItemOrder_fail_forbidden() {
            // given
            item = createMockItemWithUnrelatedStore(menuItemRepository, menuItemId);

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItemOrder(
                                            menuItemId, THIRD_ORDER_NUMBER, ownerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(ERROR_CODE_FIELD, ErrorCode.MENU_ITEM_FORBIDDEN);
        }

        @Test
        @DisplayName("실패: orderNo가 null이면 예외가 발생한다")
        void updateMenuItemOrder_fail_invalidOrder() {
            // given
            item = mock(MenuItem.class);
            MenuCategory category = mock(MenuCategory.class);
            Store store = mock(Store.class);
            given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                    .willReturn(Optional.of(item));
            given(item.getMenuCategory()).willReturn(category);
            given(category.getStore()).willReturn(store);
            given(item.getValidOrderNo())
                    .willThrow(new BusinessException(ErrorCode.INVALID_MENU_ITEM_ORDER));

            // when & then
            assertThatThrownBy(
                            () ->
                                    menuItemService.updateMenuItemOrder(
                                            menuItemId, THIRD_ORDER_NUMBER, managerUserDetails))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue(
                            ERROR_CODE_FIELD, ErrorCode.INVALID_MENU_ITEM_ORDER);
        }

        @Test
        @DisplayName("성공: 현재 순서와 동일하면 목록 조회 없이 바로 반환한다")
        void updateMenuItemOrder_success_sameOrder() {
            // given
            givenItemExists(SECOND_ORDER_NUMBER);

            // when
            menuItemService.updateMenuItemOrder(
                    menuItemId, SECOND_ORDER_NUMBER, managerUserDetails);

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
            menuItemService.updateMenuItemOrder(menuItemId, THIRD_ORDER_NUMBER, managerUserDetails);

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
            List<MenuItemResponseDto> result = menuItemService.getMenuItem(menuCategoryId, null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: 카테고리에 속한 메뉴 아이템 목록을 orderNo 순으로 반환하고 태그를 포함한다")
        void getMenuItem_success() {
            // given
            MenuCategory category = createMockCategory(menuCategoryId, DEFAULT_CATEGORY_NAME);
            UUID item1Id = UUID.randomUUID();
            UUID item2Id = UUID.randomUUID();
            MenuItem item1 =
                    createMockMenuItem(
                            item1Id,
                            DEFAULT_MENU_ITEM_NAME,
                            DEFAULT_ITEM_DESCRIPTION,
                            DEFAULT_ITEM_PRICE,
                            FIRST_ORDER_NUMBER,
                            true,
                            MenuStatus.AVAILABLE,
                            category);
            MenuItem item2 =
                    createMockMenuItem(
                            item2Id,
                            ALTERNATIVE_MENU_ITEM_NAME,
                            "달콤",
                            ALTERNATIVE_ITEM_PRICE,
                            SECOND_ORDER_NUMBER,
                            false,
                            MenuStatus.AVAILABLE,
                            category);
            given(menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId))
                    .willReturn(List.of(item1, item2));
            given(tagMappingService.getTagNamesByMenuItemIds(any()))
                    .willReturn(Map.of(item1Id, List.of(TAG_1, TAG_2)));

            // when
            List<MenuItemResponseDto> result =
                    menuItemService.getMenuItem(menuCategoryId, managerUserDetails);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo(DEFAULT_MENU_ITEM_NAME);
            assertThat(result.get(0).orderNo()).isEqualTo(FIRST_ORDER_NUMBER);
            assertThat(result.get(0).menuStatus()).isEqualTo(MenuStatus.AVAILABLE);
            assertThat(result.get(0).category().id()).isEqualTo(menuCategoryId);
            assertThat(result.get(0).tagNames()).containsExactly(TAG_1, TAG_2);
            assertThat(result.get(1).name()).isEqualTo(ALTERNATIVE_MENU_ITEM_NAME);
            assertThat(result.get(1).orderNo()).isEqualTo(SECOND_ORDER_NUMBER);
            assertThat(result.get(1).tagNames()).isEmpty();
        }

        @Test
        @DisplayName("성공: 비인증 사용자에게는 AVAILABLE, SOLD_OUT 상태만 반환된다")
        void getMenuItem_success_filtersByStatusForGuest() {
            // given
            MenuCategory category = createMockCategory(menuCategoryId, DEFAULT_CATEGORY_NAME);
            MenuItem available =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            DEFAULT_MENU_ITEM_NAME,
                            DEFAULT_ITEM_DESCRIPTION,
                            DEFAULT_ITEM_PRICE,
                            FIRST_ORDER_NUMBER,
                            false,
                            MenuStatus.AVAILABLE,
                            category);
            MenuItem soldOut =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            ALTERNATIVE_MENU_ITEM_NAME,
                            "설명",
                            ALTERNATIVE_ITEM_PRICE,
                            SECOND_ORDER_NUMBER,
                            false,
                            MenuStatus.SOLD_OUT,
                            category);
            MenuItem preparing =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            "준비중메뉴",
                            "설명",
                            DEFAULT_ITEM_PRICE,
                            THIRD_ORDER_NUMBER,
                            false,
                            MenuStatus.PREPARING,
                            category);
            given(menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId))
                    .willReturn(List.of(available, soldOut, preparing));
            given(tagMappingService.getTagNamesByMenuItemIds(any())).willReturn(Map.of());

            // when
            List<MenuItemResponseDto> result = menuItemService.getMenuItem(menuCategoryId, null);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MenuItemResponseDto::menuStatus)
                    .containsExactly(MenuStatus.AVAILABLE, MenuStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("성공: MANAGER는 모든 상태의 메뉴 아이템을 조회할 수 있다")
        void getMenuItem_success_managerSeesAllStatuses() {
            // given
            MenuCategory category = createMockCategory(menuCategoryId, DEFAULT_CATEGORY_NAME);
            MenuItem available =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            DEFAULT_MENU_ITEM_NAME,
                            DEFAULT_ITEM_DESCRIPTION,
                            DEFAULT_ITEM_PRICE,
                            FIRST_ORDER_NUMBER,
                            false,
                            MenuStatus.AVAILABLE,
                            category);
            MenuItem preparing =
                    createMockMenuItem(
                            UUID.randomUUID(),
                            "준비중메뉴",
                            "설명",
                            DEFAULT_ITEM_PRICE,
                            SECOND_ORDER_NUMBER,
                            false,
                            MenuStatus.PREPARING,
                            category);
            given(menuItemRepository.findAllByMenuCategoryIdAndIsDeletedIsFalse(menuCategoryId))
                    .willReturn(List.of(available, preparing));
            given(tagMappingService.getTagNamesByMenuItemIds(any())).willReturn(Map.of());

            // when
            List<MenuItemResponseDto> result =
                    menuItemService.getMenuItem(menuCategoryId, managerUserDetails);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MenuItemResponseDto::menuStatus)
                    .containsExactly(MenuStatus.AVAILABLE, MenuStatus.PREPARING);
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
