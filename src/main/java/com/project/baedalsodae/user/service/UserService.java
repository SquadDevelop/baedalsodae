package com.project.baedalsodae.user.service;

import com.project.baedalsodae.user.dto.request.UserRequestDto;
import com.project.baedalsodae.user.dto.response.UserResponseDto;
import java.util.UUID;

public interface UserService {

    UserResponseDto.Detail createUser(UserRequestDto.Create createRequest);

    UserResponseDto.Detail getUser(UUID userId);

    UserResponseDto.Detail updateUser(UUID userId, UserRequestDto.Update updateRequest);

    UserResponseDto.Delete deleteUser(UUID userId);
}
