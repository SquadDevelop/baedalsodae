package com.project.baedalsodae.allowedRegion.repository.custom;

import static com.project.baedalsodae.allowedRegion.entity.QAllowedRegion.allowedRegion;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionCursorRequest;
import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import com.project.baedalsodae.allowedRegion.entity.enums.AllowedRegionSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AllowedRegionCustomRepositoryImpl implements AllowedRegionCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<AllowedRegion> findAllowedRegionsByCursor(AllowedRegionCursorRequest cursor) {
        AllowedRegionSortType sortType =
                cursor.sortType() == null ? AllowedRegionSortType.ACTIVE : cursor.sortType();

        List<AllowedRegion> content =
                queryFactory
                        .selectFrom(allowedRegion)
                        .where(
                                allowedRegion.isDeleted.isFalse(),
                                sidoCodeFilter(cursor.sidoCode()),
                                isActiveFilter(cursor.activeFilter()),
                                cursor.lastId() != null ? cursorCondition(cursor, sortType) : null)
                        .orderBy(orderSpecifiers(sortType))
                        .limit(cursor.getSize() + 1)
                        .fetch();

        boolean hasNext = content.size() > cursor.getSize();
        if (hasNext) {
            content.remove(cursor.getSize());
        }

        return new SliceImpl<>(content, PageRequest.ofSize(cursor.getSize()), hasNext);
    }

    private BooleanExpression sidoCodeFilter(String sidoCode) {
        return sidoCode != null ? allowedRegion.sidoCode.eq(sidoCode) : null;
    }

    private BooleanExpression isActiveFilter(Boolean activeFilter) {
        return activeFilter != null ? allowedRegion.isActive.eq(activeFilter) : null;
    }

    private BooleanExpression cursorCondition(
            AllowedRegionCursorRequest cursor, AllowedRegionSortType sortType) {
        return switch (sortType) {
            case LATEST ->
                    allowedRegion
                            .createdAt
                            .lt(cursor.lastCreatedAt())
                            .or(
                                    allowedRegion
                                            .createdAt
                                            .eq(cursor.lastCreatedAt())
                                            .and(allowedRegion.id.lt(cursor.lastId())));
            case SIDO_NAME ->
                    allowedRegion
                            .sidoName
                            .gt(cursor.lastSidoName())
                            .or(
                                    allowedRegion
                                            .sidoName
                                            .eq(cursor.lastSidoName())
                                            .and(
                                                    allowedRegion.sigunguName.gt(
                                                            cursor.lastSigunguName())))
                            .or(
                                    allowedRegion
                                            .sidoName
                                            .eq(cursor.lastSidoName())
                                            .and(
                                                    allowedRegion.sigunguName.eq(
                                                            cursor.lastSigunguName()))
                                            .and(allowedRegion.id.gt(cursor.lastId())));
            case ACTIVE -> activeCreatedAtCursor(cursor);
        };
    }

    private BooleanExpression activeCreatedAtCursor(AllowedRegionCursorRequest cursor) {
        BooleanExpression sameGroup =
                allowedRegion
                        .isActive
                        .eq(cursor.lastIsActive())
                        .and(
                                allowedRegion
                                        .createdAt
                                        .lt(cursor.lastCreatedAt())
                                        .or(
                                                allowedRegion
                                                        .createdAt
                                                        .eq(cursor.lastCreatedAt())
                                                        .and(
                                                                allowedRegion.id.lt(
                                                                        cursor.lastId()))));
        BooleanExpression crossBoundary =
                Boolean.TRUE.equals(cursor.lastIsActive()) && cursor.activeFilter() == null
                        ? allowedRegion.isActive.isFalse()
                        : null;

        return crossBoundary != null ? crossBoundary.or(sameGroup) : sameGroup;
    }

    private OrderSpecifier<?>[] orderSpecifiers(AllowedRegionSortType sortType) {
        return switch (sortType) {
            case LATEST ->
                    new OrderSpecifier<?>[] {
                        allowedRegion.createdAt.desc(), allowedRegion.id.desc()
                    };
            case SIDO_NAME ->
                    new OrderSpecifier<?>[] {
                        allowedRegion.sidoName.asc(),
                        allowedRegion.sigunguName.asc(),
                        allowedRegion.id.asc()
                    };
            case ACTIVE ->
                    new OrderSpecifier<?>[] {
                        allowedRegion.isActive.desc(),
                        allowedRegion.createdAt.desc(),
                        allowedRegion.id.desc()
                    };
        };
    }
}
