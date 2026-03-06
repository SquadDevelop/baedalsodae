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
      verify(tagMappingService).createTagMappings(any(MenuItem.class), eq(List.of("치킨", "바삭")));
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
      given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId)).willReturn(Optional.of(item));
      given(menuCategoryRepository.findByIdAndDeletedIsFalse(newCategoryId))
          .willReturn(Optional.of(newCategory));
      given(newCategory.getStore()).willReturn(newStore);
      given(newStore.getId()).willReturn(newStoreId);
    }

    @Test
    @DisplayName("실패: 메뉴 아이템이 없으면 예외가 발생한다")
    void updateMenuItem_fail_notFound() {
      // given
      given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId)).willReturn(Optional.empty());

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
      given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId)).willReturn(Optional.of(item));
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
      verify(menuItemRepository, never()).existsByStoreIdAndNameAndDeletedIsFalse(any(), any());
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
}
