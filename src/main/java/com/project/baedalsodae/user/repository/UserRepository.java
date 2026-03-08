package com.project.baedalsodae.user.repository;

import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.isDeleted = false")
    Optional<User> findByUserIdAndIsDeletedFalse(@Param("userId") UUID userId);

    @Query(
            "SELECT u FROM User u LEFT JOIN FETCH u.userAddresses WHERE u.id = :userId AND u.isDeleted = false")
    Optional<User> findUserWithAddressesByIdAndIsDeletedFalse(@Param("userId") UUID userId);

    Page<User> findAllByRoleAndIsDeletedFalse(UserRole role, Pageable pageable);

    // 검색 조건을 위한 메서드 추가 (예: username 검색)
    Page<User> findAllByRoleAndUsernameContainingAndIsDeletedFalse(UserRole role, String username, Pageable pageable);
}
