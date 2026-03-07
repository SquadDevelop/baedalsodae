package com.project.baedalsodae.review.entity;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.entity.BaseAuditEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private UUID orderId;
    private int rating;
    private String content;
    private boolean isHidden;

    private Review(UUID userId, UUID orderId, int rating, String content) {
        validate(rating, content);
        this.userId = userId;
        this.orderId = orderId;
        this.rating = rating;
        this.content = content;
        this.isHidden = false;
    }

    private void validate(int rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.RATING_OUT_OF_RANGE);
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.REVIEW_CONTENT_EMPTY);
        }
    }

    public void update(int rating, String content) {
        validate(rating, content);
        this.rating = rating;
        this.content = content;
    }

    public static Review create(UUID userId, UUID orderId, int rating, String content) {
        return new Review(userId, orderId, rating, content);
    }

}
