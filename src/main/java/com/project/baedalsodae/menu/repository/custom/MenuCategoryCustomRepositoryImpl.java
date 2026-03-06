package com.project.baedalsodae.menu.repository.custom;

import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryItemsResponse;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.entity.MenuCategory;
import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.*;

import static com.project.baedalsodae.menu.entity.QMenuCategory.menuCategory;
import static com.project.baedalsodae.menu.entity.QMenuItem.menuItem;

@Repository
@RequiredArgsConstructor
public class MenuCategoryCustomRepositoryImpl implements MenuCategoryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MenuCategoryItemsResponse> getStoreCategoryItems(UUID storeId) {
        List<Tuple> tuples = fetchMenuTuples(storeId);
        return groupByCategory(tuples);
    }

    private List<Tuple> fetchMenuTuples(UUID storeId) {
        return queryFactory
                .select(menuCategory, menuItem)
                .from(menuCategory)
                .join(menuCategory.menuItems, menuItem)
                .where(
                        menuCategory.store.id.eq(storeId),
                        menuCategory.isDeleted.isFalse(),
                        menuItem.isDeleted.isFalse(),
                        menuItem.menuStatus.ne(MenuStatus.HIDDEN)
                )
                .orderBy(
                        menuCategory.orderNo.asc(),
                        menuItem.orderNo.asc()
                )
                .fetch();
    }

    private List<MenuCategoryItemsResponse> groupByCategory(List<Tuple> tuples) {
        Map<MenuCategory, List<MenuItem>> grouped = new LinkedHashMap<>();
        for (Tuple tuple : tuples) {
            grouped.computeIfAbsent(tuple.get(menuCategory), k -> new ArrayList<>())
                    .add(tuple.get(menuItem));
        }
        return grouped.entrySet().stream()
                .map(entry -> MenuCategoryItemsResponse.of(
                        MenuCategoryResponseDto.fromEntity(entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }
}
