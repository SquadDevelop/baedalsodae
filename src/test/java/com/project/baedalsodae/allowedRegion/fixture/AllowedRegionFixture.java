package com.project.baedalsodae.allowedRegion.fixture;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionCursorRequest;
import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import com.project.baedalsodae.allowedRegion.entity.enums.AllowedRegionSortType;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

public class AllowedRegionFixture {

    private AllowedRegionFixture() {
        throw new BusinessException(ErrorCode.UTILITY_ASSERTION);
    }

    public static final String SIDO_CODE = "11";
    public static final String SIDO_NAME = "서울특별시";
    public static final String SIGUNGU_CODE = "11010";
    public static final String SIGUNGU_NAME = "종로구";

    public static AllowedRegion createActiveRegion() {
        return AllowedRegion.create(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);
    }

    public static AllowedRegion createInactiveRegion() {
        AllowedRegion region =
                AllowedRegion.create(SIDO_CODE, SIDO_NAME, SIGUNGU_CODE, SIGUNGU_NAME);
        region.deactivate();
        return region;
    }

    public static Slice<AllowedRegion> createSingleRegionSlice() {
        return new SliceImpl<>(List.of(createActiveRegion()));
    }

    public static Slice<AllowedRegion> createEmptySlice() {
        return new SliceImpl<>(List.of());
    }

    public static Slice<AllowedRegion> createSliceWithHasNext() {
        return new SliceImpl<>(List.of(createActiveRegion()), PageRequest.ofSize(10), true);
    }

    public static AllowedRegionCursorRequest createDefaultCursorRequest() {
        return new AllowedRegionCursorRequest(null, null, null, null, null, null, null, null, 10);
    }

    public static AllowedRegionCursorRequest createCursorRequestWithSidoName() {
        return new AllowedRegionCursorRequest(
                null, AllowedRegionSortType.SIDO_NAME, null, null, null, null, null, null, 10);
    }

    public static AllowedRegionCursorRequest createCursorRequestWithSidoCode() {
        return new AllowedRegionCursorRequest(
                null, null, null, null, null, null, SIDO_CODE, null, 10);
    }

    public static AllowedRegionCursorRequest createCursorRequestWithActiveFilter() {
        return new AllowedRegionCursorRequest(null, null, null, null, null, null, null, true, 10);
    }
}
