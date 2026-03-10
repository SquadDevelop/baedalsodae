package com.project.baedalsodae.payment.pg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import jakarta.persistence.EntityListeners;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PGPaymentResponse {
	private boolean success;
	private String paymentId;
	private String pgTransactionId;
	private long amount;
	private PaymentStatus status;
	private String message;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@CreationTimestamp
	private LocalDateTime createdAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@LastModifiedDate
	private LocalDateTime modifiedAt;
}
