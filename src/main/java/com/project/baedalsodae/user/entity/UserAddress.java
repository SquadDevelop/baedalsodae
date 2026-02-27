package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "p_user_address")
public class UserAddress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "sido_id", nullable = false)
    private UUID sidoId;

    @Column(name = "sigungu_id", nullable = false)
    private UUID sigunguId;

    @Column(name = "dong_id", nullable = false)
    private UUID dongId;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "description")
    private String description;
}
