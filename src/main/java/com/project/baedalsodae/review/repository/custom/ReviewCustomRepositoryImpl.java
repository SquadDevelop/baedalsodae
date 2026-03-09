package com.project.baedalsodae.review.repository.custom;

import static com.project.baedalsodae.order.entity.QOrder.order;
import static com.project.baedalsodae.review.entity.QReview.review;
import static com.project.baedalsodae.store.entity.QStore.store;

import com.project.baedalsodae.review.dto.query.ReviewSummary;
import com.project.baedalsodae.review.dto.response.ReviewDetailResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReviewDetailResponse> findByStoreId(UUID storeId, UUID currentUserId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                ReviewDetailResponse.class,
                                review.id,
                                review.orderId,
                                review.userId,
                                review.rating,
                                review.content,
                                currentUserId != null
                                        ? review.userId.eq(currentUserId)
                                        : Expressions.asBoolean(false)))
                .from(review)
                .join(order)
                .on(review.orderId.eq(order.id))
                .join(store)
                .on(order.storeId.eq(store.id))
                .where(store.id.eq(storeId), review.isHidden.isFalse(), review.deletedAt.isNull())
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    public ReviewSummary getSummary(UUID storeId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                ReviewSummary.class, review.rating.avg(), review.count()))
                .from(review)
                .join(order)
                .on(review.orderId.eq(order.id))
                .join(store)
                .on(order.storeId.eq(store.id))
                .where(store.id.eq(storeId))
                .fetchOne();
    }
}
