package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.global.common.util.TimeUtils;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.entity.enums.ActorType;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusHistoryResponse {

    private OrderStatus fromStatus;

    private OrderStatus toStatus;

    private ActorType actorType;

    private UUID actorId;

    private LocalDateTime changedAt;

    public static OrderStatusHistoryResponse from(OrderStatusHistory history) {

        return new OrderStatusHistoryResponse(
                history.getFromStatus(),
                history.getToStatus(),
                history.getActorType(),
                history.getActorId(),
                TimeUtils.toLocalDateTime(history.getCreatedAt()));
    }
}
