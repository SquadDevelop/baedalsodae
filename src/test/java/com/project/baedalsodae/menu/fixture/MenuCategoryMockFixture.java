package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.store.entity.Store;
import java.util.Optional;
import java.util.UUID;

public class MenuCategoryMockFixture {

  private MenuCategoryMockFixture() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  public static CategoryAndStoreFixture createCategoryAndStoreFixture(
      MenuCategoryRepository menuCategoryRepository, UUID menuCategoryId, UUID storeId) {
    MenuCategory category = mock(MenuCategory.class);
    Store store = mock(Store.class);
    given(menuCategoryRepository.findByIdAndDeletedIsFalse(menuCategoryId))
        .willReturn(Optional.of(category));
    lenient().when(category.getStore()).thenReturn(store);
    lenient().when(store.getId()).thenReturn(storeId);
    lenient().when(category.getId()).thenReturn(menuCategoryId);
    lenient().when(category.getName()).thenReturn(DEFAULT_CATEGORY_NAME);
    return new CategoryAndStoreFixture(category, store);
  }

  public static MenuCategory createMockCategory(UUID categoryId, String categoryName) {
    MenuCategory category = mock(MenuCategory.class);
    lenient().when(category.getId()).thenReturn(categoryId);
    lenient().when(category.getName()).thenReturn(categoryName);
    return category;
  }

  public static MenuCategory createMockCategory(UUID categoryId) {
    return createMockCategory(categoryId, DEFAULT_CATEGORY_NAME);
  }

  public record CategoryAndStoreFixture(MenuCategory category, Store store) {}
}
