package com.project.baedalsodae.cart.entity;

import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
	name = "p_cart",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_cart_user_store", columnNames = {"user_id", "store_id"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "store_id", nullable = false)
	private UUID storeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private CartStatus status;


}
