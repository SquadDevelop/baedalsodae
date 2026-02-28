package com.project.baedalsodae.menu.repository;

import com.project.baedalsodae.menu.entity.MenuCategory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
  Optional<MenuCategory> findByIdAndDeletedIsFalse(UUID menuCategoryId);
}
