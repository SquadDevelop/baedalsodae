package com.project.baedalsodae.allowedRegion.repository;

import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import com.project.baedalsodae.allowedRegion.repository.custom.AllowedRegionCustomRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllowedRegionRepository
        extends JpaRepository<AllowedRegion, UUID>, AllowedRegionCustomRepository {
    Optional<AllowedRegion> findBySigunguCodeAndIsDeletedIsFalse(String sigunguCode);

    Optional<AllowedRegion> findBySigunguNameAndIsDeletedIsFalse(String sigunguName);

    Optional<AllowedRegion> findByIdAndIsDeletedIsFalse(UUID id);

    boolean existsBySigunguCodeAndIsDeletedIsFalse(String sigunguCode);
}
