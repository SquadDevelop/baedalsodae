package com.project.baedalsodae.menu.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import com.project.baedalsodae.tag.entity.TagMapping;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_menu_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem extends BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "is_popular", nullable = false)
  private boolean isPopular = false;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "order_no", nullable = false)
  private int orderNo;

  @Column(name = "price", nullable = false)
  private int price;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @JoinColumn(name = "menu_category_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private MenuCategory menuCategory;

  @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TagMapping> tagMappings = new ArrayList<>();

  @Column(name = "menu_status")
  @Enumerated(EnumType.STRING)
  private MenuStatus menuStatus;
}
