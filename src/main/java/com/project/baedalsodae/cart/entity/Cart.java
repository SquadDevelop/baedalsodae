package com.project.baedalsodae.cart.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
		name = "p_cart",
		uniqueConstraints = {
				@UniqueConstraint(name = "uq_cart_user_store", columnNames = {"user_id"})
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "store_id", nullable = false)
	private UUID storeId;

	@OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
		return this.storeId.equals(storeId);
	}

	public Cart(UUID userId, UUID storeId) {
		this.userId = userId;
		this.storeId = storeId;
	}

	public static Cart create(UUID userId, UUID storeId) {
		return new Cart(userId, storeId);
	}

}
