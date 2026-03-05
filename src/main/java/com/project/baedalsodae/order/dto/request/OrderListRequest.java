package com.project.baedalsodae.order.dto.request;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record OrderListRequest(LocalDate startDate, LocalDate endDate, Integer size) {

	public LocalDate resolvedStartDate() {
        return startDate != null ? startDate : LocalDate.now().minusMonths(3);
    }

    public LocalDate resolvedEndDate() {
        return endDate != null ? endDate : LocalDate.now();
    }
}
