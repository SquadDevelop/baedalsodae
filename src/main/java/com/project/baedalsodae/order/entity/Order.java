package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.store.entity.Store;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_no", length = 100, unique = true)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_nickname_snapshot", nullable = false)
    private String userNicknameSnapshot;

    @Column(name = "user_phone_snapshot", nullable = false)
    private String userPhoneSnapshot;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "store_name_snapshot", nullable = false, length = 50)
    private String storeNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "store_request_note", columnDefinition = "TEXT")
    private String storeRequestNote;

    @Column(name = "delivery_request_note", columnDefinition = "TEXT")
    private String deliveryRequestNote;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(name = "delivery_address_snapshot", columnDefinition = "TEXT")
    private String deliveryAddressSnapshot;

    @Column(name = "total_amount")
    private int totalAmount;

    @Column(name = "delivery_fee")
    private int deliveryFee;

    @Column(name = "discount_amount")
    private int discountAmount;

    @Column(name = "final_amount")
    private int finalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public void addOrderItems(List<OrderItem> orderItems) {
        this.items.addAll(orderItems);
    }

    private Order(
            UUID userId,
            UUID storeId,
            String storeNameSnapshot,
            UUID addressId,
            String deliveryAddressSnapshot,
            String orderNo,
            String storeRequestNote,
            String deliveryRequestNote,
            int totalAmount,
            int deliveryFee,
            int discountAmount,
            int finalAmount,
            OrderStatus status) {
        this.userId = userId;
        this.storeId = storeId;
        this.storeNameSnapshot = storeNameSnapshot;
        this.addressId = addressId;
        this.deliveryAddressSnapshot = deliveryAddressSnapshot;
        this.orderNo = orderNo;
        this.storeRequestNote = storeRequestNote;
        this.deliveryRequestNote = deliveryRequestNote;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.status = status;
    }

    public static Order create(
            UUID userId,
            Store store,
            UUID addressId,
            String deliveryAddressSnapshot,
            String orderNo,
            String storeRequestNote,
            String deliveryRequestNote,
            int totalAmount,
            int deliveryFee,
            int discountAmount,
            int finalAmount) {
        return new Order(
                userId,
                store.getId(),
                store.getName(),
                addressId,
                deliveryAddressSnapshot,
                orderNo,
                storeRequestNote,
                deliveryRequestNote,
                totalAmount,
                deliveryFee,
                discountAmount,
                finalAmount,
                OrderStatus.CREATED);
    }
}
