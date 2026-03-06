package com.project.baedalsodae.payment.repository;

import com.project.baedalsodae.payment.entity.Payment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query(
            """
            SELECT p
            FROM Payment p
            WHERE (:cursor IS NULL OR p.createdAt < :cursor)
            ORDER BY p.createdAt DESC
            """)
    List<Payment> findNextPage(Instant cursor, int size);

    boolean existsByOrderId(UUID orderId);
}
