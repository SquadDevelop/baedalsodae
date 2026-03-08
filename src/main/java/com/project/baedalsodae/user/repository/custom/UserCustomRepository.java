package com.project.baedalsodae.user.repository.custom;

import com.project.baedalsodae.user.dto.request.UserSearchRequest;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserCustomRepository {
    Page<User> searchUsers(UserRole role, UserSearchRequest request, Pageable pageable);
}
