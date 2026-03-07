package com.project.baedalsodae.user.dto.response;

import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDetailResponse {

    private UUID userId;
    private String username;
    private String phone;
    private String email;
    private String name;
    private String nickname;
    private UserRole role;

    private UUID userMainAddressId;
    private List<UserAddressResponse> addresses;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID updatedBy;

    public static UserDetailResponse from(User user) {
        UUID mainAddressId = user.getUserMainAddressId();
        List<UserAddressResponse> addressResponses =
                user.getUserAddresses().stream()
                        .map(address -> UserAddressResponse.from(address, mainAddressId))
                        .toList();

        return UserDetailResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .role(user.getRole())
                .userMainAddressId(user.getUserMainAddressId())
                .addresses(addressResponses)
                .createdAt(user.getLocalDateCreatedAt())
                .updatedAt(user.getLocalDateUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
