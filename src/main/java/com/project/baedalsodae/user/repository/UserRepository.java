package com.project.baedalsodae.user.repository;

import com.project.baedalsodae.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.isDeleted = false")
    Optional<User> findByUserId(@Param("userId") UUID userId);

    Optional<User> findUserWithAddressesById(UUID userId);
}
