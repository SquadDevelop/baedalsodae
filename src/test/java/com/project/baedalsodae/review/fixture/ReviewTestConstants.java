package com.project.baedalsodae.review.fixture;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;

public final class ReviewTestConstants {

    private ReviewTestConstants() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static UserDetailsImpl createCustomerUserDetails(UUID userId) {
        return UserDetailsImpl.builder()
                .userId(userId)
                .username(CUSTOMER_USERNAME)
                .password(null)
                .userRole(UserRole.CUSTOMER)
                .isDeleted(false)
                .build();
    }

    public static final String CUSTOMER_USERNAME = "customerUser";

    public static final int DEFAULT_RATING = 5;
    public static final int UPDATED_RATING = 3;
    public static final String DEFAULT_COMMENT = "맛있어요";
    public static final String UPDATED_COMMENT = "생각보다 별로였어요";
}
