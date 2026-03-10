package com.project.baedalsodae.store.entity;

import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_store",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_store_business_number", columnNames = "business_number")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Store extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_store_category"))
    private StoreCategory storeCategory;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "business_number", length = 15, nullable = false)
    private String businessNumber;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Embedded private Address address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "avg_rating", nullable = false)
    private double avgRating = 0;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", nullable = false)
    private StoreStatus storeStatus = StoreStatus.PENDING_APPROVAL;

    public void addRating(double newRating) {
        double totalRating = this.avgRating * this.reviewCount;
        totalRating += newRating;
        this.reviewCount += 1;
        this.avgRating = totalRating / this.reviewCount;
    }

    public void updateRating(double oldRating, double newRating) {
        double totalRating = this.avgRating * this.reviewCount;
        totalRating = totalRating - oldRating + newRating;
        this.avgRating = totalRating / this.reviewCount;
    }

    public void deleteRating(double oldRating) {
        if (this.reviewCount <= 1) {
            this.avgRating = 0;
            this.reviewCount = 0;
            return;
        }
        double totalRating = this.avgRating * this.reviewCount;
        totalRating -= oldRating;
        this.reviewCount -= 1;
        this.avgRating = totalRating / this.reviewCount;
    }

    public void updateInfo(
            StoreCategory storeCategory,
            String name,
            String phone,
            Address address,
            String description) {
        this.storeCategory = storeCategory;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.description = description;
    }

    public void patchStoreOpened(StoreStatus storeStatus) {
        this.storeStatus = storeStatus;
    }

    private Store(
            UUID userId,
            StoreCategory storeCategory,
            String name,
            String businessNumber,
            String phone,
            Address address,
            String description) {
        this.userId = userId;
        this.storeCategory = storeCategory;
        this.name = name;
        this.businessNumber = businessNumber;
        this.phone = phone;
        this.address = address;
        this.description = description;
    }

    public static Store createStore(
            UUID userId,
            StoreCategory storeCategory,
            String name,
            String businessNumber,
            String phone,
            Address address,
            String description) {
        return new Store(userId, storeCategory, name, businessNumber, phone, address, description);
    }
}
