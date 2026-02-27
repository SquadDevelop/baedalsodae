package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseAuditEntity {

	@Comment("주문 ID")
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Comment("가게 ID")
	@Column(name = "store_id", nullable = false)
	private UUID storeId;

	@Comment("회원 ID")
	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Comment("주문 번호(고객 표시)")
	@Column(name = "order_no", length = 100, unique = true)
	private String orderNo;

	@Comment("주문 상태")
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderStatus status = OrderStatus.CREATED;

	@Comment("가게 요청사항")
	@Column(name = "request_note", columnDefinition = "TEXT")
	private String requestNote;

	@Comment("배달주소 스냅샷(고객 표시)")
	@Column(name = "delivery_address_snapshot", columnDefinition = "TEXT")
	private String deliveryAddressSnapshot;

	@Comment("메뉴 합계 금액")
	@Column(name = "total_amount")
	private Long totalAmount;

	@Comment("배달 금액")
	@Column(name = "delivery_fee")
	private Long deliveryFee;

	@Comment("할인 금액")
	@Column(name = "discount_amount")
	private Long discountAmount;

	@Comment("최종 결제 금액")
	@Column(name = "final_amount")
	private Long finalAmount;

}