package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.cart.entity.CartItem;
import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_order_id"))
    private Order order;

    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;

    @Column(name = "name_snapshot")
    private String nameSnapshot;

    @Column(name = "price_snapshot")
    private int priceSnapshot;

    @Column(name = "quantity")
    private int quantity;

    private OrderItem(
            Order order, UUID menuItemId, String nameSnapshot, int priceSnapshot, int quantity) {
        this.order = order;
        this.menuItemId = menuItemId;
        this.nameSnapshot = nameSnapshot;
        this.priceSnapshot = priceSnapshot;
        this.quantity = quantity;
    }

    public static OrderItem create(Order order, CartItem cartItem) {
        return new OrderItem(
                order,
                cartItem.getMenuItem().getId(),
                cartItem.getMenuItem().getName(),
                cartItem.getMenuItem().getPrice(),
                cartItem.getQuantity());
    }
}
