package com.project.baedalsodae.cart.repository;

import com.project.baedalsodae.cart.entity.Cart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    @Query(
            "select c from Cart c "
                    + "left join fetch c.items i "
                    + "left join fetch i.menuItem "
                    + "where c.userId = :userId")
    Optional<Cart> findCartWithItemsByUserId(@Param("userId") UUID userId);

    @Query(
            "select c from Cart c "
                    + "left join fetch c.items i "
                    + "left join fetch i.menuItem "
                    + "where c.id = :cartId and c.userId = :userId")
    Optional<Cart> findCartWithItemsByIdAndUserId(
            @Param("cartId") UUID cartId, @Param("userId") UUID userId);
}
