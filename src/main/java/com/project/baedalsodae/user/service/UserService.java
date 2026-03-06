package com.project.baedalsodae.user.service;

import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.response.UserDeleteResponse;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;

import java.util.UUID;

public interface UserService {

    UserDetailResponse createUser(CreateUserRequest createRequest);

    UserDetailResponse getUser(UUID userId);

    UserDetailResponse updateUser(UUID userId, UpdateUserRequest updateRequest);

    UserDeleteResponse deleteUser(UUID userId);
}
