package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.repository.MenuCategoryRepository;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.repository.StoreRepository;
import java.util.Optional;
import java.util.UUID;

public class MenuCategoryMockFixture {

    private MenuCategoryMockFixture() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static Store createMockStoreWithRepository(
            StoreRepository storeRepository, UUID storeId) {
        Store store = mock(Store.class);
        given(storeRepository.findByIdAndIsDeletedIsFalse(storeId)).willReturn(Optional.of(store));
        return store;
    }

    public static MenuCategory createMockCategoryWithRepository(
            MenuCategoryRepository menuCategoryRepository, UUID menuCategoryId, UUID storeId) {
        MenuCategory category = mock(MenuCategory.class);
        Store store = mock(Store.class);
        given(menuCategoryRepository.findByIdAndDeletedIsFalse(menuCategoryId))
                .willReturn(Optional.of(category));
        lenient().when(category.getStore()).thenReturn(store);
        lenient().when(store.getId()).thenReturn(storeId);
        lenient().when(category.getId()).thenReturn(menuCategoryId);
        given(category.getName()).willReturn(DEFAULT_CATEGORY_NAME);
        return category;
    }

    public static CategoryAndStoreFixture createCategoryAndStoreFixture(
            MenuCategoryRepository menuCategoryRepository, UUID menuCategoryId, UUID storeId) {
        MenuCategory category = mock(MenuCategory.class);
        Store store = mock(Store.class);
        given(menuCategoryRepository.findByIdAndDeletedIsFalseWithLock(menuCategoryId))
                .willReturn(Optional.of(category));
        given(category.getStore()).willReturn(store);
        given(store.getId()).willReturn(storeId);
        lenient().when(category.getId()).thenReturn(menuCategoryId);
        lenient().when(category.getName()).thenReturn(DEFAULT_CATEGORY_NAME);
        return new CategoryAndStoreFixture(category, store);
    }

    public static MenuCategory createMockCategoryWithUnrelatedStore(
            MenuCategoryRepository menuCategoryRepository, UUID menuCategoryId) {
        MenuCategory category = mock(MenuCategory.class);
        Store store = mock(Store.class);
        given(menuCategoryRepository.findByIdAndDeletedIsFalse(menuCategoryId))
                .willReturn(Optional.of(category));
        given(category.getStore()).willReturn(store);
        given(store.getUserId()).willReturn(UUID.randomUUID());
        return category;
    }

    public static MenuCategory createMockCategory(UUID categoryId, String categoryName) {
        MenuCategory category = mock(MenuCategory.class);
        given(category.getId()).willReturn(categoryId);
        given(category.getName()).willReturn(categoryName);
        return category;
    }

    public static MenuCategory createMockCategory(UUID categoryId) {
        return createMockCategory(categoryId, DEFAULT_CATEGORY_NAME);
    }

    public record CategoryAndStoreFixture(MenuCategory category, Store store) {}
}
