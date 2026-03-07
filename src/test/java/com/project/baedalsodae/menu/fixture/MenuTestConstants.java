package com.project.baedalsodae.menu.fixture;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;

public final class MenuTestConstants {

    private MenuTestConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static UserDetailsImpl createManagerUserDetails() {
        return UserDetailsImpl.builder()
                .userId(UUID.randomUUID())
                .username(MANAGER_USERNAME)
                .password(null)
                .userRole(UserRole.MANAGER)
                .isDeleted(false)
                .build();
    }

    public static UserDetailsImpl createOwnerUserDetails(UUID ownerId) {
        return UserDetailsImpl.builder()
                .userId(ownerId)
                .username(OWNER_USERNAME)
                .password(null)
                .userRole(UserRole.OWNER)
                .isDeleted(false)
                .build();
    }

    public static final String ERROR_CODE_FIELD = "errorCode";

    // ORDER NUMBER
    public static final int FIRST_ORDER_NUMBER = 1;
    public static final int SECOND_ORDER_NUMBER = 2;
    public static final int THIRD_ORDER_NUMBER = 3;
    public static final int EXISTING_MAX_ORDER_NUMBER = 2;

    // USERNAME
    public static final String OWNER_USERNAME = "ownerUser";
    public static final String MANAGER_USERNAME = "managerUser";

    // MENU ITEM
    public static final String DEFAULT_MENU_ITEM_NAME = "후라이드치킨";
    public static final String DEFAULT_ITEM_DESCRIPTION = "바삭한 후라이드";
    public static final int DEFAULT_ITEM_PRICE = 18000;
    public static final String ALTERNATIVE_MENU_ITEM_NAME = "양념치킨";
    public static final String ALTERNATIVE_ITEM_DESCRIPTION = "달콤한 양념";
    public static final int ALTERNATIVE_ITEM_PRICE = 19000;

    // MENU CATEGORY
    public static final String DEFAULT_CATEGORY_NAME = "치킨류";
    public static final String ALTERNATIVE_CATEGORY_NAME = "피자류";
    public static final String NEW_CATEGORY_NAME = "새 카테고리";
    public static final String DUPLICATE_CATEGORY_NAME = "중복이름";
    public static final String NEW_UNIQUE_CATEGORY_NAME = "신카테고리";

    // TAG
    public static final String TAG_1 = "치킨";
    public static final String TAG_2 = "바삭";
}
