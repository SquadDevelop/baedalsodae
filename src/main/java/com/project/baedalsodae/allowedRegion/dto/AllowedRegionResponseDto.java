package com.project.baedalsodae.allowedRegion.dto;

import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import java.util.List;
import java.util.UUID;

public record AllowedRegionResponseDto(
        UUID allowedRegionId,
        String sidoCode,
        String sidoName,
        String sigunguCode,
        String sigunguName,
        boolean isActive) {
    public static AllowedRegionResponseDto fromEntity(AllowedRegion allowedRegion) {
        return new AllowedRegionResponseDto(
                allowedRegion.getId(),
                allowedRegion.getSidoCode(),
                allowedRegion.getSidoName(),
                allowedRegion.getSigunguCode(),
                allowedRegion.getSigunguName(),
                allowedRegion.isActive());
    }

    public static List<AllowedRegionResponseDto> fromList(List<AllowedRegion> content) {
        return content.stream().map(AllowedRegionResponseDto::fromEntity).toList();
    }
}
