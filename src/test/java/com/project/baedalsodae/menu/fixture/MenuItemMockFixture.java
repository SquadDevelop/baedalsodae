package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import com.project.baedalsodae.menu.repository.MenuItemRepository;
import com.project.baedalsodae.store.entity.Store;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class MenuItemMockFixture {

    private MenuItemMockFixture() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static MenuItem createMockItemWithUnrelatedStore(
            MenuItemRepository menuItemRepository, UUID menuItemId) {
        MenuItem item = mock(MenuItem.class);
        MenuCategory category = mock(MenuCategory.class);
        Store store = mock(Store.class);
        given(menuItemRepository.findByIdAndDeletedIsFalse(menuItemId))
                .willReturn(Optional.of(item));
        given(item.getMenuCategory()).willReturn(category);
        given(category.getStore()).willReturn(store);
        given(store.getUserId()).willReturn(UUID.randomUUID());
        return item;
    }

    public static MenuItem createMockMenuItem(
            UUID id,
            String name,
            String description,
            BigDecimal price,
            int orderNo,
            boolean isPopular,
            MenuStatus menuStatus,
            MenuCategory category) {
        MenuItem item = mock(MenuItem.class);
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getDescription()).thenReturn(description);
        lenient().when(item.getPrice()).thenReturn(price);
        lenient().when(item.getOrderNo()).thenReturn(orderNo);
        lenient().when(item.isPopular()).thenReturn(isPopular);
        lenient().when(item.getMenuStatus()).thenReturn(menuStatus);
        lenient().when(item.getMenuCategory()).thenReturn(category);
        return item;
    }
}
