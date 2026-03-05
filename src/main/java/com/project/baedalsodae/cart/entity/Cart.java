package com.project.baedalsodae.cart.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import com.project.baedalsodae.store.entity.Store;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_cart",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_cart_user_store",
                    columnNames = {"user_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToMany(
            mappedBy = "cart",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public int getTotalAmount() {
        return items.stream().mapToInt(CartItem::getLineAmount).sum();
    }

    public void addItem(CartItem item) {
        for (CartItem existingCartItem : this.items) {
            if (existingCartItem.isSameMenuItem(item.getMenuItem().getId())) {
                existingCartItem.increaseQuantity(item.getQuantity());
                return;
            }
        }
        this.items.add(item);
    }

    public boolean isSameStore(UUID storeId) {
        return this.store.getId().equals(storeId);
    }

    public Optional<CartItem> findCartItemById(UUID cartItemId) {
        for (CartItem existingCartItem : this.items) {
            if (existingCartItem.getId().equals(cartItemId)) {
                return Optional.of(existingCartItem);
            }
        }
        return Optional.empty();
    }

    public boolean removeItem(UUID cartItemId) {
        return this.items.removeIf(i -> i.getId().equals(cartItemId));
    }

    public boolean hasNoItems() {
        return this.items.isEmpty();
    }

    private Cart(UUID userId, Store store) {
        this.userId = userId;
        this.store = store;
    }

    public static Cart create(UUID userId, Store store) {
        return new Cart(userId, store);
    }
}
