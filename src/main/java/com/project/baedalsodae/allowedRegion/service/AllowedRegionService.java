package com.project.baedalsodae.allowedRegion.service;

public interface AllowedRegionService {

    boolean isAllowedByCode(String sigunguCode);

    boolean isAllowedByName(String sigunguName);
}
