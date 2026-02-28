package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.UUID;

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
	@JoinColumn(name = "order_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_order_item_order_id"))
	private Order order;

	@Column(name = "menu_item_id", nullable = false)
	private UUID menuItemId;

	@Column(name = "name_snapshot")
	private String nameSnapshot;

	@Column(name = "price_snapshot")
	private Long priceSnapshot;

	@Column(name = "quantity")
	private Long quantity;
}