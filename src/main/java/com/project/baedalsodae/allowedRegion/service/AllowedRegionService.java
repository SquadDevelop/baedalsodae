package com.project.baedalsodae.allowedRegion.service;

import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.user.entity.UserAddress;

public interface AllowedRegionService {

  boolean isAllowedByCode(String sigunguCode);

  boolean isAllowedByName(String sigunguName);
}
