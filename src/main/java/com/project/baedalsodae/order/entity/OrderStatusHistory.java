package com.project.baedalsodae.order.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import com.project.baedalsodae.order.entity.enums.ActorType;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    private OrderStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)
    private ActorType actorType;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    private OrderStatusHistory(
            UUID orderId,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            ActorType actorType,
            UUID actorId) {
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorType = actorType;
        this.actorId = actorId;
    }

    private OrderStatusHistory(
            UUID orderId,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            ActorType actorType,
            UUID actorId,
            String reason) {
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorType = actorType;
        this.actorId = actorId;
        this.reason = reason;
    }

    public static OrderStatusHistory createForCustomer(Order order, UUID userId) {
        return new OrderStatusHistory(
                order.getId(), null, order.getStatus(), ActorType.CUSTOMER, userId);
    }

    public static OrderStatusHistory createForCustomer(
            Order order, OrderStatus fromStatus, UUID userId) {
        return new OrderStatusHistory(
                order.getId(), fromStatus, order.getStatus(), ActorType.CUSTOMER, userId);
    }

    public static OrderStatusHistory createForOwner(
            Order order, OrderStatus fromStatus, UUID userId, String reason) {
        String why = "";
        why = order.getStatus().equals(OrderStatus.CANCELED) ? "주문 취소" : "";
        why = order.getStatus().equals(OrderStatus.REJECTED) ? "주문 거절" : "";
        if (reason == null) reason = "가게 사정으로 인하여 " + why + " 되었습니다.";

        return new OrderStatusHistory(
                order.getId(), fromStatus, order.getStatus(), ActorType.OWNER, userId, reason);
    }

    public static OrderStatusHistory createForSystem(Order order, OrderStatus fromStatus) {
        return new OrderStatusHistory(
                order.getId(), fromStatus, order.getStatus(), ActorType.SYSTEM, null);
    }

    public static OrderStatusHistory createForAdmin(Order order, OrderStatus fromStatus, UUID adminId, String reason) {
        String why = "";
        why = order.getStatus().equals(OrderStatus.CANCELED) ? "주문 취소" : "";
        why = order.getStatus().equals(OrderStatus.REJECTED) ? "주문 거절" : "";
        if (reason == null) reason = "관리자의 강제 취소로 인하여 " + why + " 되었습니다.";
        
        return new OrderStatusHistory(
                order.getId(), fromStatus, order.getStatus(), ActorType.MANAGER, adminId, reason);
    }
}
