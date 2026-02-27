package com.project.baedalsodae.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseAuditEntity extends BaseTimeEntity {

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private Long createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  private Long updatedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "is_deleted")
  @ColumnDefault("false")
  private boolean isDeleted = false;

  public void softDelete(Long userId) {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = userId;
  }

  public void restore() {
    this.isDeleted = false;
    this.deletedAt = null;
    this.deletedBy = null;
  }
}
