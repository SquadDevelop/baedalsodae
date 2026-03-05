package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.OrderStatusHistory;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderStatusResponse {

    private UUID orderId;

    private OrderStatus currentStatus;

    private List<OrderStatusHistoryResponse> histories;

    public static OrderStatusResponse from(Order order, List<OrderStatusHistory> histories) {

        List<OrderStatusHistoryResponse> historyResponses =
                histories.stream().map(OrderStatusHistoryResponse::from).toList();

        return new OrderStatusResponse(order.getId(), order.getStatus(), historyResponses);
    }
}
