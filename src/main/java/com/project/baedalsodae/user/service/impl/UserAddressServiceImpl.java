package com.project.baedalsodae.user.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.repository.UserAddressRepository;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.UserAddressService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        if (userAddressRepository.existsByUserIdAndAddressRoadAddressAndAddressDetailAddress(
                userId, request.getRoadAddress(), request.getDetailAddress())) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_DUPLICATED);
        }

        User foundUser =
                userRepository
                        .findByUserIdAndIsDeletedFalse(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Address address = Address.createAddress(
                request.getSidoCode(), request.getSidoName(),
                request.getSigunguCode(), request.getSigunguName(),
                request.getDongCode(), request.getDongName(),
                request.getRoadAddress(), request.getDetailAddress());

        UserAddress userAddress = UserAddress.create(foundUser, address, request.getDescription());

        foundUser.addAddress(userAddress);
        UserAddress savedAddress = userAddressRepository.save(userAddress);

        if (foundUser.getUserMainAddressId() == null) {
            foundUser.changeMainAddress(savedAddress.getId());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserAddressResponse getMainAddress(UUID userId) {
        User foundUser = userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserAddress mainAddress = foundUser.getMainAddress();

        if (mainAddress == null) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND);
        }

        return UserAddressResponse.from(mainAddress, mainAddress.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserAddressResponse> getAddressList(UUID userId) {
        User foundUser =
                userRepository
                        .findByUserIdAndIsDeletedFalse(userId)
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
    public UserAddressResponse updateAddress(UUID userId, UUID addressId, UpdateUserAddressRequest request) {
        if (userAddressRepository.existsByUserIdAndAddressRoadAddressAndAddressDetailAddressAndIdNot(
                userId, request.getRoadAddress(), request.getDetailAddress(), addressId)) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_DUPLICATED);
        }

        UserAddress addressToUpdate =
                userAddressRepository
                        .findByIdAndUserId(addressId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        
        Address address = Address.createAddress(
                request.getSidoCode(), request.getSidoName(),
                request.getSigunguCode(), request.getSigunguName(),
                request.getDongCode(), request.getDongName(),
                request.getRoadAddress(), request.getDetailAddress());

        addressToUpdate.update(addressToUpdate.getId(), address, request.getDescription());

        return UserAddressResponse.from(addressToUpdate, addressToUpdate.getUser().getUserMainAddressId());
    }

    @Transactional
    @Override
    public void updateAddressList(UUID userId, List<UpdateUserAddressRequest> updatedAddressReq) {
        User user =
                userRepository
                        .findUserWithAddressesByIdAndIsDeletedFalse(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Map<UUID, UserAddress> existingAddressMap = user.getUserAddresses().stream()
                .collect(Collectors.toMap(UserAddress::getId, a -> a));

        List<UserAddress> targetAddresses = updatedAddressReq.stream()
                .map(req -> {
                    Address address = Address.createAddress(
                            req.getSidoCode(), req.getSidoName(),
                            req.getSigunguCode(), req.getSigunguName(),
                            req.getDongCode(), req.getDongName(),
                            req.getRoadAddress(), req.getDetailAddress());

                    if (req.getUserAddressId() != null && existingAddressMap.containsKey(req.getUserAddressId())) {
                        UserAddress existing = existingAddressMap.get(req.getUserAddressId());
                        existing.update(existing.getId(), address, req.getDescription());
                        return existing;
                    } else {
                        return UserAddress.create(user, address, req.getDescription());
                    }
                })
                .toList();

        user.updateAddresses(targetAddresses);
    }

    @Transactional
    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress addressToDelete =
                userAddressRepository
                        .findByIdAndUserId(addressId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToDelete.getUser();

        if (user.getUserAddresses().size() <= 1) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_CANNOT_DELETE);
        }

        if (addressId.equals(user.getUserMainAddressId())) {
            UUID nextMainAddressId =
                    user.getUserAddresses().stream()
                            .map(UserAddress::getId)
                            .filter(id -> !addressId.equals(id))
                            .findFirst()
                            .orElse(null);
            user.changeMainAddress(nextMainAddressId);
        }

        user.getUserAddresses().remove(addressToDelete);
        userAddressRepository.delete(addressToDelete);
    }

    @Transactional
    @Override
    public void setMainAddress(UUID userId, UUID addressId) {
        UserAddress addressToSetMain =
                userAddressRepository
                        .findByIdAndUserId(addressId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND));
        User user = addressToSetMain.getUser();

        user.changeMainAddress(addressToSetMain.getId());
    }

    @Transactional
    @Override
    public void deleteAllAddressesByUserId(UUID userId) {
        User user =
                userRepository
                        .findUserWithAddressesByIdAndIsDeletedFalse(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserAddresses().isEmpty()) {
            return;
        }
        userAddressRepository.deleteAllByUserId(userId);

        user.changeMainAddress(null);
        user.getUserAddresses().clear();
    }
}
