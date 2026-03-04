package com.project.baedalsodae.user.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.repository.UserAddressRepository;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
    public void createAddress(UUID userId, CreateUserAddressRequest request) {
        if (userAddressRepository.existsByRoadAddressAndDetailAddress(request.getRoadAddress(), request.getDetailAddress())) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_DUPLICATED);
        }

        User foundUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserAddress address = UserAddress.create(
                foundUser,
                request.getRoadAddress(),
                request.getDetailAddress(),
                request.getDescription()
        );

        foundUser.addAddress(address);
        UserAddress savedAddress = userAddressRepository.save(address);

        if (foundUser.getUserMainAddressId() == null) {
            foundUser.changeMainAddress(savedAddress.getId());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserAddressResponse> getAddressList(UUID userId) {
        User foundUser = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UUID mainUserAddressId = foundUser.getUserMainAddressId();

        List<UserAddress> addresses = foundUser.getUserAddresses();
        if (addresses.isEmpty()) {
            return Collections.emptyList();
        }
        return addresses.stream()
                .map(userAddress -> UserAddressResponse.from(userAddress, mainUserAddressId))
                .toList();
    }

    @Transactional
    @Override
    public UserAddressResponse updateAddress(UUID userId, UpdateUserAddressRequest request) {
        if (userAddressRepository.existsByRoadAddressAndDetailAddressAndIdNot(
                request.getRoadAddress(), request.getDetailAddress(), request.getUserAddressId())) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_DUPLICATED);
        }

        UserAddress addressToUpdate = userAddressRepository.findByIdAndUserId(request.getUserAddressId(), userId)
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
    public void updateAddressList(UUID userId, List<UpdateUserAddressRequest> updatedAddressReq) {
        User user = userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<UUID> userAddressIds = user.getUserAddresses().stream()
                .map(UserAddress::getId)
                .collect(Collectors.toSet());

        List<UserAddress> newAddresses = updatedAddressReq.stream()
                .filter(req -> req.getUserAddressId() == null || userAddressIds.contains(req.getUserAddressId())) // 타인의 주소 ID는 무시
                .map(req -> {
                    UserAddress userAddress = UserAddress.builder()
                            .id(req.getUserAddressId())
                            .user(user)
                            .build();
                    userAddress.update(req.getRoadAddress(), req.getDetailAddress(), req.getDescription());
                    return userAddress;
                })
                .toList();

        user.updateAddresses(newAddresses);
    }

    @Transactional
    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress addressToDelete = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToDelete.getUser();

        if (user.getUserAddresses().size() <= 1) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_CANNOT_DELETE);
        }

        UserAddress mainAddress = user.getMainAddress();
        if (mainAddress != null && addressId.equals(mainAddress.getId())) {
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
    public void setMainAddress(UUID userId, UUID newMainAddressId) {
        UserAddress addressToSetMain = userAddressRepository.findByIdAndUserId(newMainAddressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToSetMain.getUser();

        user.changeMainAddress(addressToSetMain.getId());
    }

    @Transactional
    @Override
    public void deleteAllAddressesByUserId(UUID userId) {
        User user = userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserAddresses().isEmpty()) {
            return;
        }
        userAddressRepository.deleteAllByUserId(userId);

        user.changeMainAddress(null);
        user.getUserAddresses().clear();
    }
}
