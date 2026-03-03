package com.project.baedalsodae.user.repository;

import com.project.baedalsodae.user.entity.UserAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    @Query("SELECT ua FROM UserAddress ua JOIN FETCH User u WHERE u.id = :userId")
    List<UserAddress> findAllByUserIdWithUser(@Param("userId") UUID userId);

    Optional<UserAddress> findByIdAndUserId(UUID addressId, UUID userId);

    void deleteAllByUserId(UUID uuid);
}
