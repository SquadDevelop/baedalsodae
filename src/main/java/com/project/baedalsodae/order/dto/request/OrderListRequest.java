package com.project.baedalsodae.order.dto.request;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record OrderListRequest(LocalDate startDate, LocalDate endDate) {}
