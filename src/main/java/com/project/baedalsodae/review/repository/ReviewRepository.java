package com.project.baedalsodae.review.repository;

import com.project.baedalsodae.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query(
        """
        SELECT r
        FROM Review r
        WHERE (:cursor IS NULL OR r.createdAt < :cursor)
        AND r.userId = :userId
        ORDER BY r.createdAt DESC
        """)
    List<Review> findNextPageWithUserId(UUID userId, Instant cursor, int size);
}
