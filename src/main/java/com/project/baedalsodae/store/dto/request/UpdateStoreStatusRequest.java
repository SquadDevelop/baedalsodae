package com.project.baedalsodae.store.dto.request;

import com.project.baedalsodae.store.entity.enums.StoreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateStoreStatusRequest {
    @NotNull(message = "가게 상태는 필수입니다.")
    StoreStatus status;
}
