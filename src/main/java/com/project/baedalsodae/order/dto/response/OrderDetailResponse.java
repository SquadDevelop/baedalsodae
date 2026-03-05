package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.global.common.util.TimeUtils;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderDetailResponse {

    private UUID orderId;
    private String orderNo;

    private String userNickname;
    private String userPhone;

    private UUID storeId;
    private String storeName;

    private OrderStatus status;

    private String storeRequestNote;
    private String deliveryRequestNote;

    private String deliveryAddressSnapshot;

    private int totalAmount;
    private int deliveryFee;
    private int discountAmount;
    private int finalAmount;

    private List<OrderItemDetailResponse> items;

    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderedAt;

    public static OrderDetailResponse from(Order order) {

        List<OrderItemDetailResponse> items =
                order.getItems().stream().map(OrderItemDetailResponse::from).toList();

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userNickname(order.getUserNicknameSnapshot())
                .userPhone(order.getUserPhoneSnapshot())
                .storeId(order.getStoreId())
                .storeName(order.getStoreNameSnapshot())
                .status(order.getStatus())
                .storeRequestNote(order.getStoreRequestNote())
                .deliveryRequestNote(order.getDeliveryRequestNote())
                .deliveryAddressSnapshot(order.getDeliveryAddressSnapshot())
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .items(items)
                .orderCreatedAt(TimeUtils.toLocalDateTime(order.getCreatedAt()))
                .orderedAt(TimeUtils.toLocalDateTime(order.getUpdatedAt()))
                .build();
    }
}
