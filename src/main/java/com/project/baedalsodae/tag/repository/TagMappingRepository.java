package com.project.baedalsodae.tag.repository;

import com.project.baedalsodae.tag.entity.TagMapping;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagMappingRepository extends JpaRepository<TagMapping, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TagMapping tm WHERE tm.menuItem.id = :menuItemId")
    void deleteByMenuItemId(UUID menuItemId);
}
