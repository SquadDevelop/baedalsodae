package com.project.baedalsodae.user.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UserRequestDto;
import com.project.baedalsodae.user.dto.response.UserResponseDto.Delete;
import com.project.baedalsodae.user.dto.response.UserResponseDto.Detail;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.UserAddressService;
import com.project.baedalsodae.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAddressService userAddressService;

    @Transactional
    @Override
    public Detail createUser(UserRequestDto.Create createRequest) {
        String encodedPassword = passwordEncoder.encode(createRequest.getPassword());

        User newUser = User.create(
                createRequest.getUsername(),
                createRequest.getPhone(),
                createRequest.getEmail(),
                encodedPassword,
                createRequest.getName(),
                createRequest.getNickname(),
                createRequest.getRole()
        );

        User savedUser = userRepository.save(newUser);
        CreateUserAddressRequest addressRequest = CreateUserAddressRequest.from(
                createRequest.getRoadAddress(),
                createRequest.getDetailAddress(),
                createRequest.getDescription()
        );
        userAddressService.createAddress(savedUser.getId(), addressRequest);

        return Detail.from(savedUser);
    }

    @Transactional(readOnly = true)
    @Override
    public Detail getUser(UUID userId) {
        User user = findByUserId(userId);

        return Detail.from(user);
    }

    @Transactional
    @Override
    public Detail updateUser(UUID userId, UserRequestDto.Update updateRequest) {
        User user = findByUserId(userId);

        String encodedPassword = user.getPassword();
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(updateRequest.getPassword());
        }

        user.update(
            updateRequest.getPhone(),
            updateRequest.getEmail(),
            encodedPassword,
            updateRequest.getNickname()
        );

        if (updateRequest.getAddresses() != null) {
            userAddressService.updateAddressList(userId, updateRequest.getAddresses());
        }

        return Detail.from(user);
    }

    @Transactional
    @Override
    public Delete deleteUser(UUID userId) {
        User user = findByUserId(userId);

        userAddressService.deleteAllAddressesByUserId(userId);
        user.softDelete(userId);

        return Delete.from(user);
    }

    private User findByUserId(UUID userId) {
        return userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
