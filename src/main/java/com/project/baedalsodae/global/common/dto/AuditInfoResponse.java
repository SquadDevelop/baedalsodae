package com.project.baedalsodae.global.common.dto;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class AuditInfoResponse {
    private final UUID createdBy;
    private final Instant createdAt;
    private final UUID updatedBy;
    private final Instant updatedAt;
    private final UUID deletedBy;
    private final Instant deletedAt;

    private AuditInfoResponse(BaseAuditEntity entity) {
        this.createdBy = entity.getCreatedBy();
        this.createdAt = entity.getCreatedAt();
        this.updatedBy = entity.getUpdatedBy();
        this.updatedAt = entity.getUpdatedAt();
        this.deletedBy = entity.getDeletedBy();
        this.deletedAt = entity.getDeletedAt();
    }

    public static AuditInfoResponse fromEntity(BaseAuditEntity entity){
        if (entity == null) return null;
        return new AuditInfoResponse(entity);
    }
}
