package com.project.baedalsodae.user.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.dto.request.UserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.repository.UserAddressRepository;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public List<UserAddressResponse> addAddresses(UUID userId, List<UserAddressRequest> requests) {
        User foundUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<UserAddress> addresses = requests.stream()
                .map(request -> UserAddress.create(
                        foundUser,
                        request.getRoadAddress(),
                        request.getDetailAddress(),
                        request.getDescription()
                )).toList();

        List<UserAddress> savedAddresses = userAddressRepository.saveAll(addresses);
        foundUser.addAddresses(savedAddresses);
        if (foundUser.getUserMainAddressId() == null && !savedAddresses.isEmpty()) {
            foundUser.changeMainAddress(savedAddresses.get(0).getId());
        }

        UUID mainAddressId = foundUser.getUserMainAddressId();
        return savedAddresses.stream()
                .map(userAddress -> UserAddressResponse.from(userAddress, mainAddressId))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserAddressResponse> getAddresses(UUID userId) {
        User foundUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UUID mainUserAddressId = foundUser.getUserMainAddressId();

        List<UserAddress> addresses = userAddressRepository.findAllByUserIdWithUser(userId);
        if (addresses.isEmpty()) {
            return Collections.emptyList();
        }
        return addresses.stream()
                .map(userAddress -> UserAddressResponse.from(userAddress, mainUserAddressId))
                .toList();
    }

    @Transactional
    @Override
    public UserAddressResponse updateAddress(UUID userId, UUID addressId, UserAddressRequest request) {
        UserAddress addressToUpdate = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToUpdate.getUser();

        addressToUpdate.update(
                request.getRoadAddress(),
                request.getDetailAddress(),
                request.getDescription()
        );

        return UserAddressResponse.from(addressToUpdate, user.getUserMainAddressId());
    }

    @Transactional
    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress addressToDelete = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToDelete.getUser();

        if (user.getUserAddresses().size() <= 1) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_LAST_ADDRESS);
        }

        if (user.getUserMainAddressId() != null && user.getUserMainAddressId().equals(addressId)) {
            UUID nextMainAddressId = user.getUserAddresses().stream()
                    .map(UserAddress::getId)
                    .filter(id -> !addressId.equals(id))
                    .findAny()
                    .orElse(null);
            user.changeMainAddress(nextMainAddressId);
        }

        userAddressRepository.delete(addressToDelete);
        user.getUserAddresses().remove(addressToDelete);
    }

    @Transactional
    @Override
    public void setMainAddress(UUID userId, UUID addressId) {
        UserAddress addressToSetMain = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToSetMain.getUser();

        user.changeMainAddress(addressToSetMain.getId());
    }

    @Transactional
    @Override
    public void deleteAllAddressesByUserId(UUID userId) {
        User user = userRepository.findUserWithAddressesById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserAddresses().isEmpty()) {
            return;
        }
        userAddressRepository.deleteAllByUserId(userId);

        user.changeMainAddress(null);
        user.getUserAddresses().clear();
    }
}
