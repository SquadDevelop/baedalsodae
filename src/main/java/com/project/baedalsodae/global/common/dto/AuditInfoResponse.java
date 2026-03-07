package com.project.baedalsodae.global.common.dto;

import static com.project.baedalsodae.global.common.util.TimeUtils.toLocalDateTime;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AuditInfoResponse {
    private final UUID createdBy;
    private final LocalDateTime createdAt;
    private final UUID updatedBy;
    private final LocalDateTime updatedAt;
    private final UUID deletedBy;
    private final LocalDateTime deletedAt;

    private AuditInfoResponse(BaseAuditEntity entity) {
        this.createdBy = entity.getCreatedBy();
        this.createdAt = toLocalDateTime(entity.getCreatedAt());
        this.updatedBy = entity.getUpdatedBy();
        this.updatedAt = toLocalDateTime(entity.getUpdatedAt());
        this.deletedBy = entity.getDeletedBy();
        this.deletedAt = toLocalDateTime(entity.getDeletedAt());
    }

    public static AuditInfoResponse fromEntity(BaseAuditEntity entity) {
        if (entity == null) return null;
        return new AuditInfoResponse(entity);
    }
}
