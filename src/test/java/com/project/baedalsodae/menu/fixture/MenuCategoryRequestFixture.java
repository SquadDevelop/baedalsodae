package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;

import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;

public class MenuCategoryRequestFixture {

    private MenuCategoryRequestFixture() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static MenuCategoryPostRequestDto createDefaultPostRequest() {
        return new MenuCategoryPostRequestDto(DEFAULT_CATEGORY_NAME);
    }

    public static MenuCategoryPostRequestDto createPostRequestWithName(String name) {
        return new MenuCategoryPostRequestDto(name);
    }
}
