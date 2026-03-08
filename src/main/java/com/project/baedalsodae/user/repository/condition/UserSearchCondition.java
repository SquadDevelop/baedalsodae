package com.project.baedalsodae.user.repository.condition;

import com.project.baedalsodae.global.common.util.TimeUtils;
import com.project.baedalsodae.user.entity.UserRole;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import static com.project.baedalsodae.user.entity.QUser.user;

public class UserSearchCondition {

	protected BooleanExpression roleEq(UserRole role) {
		return role != null ? user.role.eq(role) : null;
	}

	protected BooleanExpression usernameEq(String username) {
		return StringUtils.hasText(username) ? user.username.contains(username) : null;
	}

	protected BooleanExpression nameEq(String name) {
		return StringUtils.hasText(name) ? user.name.contains(name) : null;
	}

	protected BooleanExpression isDeletedEq(Boolean isDeleted) {
		return isDeleted != null ? user.isDeleted.eq(isDeleted) : null;
	}

	protected BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
		if (start == null && end == null) return null;
		if (start != null && end == null) return user.createdAt.goe(TimeUtils.toInstant(start));
		if (start == null) return user.createdAt.loe(TimeUtils.toInstant(end));
		return user.createdAt.between(
				TimeUtils.toInstant(start),
				TimeUtils.toInstant(end)
		);
	}
}
