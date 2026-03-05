package com.project.baedalsodae.order.dto.request;

import com.project.baedalsodae.order.entity.enums.OrderStatus;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record OrderListRequest(
        OrderStatus status, LocalDate startDate, LocalDate endDate, String keyword, Integer size) {

	public LocalDate resolvedStartDate() {
        return startDate != null ? startDate : LocalDate.now().minusMonths(3);
    }

    public LocalDate resolvedEndDate() {
        return endDate != null ? endDate : LocalDate.now();
    }
}
