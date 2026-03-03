package com.project.baedalsodae.store.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "p_store_category",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_business_number", columnNames = "business_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreCategory extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    public void patchStoreCategory(String name, String description) {
        if(name != null) {
            this.name = name;
        }
        this.description = description;
    }

    public static StoreCategory createStoreCategory(String name, String description) {
        return new StoreCategory(null, name, description);
    }
}
