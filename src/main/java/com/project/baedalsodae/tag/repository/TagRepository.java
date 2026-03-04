package com.project.baedalsodae.tag.repository;

import com.project.baedalsodae.tag.entity.Tag;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findAllByNameIn(Collection<String> names);

    @Query(
            value =
                    """
                SELECT t.* FROM p_tag t
                LEFT JOIN p_tag_mapping tm ON t.id = tm.tag_id
                GROUP BY t.id
                ORDER BY COUNT(tm.id) DESC
                LIMIT :count
                """,
            nativeQuery = true)
    List<Tag> findTopByMappingCount(int count);

    @Query(
            value =
                    """
                SELECT t.* FROM p_tag t
                LEFT JOIN p_tag_mapping tm ON t.id = tm.tag_id
                WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                GROUP BY t.id
                ORDER BY
                    CASE WHEN LOWER(t.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END,
                    COUNT(tm.id) DESC
                LIMIT :count
                """,
            nativeQuery = true)
    List<Tag> findByKeywordOrderByRelevance(String keyword, int count);
}
