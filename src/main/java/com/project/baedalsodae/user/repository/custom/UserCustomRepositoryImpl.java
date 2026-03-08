package com.project.baedalsodae.user.repository.custom;

import static com.project.baedalsodae.user.entity.QUser.user;

import com.project.baedalsodae.user.dto.request.UserSearchRequest;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.repository.condition.UserSearchCondition;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl extends UserSearchCondition implements UserCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(UserRole role, UserSearchRequest request, Pageable pageable) {
        List<User> content = queryFactory
                .selectFrom(user)
                .where(
                        roleEq(role),
                        usernameEq(request.getUsername()),
                        nameEq(request.getName()),
                        isDeletedEq(request.getIsDeleted()),
                        createdAtBetween(request.getStartDate(), request.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        roleEq(role),
                        usernameEq(request.getUsername()),
                        nameEq(request.getName()),
                        isDeletedEq(request.getIsDeleted()),
                        createdAtBetween(request.getStartDate(), request.getEndDate())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
