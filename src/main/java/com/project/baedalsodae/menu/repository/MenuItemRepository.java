package com.project.baedalsodae.menu.repository;

import com.project.baedalsodae.menu.entity.MenuItem;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

  @Query(
      "SELECT m FROM MenuItem m join fetch m.menuCategory WHERE m.id = :id AND m.isDeleted ="
          + " false")
  Optional<MenuItem> findByIdAndDeletedIsFalse(UUID id);

  Optional<Integer> findMaxOrderNoByMenuCategoryId(UUID menuCategoryId);

  boolean existsByMenuCategoryIdAndNameAndIsDeletedIsFalse(UUID menuCategoryId, String name);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT i FROM MenuItem i WHERE i.menuCategory.id = :menuCategoryId AND i.isDeleted = false ORDER BY i.orderNo ASC")
  List<MenuItem> findAllByMenuCategoryIdAndIsDeletedIsFalseForUpdate(UUID menuCategoryId);
}
