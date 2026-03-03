package com.project.baedalsodae.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeEntity {

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public LocalDateTime getLocalDateCreatedAt() {
    return createdAt != null ? LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault()) : null;
  }

  public LocalDateTime getLocalDateUpdatedAt() {
    return updatedAt != null ? LocalDateTime.ofInstant(updatedAt, ZoneId.systemDefault()) : null;
  }
}
