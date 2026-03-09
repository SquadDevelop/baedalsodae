package com.project.baedalsodae.menu.fixture;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MenuItemRequestFixture {

    private MenuItemRequestFixture() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static MenuItemPostRequestBuilder aPostRequest() {
        return new MenuItemPostRequestBuilder();
    }

    public static MenuItemPutRequestBuilder aPutRequest() {
        return new MenuItemPutRequestBuilder();
    }

    public static MenuItemPatchRequestBuilder aPatchRequest() {
        return new MenuItemPatchRequestBuilder();
    }

    public static MenuItemPostRequestDto createDefaultPostRequest() {
        return aPostRequest().build();
    }

    public static MenuItemPatchRequestDto createPatchRequestWithName(String name) {
        return aPatchRequest().withName(name).build();
    }

    public static MenuItemPatchRequestDto createEmptyPatchRequest() {
        return aPatchRequest().build();
    }

    public static MenuItemPatchRequestDto createPatchRequestWithTags(List<String> tags) {
        return aPatchRequest().withTagNames(tags).build();
    }

    public static MenuItemPatchRequestDto createPatchRequestWithCategory(UUID categoryId) {
        return aPatchRequest().withCategoryId(categoryId).build();
    }

    public static class MenuItemPostRequestBuilder {
        private String name = DEFAULT_MENU_ITEM_NAME;
        private String description = DEFAULT_ITEM_DESCRIPTION;
        private BigDecimal price = DEFAULT_ITEM_PRICE;
        private Boolean isPopular = false;
        private MenuStatus menuStatus = MenuStatus.AVAILABLE;
        private List<String> tagNames = List.of("치킨", "바삭");

        public MenuItemPostRequestBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MenuItemPostRequestBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public MenuItemPostRequestBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public MenuItemPostRequestBuilder withIsPopular(Boolean isPopular) {
            this.isPopular = isPopular;
            return this;
        }

        public MenuItemPostRequestBuilder withMenuStatus(MenuStatus menuStatus) {
            this.menuStatus = menuStatus;
            return this;
        }

        public MenuItemPostRequestBuilder withTagNames(List<String> tagNames) {
            this.tagNames = tagNames;
            return this;
        }

        public MenuItemPostRequestDto build() {
            return new MenuItemPostRequestDto(
                    name, description, price, isPopular, menuStatus, tagNames);
        }
    }

    public static class MenuItemPutRequestBuilder {
        private String name = ALTERNATIVE_MENU_ITEM_NAME;
        private String description = ALTERNATIVE_ITEM_DESCRIPTION;
        private BigDecimal price = ALTERNATIVE_ITEM_PRICE;
        private Boolean isPopular = true;
        private UUID categoryId;
        private MenuStatus menuStatus = MenuStatus.AVAILABLE;
        private List<String> tagNames = List.of("치킨");

        public MenuItemPutRequestBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MenuItemPutRequestBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public MenuItemPutRequestBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public MenuItemPutRequestBuilder withIsPopular(Boolean isPopular) {
            this.isPopular = isPopular;
            return this;
        }

        public MenuItemPutRequestBuilder withCategoryId(UUID categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public MenuItemPutRequestBuilder withMenuStatus(MenuStatus menuStatus) {
            this.menuStatus = menuStatus;
            return this;
        }

        public MenuItemPutRequestBuilder withTagNames(List<String> tagNames) {
            this.tagNames = tagNames;
            return this;
        }

        public MenuItemPutRequestDto build() {
            return new MenuItemPutRequestDto(
                    name, description, price, isPopular, categoryId, menuStatus, tagNames);
        }
    }

    public static class MenuItemPatchRequestBuilder {
        private String name;
        private String description;
        private BigDecimal price;
        private Boolean isPopular;
        private UUID categoryId;
        private MenuStatus menuStatus;
        private List<String> tagNames;

        public MenuItemPatchRequestBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MenuItemPatchRequestBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public MenuItemPatchRequestBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public MenuItemPatchRequestBuilder withIsPopular(Boolean isPopular) {
            this.isPopular = isPopular;
            return this;
        }

        public MenuItemPatchRequestBuilder withCategoryId(UUID categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public MenuItemPatchRequestBuilder withMenuStatus(MenuStatus menuStatus) {
            this.menuStatus = menuStatus;
            return this;
        }

        public MenuItemPatchRequestBuilder withTagNames(List<String> tagNames) {
            this.tagNames = tagNames;
            return this;
        }

        public MenuItemPatchRequestDto build() {
            return new MenuItemPatchRequestDto(
                    name, description, price, isPopular, categoryId, menuStatus, tagNames);
        }
    }
}
