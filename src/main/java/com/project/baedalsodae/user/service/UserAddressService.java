package com.project.baedalsodae.user.service;

import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.UserAddress;
import java.util.List;
import java.util.UUID;

public interface UserAddressService {

    void createAddress(UUID userId, CreateUserAddressRequest request);

    List<UserAddressResponse> getAddressList(UUID userId);

    UserAddressResponse getMainAddress(UUID userId);

    UserAddressResponse updateAddress(
            UUID userId, UUID addressId, UpdateUserAddressRequest request);

    void updateAddressList(UUID userId, List<UpdateUserAddressRequest> addresses);

    void deleteAddress(UUID userId, UUID addressId);

    void deleteAllAddressesByUserId(UUID userId);

    void setMainAddress(UUID userId, UUID addressId);

    UserAddress getMainUserAddress(UUID userId);
}
