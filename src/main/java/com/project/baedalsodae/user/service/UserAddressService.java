package com.project.baedalsodae.user.service;

import com.project.baedalsodae.user.dto.request.UserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import java.util.List;
import java.util.UUID;

public interface UserAddressService {

    List<UserAddressResponse> addAddresses(UUID userId, List<UserAddressRequest> requests);
    List<UserAddressResponse> getAddresses(UUID userId);
    UserAddressResponse updateAddress(UUID userId, UUID addressId, UserAddressRequest request);
    void deleteAddress(UUID userId, UUID addressId);
    void setMainAddress(UUID userId, UUID addressId);
    void deleteAllAddressesByUserId(UUID userId);
}
