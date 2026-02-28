package com.project.baedalsodae.tag.entity;

import com.project.baedalsodae.menu.entity.MenuItem;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "p_tag_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TagMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "order_no", nullable = false)
  private int orderNo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tag_id", nullable = false)
  private Tag tag;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "menu_item", nullable = false)
  private MenuItem menuItem;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static TagMapping create(Tag tag, MenuItem menuItem, int orderNo) {
    TagMapping tagMapping = new TagMapping();
    tagMapping.tag = tag;
    tagMapping.menuItem = menuItem;
    tagMapping.orderNo = orderNo;
    return tagMapping;
  }
}
