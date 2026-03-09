package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;

public class MenuCategoryRequestFixture {

    private MenuCategoryRequestFixture() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static MenuCategoryPostRequestDto createDefaultPostRequest() {
        return new MenuCategoryPostRequestDto(DEFAULT_CATEGORY_NAME);
    }

    public static MenuCategoryPostRequestDto createPostRequestWithName(String name) {
        return new MenuCategoryPostRequestDto(name);
    }

    public static MenuCategoryPutRequestDto createDefaultPutRequest() {
        return new MenuCategoryPutRequestDto(DEFAULT_CATEGORY_NAME);
    }

    public static MenuCategoryPutRequestDto createPutRequestWithName(String name) {
        return new MenuCategoryPutRequestDto(name);
    }

    public static MenuCategoryPatchRequestDto createPatchRequest(int orderNo) {
        return new MenuCategoryPatchRequestDto(orderNo);
    }
}
