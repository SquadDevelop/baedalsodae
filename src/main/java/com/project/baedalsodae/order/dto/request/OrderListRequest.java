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
        UUID storeId,
        Integer size,
        Instant cursorCreatedAt,
        UUID cursorId) {

	public LocalDate resolvedStartDate() {
        return startDate != null ? startDate : LocalDate.now().minusMonths(3);
    }

    public LocalDate resolvedEndDate() {
        return endDate != null ? endDate : LocalDate.now();
    }
}
