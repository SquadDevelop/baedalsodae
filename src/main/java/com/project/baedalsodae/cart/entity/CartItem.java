package com.project.baedalsodae.cart.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import com.project.baedalsodae.menu.entity.MenuItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
		name = "p_cart_item",
		uniqueConstraints = {
				@UniqueConstraint(name = "uq_cart_item_menu_item", columnNames = {"cart_id", "menu_item_id"})
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cart_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_cart_item_cart_id"))
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_item_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_cart_item_menu_item_id"))
	private MenuItem menuItem;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	public int getLineAmount() {
		return menuItem.getPrice() * quantity;
	}

	public CartItem(Cart cart, MenuItem menuItem, int quantity) {
		this.cart = cart;
		this.menuItem = menuItem;
		this.quantity = quantity;
	}

	public static CartItem create(Cart cart, MenuItem menuItem, int quantity) {
		return new CartItem(cart, menuItem, quantity);
	}

	public boolean isSameMenuItem(UUID menuItemId){
		return this.menuItem.getId().equals(menuItemId);
	}

	public void increaseQuantity(int quantity) {
		this.quantity += quantity;
	}
}
