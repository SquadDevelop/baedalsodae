package com.project.baedalsodae.allowedRegion.dto;

import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Builder
public class AllowedRegionPageResponse {
    private boolean hasNext;
    private int allowedRegionCount;
    private UUID lastCursorId;
    private List<AllowedRegionResponseDto> allowedRegions;

    public static AllowedRegionPageResponse of(Slice<AllowedRegion> slice) {
        return AllowedRegionPageResponse.builder()
                .hasNext(slice.hasNext())
                .allowedRegionCount(slice.getNumberOfElements())
                .lastCursorId(getLastCursorId(slice))
                .allowedRegions(AllowedRegionResponseDto.fromList(slice.getContent()))
                .build();
    }

    private static UUID getLastCursorId(Slice<AllowedRegion> slice) {
        List<AllowedRegion> content = slice.getContent();

        if (content.isEmpty()) return null;

        return content.get(content.size() - 1).getId();
    }
}
