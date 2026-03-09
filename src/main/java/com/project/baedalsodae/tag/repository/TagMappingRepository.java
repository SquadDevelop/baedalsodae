package com.project.baedalsodae.tag.repository;

import com.project.baedalsodae.tag.entity.TagMapping;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagMappingRepository extends JpaRepository<TagMapping, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TagMapping tm WHERE tm.menuItem.id = :menuItemId")
    void deleteByMenuItemId(UUID menuItemId);

    interface TagNameProjection {
        UUID getMenuItemId();

        String getTagName();
    }

    @Query(
            "SELECT tm.menuItem.id AS menuItemId, tm.tag.name AS tagName FROM TagMapping tm WHERE tm.menuItem.id IN :menuItemIds ORDER BY tm.menuItem.id, tm.orderNo")
    List<TagNameProjection> findTagDataByMenuItemIds(Collection<UUID> menuItemIds);
}
