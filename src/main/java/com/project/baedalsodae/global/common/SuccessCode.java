package com.project.baedalsodae.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

	//cart
	CART_FOUND("CT200", HttpStatus.OK, "장바구니 조회 성공"),
	CART_ITEM_ADDED("CT201", HttpStatus.CREATED, "장바구니 아이템 추가 성공"),
	CART_ITEM_QUANTITY_UPDATED("CT202", HttpStatus.OK, "장바구니 아이템 수량 변경 성공"),
	CART_ITEM_REMOVED("CT203", HttpStatus.OK, "장바구니 아이템 삭제 성공"),
	CART_CLEARED("CT204", HttpStatus.OK, "장바구니 비우기 성공"),

	// store category
	STORE_CATEGORY_LIST_FOUND("SC200", HttpStatus.OK, "가게 카테고리 목록 조회 성공"),
	STORE_CATEGORY_DETAIL_FOUND("SC201", HttpStatus.OK, "가게 카테고리 상세 조회 성공"),
	STORE_CATEGORY_CREATED("SC202", HttpStatus.CREATED, "가게 카테고리 생성 성공"),
	STORE_CATEGORY_PATCHED("SC203", HttpStatus.OK, "가게 카테고리 수정 성공"),
	STORE_CATEGORY_DELETED("SC204", HttpStatus.OK, "가게 카테고리 삭제 성공"),
	;

	private final String code;
	private final HttpStatus status;
	private final String message;
}