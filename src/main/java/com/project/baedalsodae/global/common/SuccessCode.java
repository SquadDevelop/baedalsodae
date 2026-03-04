package com.project.baedalsodae.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

	//cart
	CART_FOUND("CT200", HttpStatus.OK, "장바구니 조회 성공"),
	CART_ITEM_ADDED("CT201", HttpStatus.OK, "장바구니 아이템 추가 성공"),
	CART_ITEM_QUANTITY_UPDATED("CT202", HttpStatus.OK, "장바구니 아이템 수량 변경 성공"),
	CART_ITEM_REMOVED("CT203", HttpStatus.OK, "장바구니 아이템 삭제 성공"),
	CART_CLEARED("CT204", HttpStatus.OK, "장바구니 비우기 성공"),

	//order
	ORDER_CREATED("OR201", HttpStatus.CREATED, "주문 생성 성공"),

	;

	private final String code;
	private final HttpStatus status;
	private final String message;
}
