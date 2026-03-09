package com.project.baedalsodae.store.repository;

import com.project.baedalsodae.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    Boolean existsByBusinessNumber(String businessNumber);

    Optional<Store> findByIdAndIsDeletedIsFalse(UUID id);

    boolean existsByIdAndIsDeletedIsFalse(UUID id);
}
