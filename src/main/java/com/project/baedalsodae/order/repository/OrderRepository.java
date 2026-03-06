package com.project.baedalsodae.order.repository;

import com.project.baedalsodae.order.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndIsDeletedFalse(UUID orderId);

    @Query(
            """
			    select o from Order o
			    join fetch o.items oi
			    where o.id = :orderId
			    and o.isDeleted = false
			""")
    Optional<Order> findOrderWithItemsById(@Param("orderId") UUID orderId);
}
