package com.project.baedalsodae.order.dto.request;

import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderListRequest(
        OrderStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String keyword,
        String orderNo,
        UUID storeId,
        Integer size,
        Instant cursorCreatedAt,
        UUID cursorId) {}
