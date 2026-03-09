package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_user")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;

    @Column(name = "phone", nullable = false, length = 100)
    private String phone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "nickname", nullable = false, length = 100, unique = true)
    private String nickname;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "user_main_address_id")
    private UUID userMainAddressId;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> userAddresses = new ArrayList<>();

    @PrePersist
    public void prePersist() {}

    public static User create(
            String username,
            String phone,
            String email,
            String encodedPassword,
            String name,
            String nickname,
            UserRole role) {
        return User.builder()
                .username(username)
                .phone(phone)
                .email(email)
                .password(encodedPassword)
                .name(name)
                .nickname(nickname)
                .role(role)
                .build();
    }

    public void update(String phone, String email, String password, String nickname) {
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
    }

    public void addAddress(UserAddress address) {
        if (!this.userAddresses.contains(address)) {
            this.userAddresses.add(address);
        }
        if (address.getUser() != this) {
            address.changeUser(this);
        }
    }

    public void addAddresses(List<UserAddress> addresses) {
        addresses.forEach(this::addAddress);
        if (this.userMainAddressId == null && !this.userAddresses.isEmpty()) {
            this.changeMainAddress(this.userAddresses.get(0).getId());
        }
    }

    public void updateAddresses(List<UserAddress> newAddresses) {
        Set<UUID> newIds =
                newAddresses.stream()
                        .map(UserAddress::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        this.userAddresses.removeIf(
                existing -> existing.getId() != null && !newIds.contains(existing.getId()));

        Map<UserAddress, UserAddress> existingMap =
                this.userAddresses.stream().collect(Collectors.toMap(a -> a, a -> a));

        newAddresses.forEach(
                newAddr -> {
                    Optional.ofNullable(existingMap.get(newAddr))
                            .ifPresentOrElse(
                                    existing ->
                                            existing.update(
                                                    existing.getId(),
                                                    newAddr.getAddress(),
                                                    newAddr.getDescription()),
                                    () -> this.addAddress(newAddr));
                });

        if (this.userMainAddressId != null && this.getMainAddress() == null) {
            this.userMainAddressId =
                    this.userAddresses.isEmpty() ? null : this.userAddresses.get(0).getId();
        } else if (this.userMainAddressId == null && !this.userAddresses.isEmpty()) {
            this.userMainAddressId = this.userAddresses.get(0).getId();
        }
    }

    public UserAddress getMainAddress() {
        return this.userAddresses.stream()
                .filter(
                        userAddress ->
                                this.userMainAddressId != null
                                        && this.userMainAddressId.equals(userAddress.getId()))
                .findAny()
                .orElse(null);
    }

    public void changeMainAddress(UUID userMainAddressId) {
        this.userMainAddressId = userMainAddressId;
    }
}
