package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserAddress> userAddresses = new ArrayList<>();

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
}
