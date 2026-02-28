package com.project.baedalsodae.cart.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

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

	@Column(name = "menu_item_id", nullable = false)
	private UUID menuItemId;

	@Column(name = "quantity", nullable = false)
	private int quantity;

}
