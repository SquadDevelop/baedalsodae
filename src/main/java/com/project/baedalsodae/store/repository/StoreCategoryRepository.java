package com.project.baedalsodae.store.repository;

import com.project.baedalsodae.store.entity.StoreCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, UUID> {
    List<StoreCategory> findAllByIsDeletedFalse();
}
