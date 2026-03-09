package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.store.entity.Store;
import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_fee", precision = 15, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", precision = 15, scale = 2)
    private BigDecimal finalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public void addOrderItems(List<OrderItem> orderItems) {
        this.items.addAll(orderItems);
    }

    public boolean canRequest() {
        return this.status == OrderStatus.CREATED;
    }

    public void request() {
        this.status = OrderStatus.REQUESTED;
    }

    public boolean canAcceptOrReject() {
        return this.status == OrderStatus.REQUESTED;
    }

    public void accept() {
        this.status = OrderStatus.ACCEPTED;
    }

    public void reject() {
        this.status = OrderStatus.REJECTED;
    }

    public boolean canCompleteCooking() {
        return this.status == OrderStatus.ACCEPTED;
    }

    public void completeCooking() {
        this.status = OrderStatus.COOKED;
    }

    public boolean canStartDelivery() {
        return this.status == OrderStatus.COOKED;
    }

    public void startDelivery() {
        this.status = OrderStatus.DELIVERING;
    }

    public boolean canCompleteDelivery() {
        return this.status == OrderStatus.DELIVERING;
    }

    public void completeDelivery() {
        this.status = OrderStatus.DELIVERED;
    }

    public boolean canCancelRequestByCustomer() {
        return this.status == OrderStatus.REQUESTED || this.status == OrderStatus.ACCEPTED;
    }

    public boolean canCancelRequestByOwner() {
        return this.status == OrderStatus.ACCEPTED;
    }

    public void cancelRequested() {
        this.status = OrderStatus.CANCEL_REQUESTED;
    }

    public boolean canCompleteCancel() {
        return this.status == OrderStatus.CANCEL_REQUESTED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    private Order(
            UUID userId,
            String userNicknameSnapshot,
            String userPhoneSnapshot,
            UUID storeId,
            String storeNameSnapshot,
            UUID addressId,
            String deliveryAddressSnapshot,
            String orderNo,
            String storeRequestNote,
            String deliveryRequestNote,
            BigDecimal totalAmount,
            BigDecimal deliveryFee,
            BigDecimal discountAmount,
            BigDecimal finalAmount,
            OrderStatus status) {
        this.userId = userId;
        this.userNicknameSnapshot = userNicknameSnapshot;
        this.userPhoneSnapshot = userPhoneSnapshot;
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
            String userNicknameSnapshot,
            String userPhoneSnapshot,
            Store store,
            UUID addressId,
            String deliveryAddressSnapshot,
            String orderNo,
            String storeRequestNote,
            String deliveryRequestNote,
            BigDecimal totalAmount,
            BigDecimal deliveryFee,
            BigDecimal discountAmount,
            BigDecimal finalAmount) {
        return new Order(
                userId,
                userNicknameSnapshot,
                userPhoneSnapshot,
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
