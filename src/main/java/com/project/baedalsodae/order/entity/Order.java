package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "store_id", nullable = false)
	private UUID storeId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "order_no", length = 100, unique = true)
	private String orderNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderStatus status = OrderStatus.CREATED;

	@Column(name = "store_request_note", columnDefinition = "TEXT")
	private String storeRequestNote;

	@Column(name = "delivery_request_note", columnDefinition = "TEXT")
	private String deliveryRequestNote;

	@Column(name = "delivery_address_snapshot", columnDefinition = "TEXT")
	private String deliveryAddressSnapshot;

	@Column(name = "total_amount")
	private Long totalAmount;

	@Column(name = "delivery_fee")
	private Long deliveryFee;

	@Column(name = "discount_amount")
	private Long discountAmount;

	@Column(name = "final_amount")
	private Long finalAmount;

}