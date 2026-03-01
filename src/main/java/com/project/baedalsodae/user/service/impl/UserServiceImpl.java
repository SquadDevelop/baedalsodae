package com.project.baedalsodae.user.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.dto.UserRequestDto;
import com.project.baedalsodae.user.dto.UserResponseDto.Delete;
import com.project.baedalsodae.user.dto.UserResponseDto.Detail;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.repository.UserRepository;
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

    @Transactional
    @Override
    public Detail createUser(UserRequestDto.Create createRequest) {
        String encodedPassword = passwordEncoder.encode(createRequest.getPassword());
        User newUser = userRepository.save(createRequest.toEntity(encodedPassword));

        return Detail.from(newUser);
    }

    @Transactional(readOnly = true)
    @Override
    public Detail getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return Detail.from(user);
    }

    @Transactional
    @Override
    public Detail updateUser(UUID userId, UserRequestDto.Update updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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

        return Detail.from(user);
    }

    @Transactional
    @Override
    public Delete deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.softDelete(userId);

        return Delete.from(user);
    }
}
