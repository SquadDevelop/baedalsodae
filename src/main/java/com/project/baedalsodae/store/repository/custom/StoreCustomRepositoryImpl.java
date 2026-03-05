package com.project.baedalsodae.store.repository.custom;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.project.baedalsodae.store.entity.QStore.store;

@Repository
@RequiredArgsConstructor
public class StoreCustomRepositoryImpl implements StoreCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Store> findStoresByCursor(UUID storeCategoryId, StoreCursorRequest cursor, SortType sortType) {
        NumberExpression<Integer> statusOrder = new CaseBuilder()
                .when(store.storeStatus.eq(StoreStatus.OPEN)).then(1)
                .otherwise(0);

        List<Store> content = queryFactory
                .selectFrom(store)
                .where(
                        store.storeCategory.id.eq(storeCategoryId),
                        cursor.lastId() != null ? cursorCondition(cursor, sortType) : null
                )
                .orderBy(
                        statusOrder.desc(),
                        orderSpecifier(sortType),
                        store.id.desc()
                )
                .limit(cursor.getSize() + 1)
                .fetch();

        boolean hasNext = content.size() > cursor.getSize();
        if (hasNext) {
            content.remove(cursor.getSize());
        }

        return new SliceImpl<>(content, PageRequest.ofSize(cursor.getSize()), hasNext);
    }

    private BooleanExpression cursorCondition(StoreCursorRequest cursor, SortType sortType) {
        return switch (sortType) {
            case LATEST -> store.createdAt.lt(cursor.lastCreatedAt())
                    .or(store.createdAt.eq(cursor.lastCreatedAt())
                            .and(store.id.lt(cursor.lastId())));
            case RATING -> store.avgRating.lt(cursor.lastRating())
                    .or(store.avgRating.eq(cursor.lastRating())
                            .and(store.id.lt(cursor.lastId())));
            case REVIEW -> store.reviewCount.lt(cursor.lastReviewCount())
                    .or(store.reviewCount.eq(cursor.lastReviewCount())
                            .and(store.id.lt(cursor.lastId())));
        };
    }

    private OrderSpecifier<?> orderSpecifier(SortType sortType) {
        return switch (sortType) {
            case LATEST -> store.createdAt.desc();
            case RATING -> store.avgRating.desc();
            case REVIEW -> store.reviewCount.desc();
        };
    }
}