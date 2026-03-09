package com.project.baedalsodae.allowedRegion.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_allowed_region",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_allowed_region_sigungu_code",
                    columnNames = {"sigungu_code"}),
            @UniqueConstraint(
                    name = "uq_allowed_region_sido_sigungu_code",
                    columnNames = {"sido_code", "sigungu_code"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AllowedRegion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sido_code", length = 20, nullable = false)
    private String sidoCode;

    @Column(name = "sido_name", length = 50, nullable = false)
    private String sidoName;

    @Column(name = "sigungu_code", length = 20, nullable = false, unique = true)
    private String sigunguCode;

    @Column(name = "sigungu_name", length = 50, nullable = false)
    private String sigunguName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    private AllowedRegion(
            String sidoCode, String sidoName, String sigunguCode, String sigunguName) {
        this.sidoCode = sidoCode;
        this.sidoName = sidoName;
        this.sigunguCode = sigunguCode;
        this.sigunguName = sigunguName;
    }

    public static AllowedRegion create(
            String sidoCode, String sidoName, String sigunguCode, String sigunguName) {
        return new AllowedRegion(sidoCode, sidoName, sigunguCode, sigunguName);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
