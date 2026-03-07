package com.project.baedalsodae.user.dto.response;

import com.project.baedalsodae.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDeleteResponse {

    private UUID id;
    private LocalDateTime deletedAt;
    private UUID deletedBy;

    public static UserDeleteResponse from(User user) {
        return UserDeleteResponse.builder()
                .id(user.getId())
                .deletedAt(user.getLocalDateDeletedAt())
                .deletedBy(user.getDeletedBy())
                .build();
    }
}
