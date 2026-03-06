package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.util.UUID;

public class MenuItemMockFixture {

    private MenuItemMockFixture() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static MenuItem createFullyMockedMenuItem(
            UUID id,
            String name,
            String description,
            int price,
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

    public static MenuItem createMockMenuItem(
            UUID id,
            String name,
            String description,
            int price,
            int orderNo,
            boolean isPopular,
            MenuStatus menuStatus,
            MenuCategory category) {
        return createFullyMockedMenuItem(
                id, name, description, price, orderNo, isPopular, menuStatus, category);
    }
}
