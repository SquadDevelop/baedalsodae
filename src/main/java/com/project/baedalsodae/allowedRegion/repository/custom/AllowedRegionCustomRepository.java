package com.project.baedalsodae.allowedRegion.repository.custom;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionCursorRequest;
import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import org.springframework.data.domain.Slice;

public interface AllowedRegionCustomRepository {

    Slice<AllowedRegion> findAllowedRegionsByCursor(AllowedRegionCursorRequest cursor);
}
