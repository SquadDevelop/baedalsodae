package com.project.baedalsodae.store.repository;

import com.project.baedalsodae.store.entity.StoreHours;
import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreHoursRepository extends JpaRepository<StoreHours, UUID> {

    List<StoreHours> findAllByStoreId(UUID storeId);
    boolean existsByStoreId(UUID storeId);

    void deleteAllByStoreId(UUID storeId);
}
