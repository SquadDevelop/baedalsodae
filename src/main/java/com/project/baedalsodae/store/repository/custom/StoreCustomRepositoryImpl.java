package com.project.baedalsodae.store.repository.custom;

import static com.project.baedalsodae.menu.entity.QMenuCategory.menuCategory;
import static com.project.baedalsodae.menu.entity.QMenuItem.menuItem;
import static com.project.baedalsodae.store.entity.QStore.store;
import static com.project.baedalsodae.store.entity.QStoreCategory.storeCategory;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreCustomRepositoryImpl implements StoreCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Store> findStoresByCursor(
            UUID storeCategoryId, StoreCursorRequest cursor, SortType sortType) {
        NumberExpression<Integer> statusOrder =
                new CaseBuilder().when(store.storeStatus.eq(StoreStatus.OPEN)).then(1).otherwise(0);

        List<Store> content =
                queryFactory
                        .selectFrom(store)
                        .where(
                                store.storeCategory.id.eq(storeCategoryId),
                                cursor.lastId() != null ? cursorCondition(cursor, sortType) : null)
                        .orderBy(statusOrder.desc(), orderSpecifier(sortType), store.id.desc())
                        .limit(cursor.getSize() + 1)
                        .fetch();

        boolean hasNext = content.size() > cursor.getSize();
        if (hasNext) {
            content.remove(cursor.getSize());
        }

        return new SliceImpl<>(content, PageRequest.ofSize(cursor.getSize()), hasNext);
    }

    @Override
    public List<Store> searchStoreByKeyword(String keyword, Pageable pageable, SortType sortType) {
        return queryFactory
                .selectDistinct(store)
                .from(store)
                .join(store.storeCategory, storeCategory)
                .fetchJoin()
                .leftJoin(menuCategory)
                .on(menuCategory.store.id.eq(store.id))
                .leftJoin(menuItem)
                .on(menuItem.menuCategory.id.eq(menuCategory.id))
                .where(keywordCondition(keyword))
                .orderBy(orderSpecifier(sortType), store.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
    }

    @Override
    public Long countStoresByKeyword(String keyword) {
        return Optional.ofNullable(
                        queryFactory
                                .select(store.countDistinct())
                                .from(store)
                                .join(store.storeCategory, storeCategory)
                                .leftJoin(menuCategory)
                                .on(menuCategory.store.id.eq(store.id))
                                .leftJoin(menuItem)
                                .on(menuItem.menuCategory.id.eq(menuCategory.id))
                                .where(keywordCondition(keyword))
                                .fetchOne())
                .orElse(0L);
    }

    private BooleanExpression keywordCondition(String keyword) {
        String likeKeyword = "%" + keyword + "%";

        return store.name
                .like(likeKeyword)
                .or(store.storeCategory.name.like(likeKeyword))
                .or(menuCategory.name.like(likeKeyword))
                .or(menuItem.name.like(likeKeyword));
    }

    private BooleanExpression cursorCondition(StoreCursorRequest cursor, SortType sortType) {
        return switch (sortType) {
            case LATEST ->
                    store.createdAt
                            .lt(cursor.lastCreatedAt())
                            .or(
                                    store.createdAt
                                            .eq(cursor.lastCreatedAt())
                                            .and(store.id.lt(cursor.lastId())));
            case RATING ->
                    store.avgRating
                            .lt(cursor.lastRating())
                            .or(
                                    store.avgRating
                                            .eq(cursor.lastRating())
                                            .and(store.id.lt(cursor.lastId())));
            case REVIEW ->
                    store.reviewCount
                            .lt(cursor.lastReviewCount())
                            .or(
                                    store.reviewCount
                                            .eq(cursor.lastReviewCount())
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
