package com.project.baedalsodae.user.dto.response;

import com.project.baedalsodae.user.entity.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminUserDetailResponse {

    private UUID userId;
    private String username;
    private String phone;
    private String email;
    private String name;
    private String nickname;
    private UserRole role;

    private UserAddressResponse address;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID createdBy;
    private UUID updatedBy;

    public static AdminUserDetailResponse from(UserDetailResponse detailResponse) {
        UserAddressResponse singleAddress = (detailResponse.getAddresses() != null && !detailResponse.getAddresses().isEmpty())
                ? detailResponse.getAddresses().get(0)
                : null;

        return AdminUserDetailResponse.builder()
                .userId(detailResponse.getUserId())
                .username(detailResponse.getUsername())
                .phone(detailResponse.getPhone())
                .email(detailResponse.getEmail())
                .name(detailResponse.getName())
                .nickname(detailResponse.getNickname())
                .role(detailResponse.getRole())
                .address(singleAddress)
                .createdAt(detailResponse.getCreatedAt())
                .updatedAt(detailResponse.getUpdatedAt())
                .createdBy(detailResponse.getCreatedBy())
                .updatedBy(detailResponse.getUpdatedBy())
                .build();
    }
}
