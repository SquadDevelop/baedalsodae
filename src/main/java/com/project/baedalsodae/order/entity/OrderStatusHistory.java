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

    public static OrderStatusHistory createForCustomer(Order order, UUID userId) {
        return new OrderStatusHistory(
                order.getId(), null, order.getStatus(), ActorType.CUSTOMER, userId);
    }

    public static OrderStatusHistory createForOwner(Order order, OrderStatus fromStatus, UUID userId) {
        return new OrderStatusHistory(
                order.getId(), fromStatus, order.getStatus(), ActorType.OWNER, userId);
    }
}
