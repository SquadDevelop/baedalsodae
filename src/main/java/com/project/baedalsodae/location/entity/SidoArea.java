package com.project.baedalsodae.location.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_sido_area")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SidoArea extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "adm_code", nullable = false, length = 50, unique = true)
    private String admCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Version private Long version;
}
