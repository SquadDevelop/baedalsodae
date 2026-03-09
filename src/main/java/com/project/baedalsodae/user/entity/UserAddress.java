package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_user_address")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAddress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    private Address address;

    @Column(name = "description")
    private String description;

    public static UserAddress create(User user, Address address, String description) {
        return UserAddress.builder()
                .user(user)
                .address(address)
                .description(description)
                .build();
    }

    public void changeUser(User user) {
        if (this.user != null) {
            this.user.getUserAddresses().remove(this);
        }
        this.user = user;
    }

    public void update(UUID userAddressId, Address address, String description) {
        if (userAddressId != null) {
            this.id = userAddressId;
        }
        this.address = address;
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserAddress that)) {
            return false;
        }
        return this.getId() != null
                && that.getId() != null
                && Objects.equals(this.getId(), that.getId());
    }
}
