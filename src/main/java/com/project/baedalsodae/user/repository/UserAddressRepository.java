package com.project.baedalsodae.user.repository;

import com.project.baedalsodae.user.entity.UserAddress;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    boolean existsByUserIdAndAddressRoadAddressAndAddressDetailAddress(
            UUID userId, String roadAddress, String detailAddress);

    boolean existsByUserIdAndAddressRoadAddressAndAddressDetailAddressAndIdNot(
            UUID userId, String roadAddress, String detailAddress, UUID addressId);

    Optional<UserAddress> findByIdAndUserId(UUID addressId, UUID userId);

    void deleteAllByUserId(UUID uuid);
}
