package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import com.project.baedalsodae.location.entity.EndArea;
import com.project.baedalsodae.location.entity.SidoArea;
import com.project.baedalsodae.location.entity.SiggArea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_id", nullable = false)
    private SidoArea sido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sigg_id", nullable = false)
    private SiggArea sigungu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dong_id", nullable = false)
    private EndArea dong;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "description")
    private String description;

    public static UserAddress create(User user, String roadAddress, String detailAddress, String description) {
        return UserAddress.builder()
                .user(user)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .description(description)
                .build();
    }

    public void changeUser(User user) {
        this.user = user;
    }

    public void update(String roadAddress, String detailAddress, String description) {
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.description = description;
    }
}
