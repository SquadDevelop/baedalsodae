package com.project.baedalsodae.event.listener;

import com.project.baedalsodae.event.dto.OrderDeliveredEvent;
import com.project.baedalsodae.store.service.StoreCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreEventListener {

    private final StoreCommandService storeCommandService;

    @EventListener
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        storeCommandService.incrementOrderCount(event.storeId());
    }
}