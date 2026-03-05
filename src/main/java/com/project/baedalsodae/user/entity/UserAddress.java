package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
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
@Table(name = "p_user_address") // 추후 지역코드 & 지역명 관련 unique 제약조건 추가 필요
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

    // 임시 주석 처리
    //    @Column(name = "sido_code", nullable = false)
    //    private String sidoCode;
    //
    //    @Column(name = "sido_name", nullable = false)
    //    private String sidoName;
    //
    //    @Column(name = "sigg_code", nullable = false)
    //    private String sigunguCode;
    //
    //    @Column(name = "sigg_name", nullable = false)
    //    private String sigunguName;
    //
    //    @Column(name = "dong_code", nullable = false)
    //    private String dongCode;
    //
    //    @Column(name = "dong_name", nullable = false)
    //    private String dongName;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "description")
    private String description;

    public static UserAddress create(
            User user, String roadAddress, String detailAddress, String description) {
        return UserAddress.builder()
                .user(user)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .description(description)
                .build();
    }

    public void changeUser(User user) {
        if (this.user != null) {
            this.user.getUserAddresses().remove(this);
        }
        this.user = user;
    }

    public void update(String roadAddress, String detailAddress, String description) {
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
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
