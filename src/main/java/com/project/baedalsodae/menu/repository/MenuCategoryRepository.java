package com.project.baedalsodae.menu.repository;

import com.project.baedalsodae.menu.entity.MenuCategory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    @Query(
            "SELECT c FROM MenuCategory c join fetch c.store WHERE c.id = :id AND c.isDeleted = false")
    Optional<MenuCategory> findByIdAndDeletedIsFalse(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "SELECT c FROM MenuCategory c WHERE c.store.id = :storeId AND c.isDeleted = false ORDER BY c.orderNo ASC")
    List<MenuCategory> findAllByStoreIdAndDeletedIsFalseWithLock(UUID storeId);

    @Query(
            "SELECT MAX(c.orderNo) FROM MenuCategory c WHERE c.store.id = :storeId AND c.isDeleted = false")
    Optional<Integer> findMaxOrderNoByStoreIdAndDeletedIsFalse(UUID storeId);

    @Query(
            "SELECT c FROM MenuCategory c WHERE c.store.id = :storeId AND c.isDeleted = false ORDER BY c.orderNo ASC")
    List<MenuCategory> findAllByStoreIdAndDeletedIsFalse(UUID storeId);

    @Query(
            "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM MenuCategory c WHERE c.store.id = :storeId AND c.name = :name AND c.isDeleted = false")
    boolean existsByStoreIdAndNameAndDeletedIsFalse(UUID storeId, String name);
}
