package com.project.baedalsodae.store.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import java.util.UUID;

@Entity
@Table(
        name = "p_store",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_business_number", columnNames = "business_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Store extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_id"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_store_category"))
    private StoreCategory storeCategory;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "business_number", length = 10, nullable = false)
    private String businessNumber;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "sido_id", nullable = false)
    private UUID sidoId;

    @Column(name = "sigungu_id", nullable = false)
    private UUID sigunguId;

    @Column(name = "dong_id", nullable = false)
    private UUID dongId;

    @Column(name = "road_address", length = 255, nullable = false)
    private String roadAddress;

    @Column(name = "detail_address", length = 255, nullable = false)
    private String detailAddress;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "rating_sum", nullable = false)
    @ColumnDefault("0")
    private int ratingSum = 0;

    @Column(name = "review_count", nullable = false)
    @ColumnDefault("0")
    private int reviewCount = 0;

    @Column(name = "is_closed", nullable = false)
    @ColumnDefault("true")
    private boolean isClosed = true;
}