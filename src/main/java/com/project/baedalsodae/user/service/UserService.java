package com.project.baedalsodae.user.service;

import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.response.UserDeleteResponse;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDetailResponse createUser(CreateUserRequest createRequest);

    UserDetailResponse getUser(UUID userId);

    UserDetailResponse updateUser(UUID userId, UpdateUserRequest updateRequest);

    UserDeleteResponse deleteUser(UUID userId);

    Page<UserDetailResponse> getUsers(UserRole role, String username, Pageable pageable);
}
