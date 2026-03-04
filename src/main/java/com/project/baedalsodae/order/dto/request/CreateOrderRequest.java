package com.project.baedalsodae.order.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateOrderRequest(
        @NotNull UUID cartId,
        @NotNull UUID addressId,
        String storeRequestMessage,
        String deliveryRequestMessage) {}
