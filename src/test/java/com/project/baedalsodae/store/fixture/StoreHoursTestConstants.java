package com.project.baedalsodae.store.fixture;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.user.entity.UserRole;
import java.time.LocalTime;
import java.util.UUID;

public final class StoreHoursTestConstants {

    private StoreHoursTestConstants() {
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

    public static final String OWNER_USERNAME = "ownerUser";
    public static final String MANAGER_USERNAME = "managerUser";

    public static final LocalTime DEFAULT_OPEN_TIME = LocalTime.of(9, 0);
    public static final LocalTime DEFAULT_CLOSE_TIME = LocalTime.of(22, 0);
    public static final LocalTime DEFAULT_BREAK_START = LocalTime.of(15, 0);
    public static final LocalTime DEFAULT_BREAK_END = LocalTime.of(16, 0);
}
