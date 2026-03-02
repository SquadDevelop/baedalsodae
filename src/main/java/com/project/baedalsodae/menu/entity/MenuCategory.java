package com.project.baedalsodae.menu.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.menu.common.Orderable;
import com.project.baedalsodae.store.entity.Store;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "p_menu_category",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_menu_category_order_no",
          columnNames = {"store_id", "order_no"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategory extends BaseAuditEntity implements Orderable {

  @OneToMany(mappedBy = "menuCategory", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<MenuItem> menuItems = new ArrayList<>();

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "order_no", nullable = false)
  private int orderNo;

  @JoinColumn(name = "store_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Store store;

  public void changeMenuCategoryName(String name) {
    this.name = name;
  }

  @Override
  public void changeOrderNo(int orderNo) {
    this.orderNo = orderNo;
  }
}
