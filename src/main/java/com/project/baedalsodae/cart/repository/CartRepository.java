package com.project.baedalsodae.cart.repository;

import com.project.baedalsodae.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
	@Query("select c from Cart c " +
			"left join fetch c.items i " +
			"left join fetch i.menuItem " +
			"where c.userId = :userId")
	Optional<Cart> findCartWithItemsByUserId(@Param("userId") UUID userId);

	@Query("select c from Cart c " +
			"left join fetch c.items i " +
			"left join fetch i.menuItem " +
			"where c.userId = :userId and c.id = :cartId")
	Optional<Cart> findCartWithItemsByUserIdAndCartId(@Param("userId") UUID userId, @Param("cartId") UUID cartId);

}
