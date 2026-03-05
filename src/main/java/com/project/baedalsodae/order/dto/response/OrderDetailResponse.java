package com.project.baedalsodae.order.dto.response;

import com.project.baedalsodae.order.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}