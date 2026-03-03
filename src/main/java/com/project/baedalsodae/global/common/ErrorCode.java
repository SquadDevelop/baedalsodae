package com.project.baedalsodae.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // common
    INTERNAL_SERVER_ERROR("Z001", HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 오류가 발생했습니다"),
    INVALID_REQUEST("Z002", HttpStatus.BAD_REQUEST, "요청값이 잘못됐습니다"),
    UNAUTHORIZED("Z003", HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN("Z004", HttpStatus.FORBIDDEN, "권한이 없니다."),

    // database
    DATABASE_UNAVAILABLE("DA001", HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스 연결이 불가능합니다"),
    DATA_ACCESS_ERROR("DA002", HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 작업 중 오류가 발생했습니다"),
    DATA_INTEGRITY_VIOLATION("DA003", HttpStatus.CONFLICT, "데이터 무결성 제약 조건을 위반했습니다"),

    // user/signup
    USER_NOT_FOUND("U001", HttpStatus.NOT_FOUND, "유저가 없습니다"),
    USER_DUPLICATED_EMAIL("U002", HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_DUPLICATED_PHONE_NUMBER("U003", HttpStatus.CONFLICT, "이미 가입된 전화번호입니다"),

    // store-category
    STORE_CATEGORY_NOT_FOUND("SC001", HttpStatus.NOT_FOUND, "가게 카테고리가 존재하지 않습니다"),

    // MenuItem
    MENU_CATEGORY_NOT_FOUND("MC001", HttpStatus.NOT_FOUND, "메뉴 카테고리가 없습니다"),
    MENU_ITEM_NOT_FOUND("MI001", HttpStatus.NOT_FOUND, "메뉴 아이템이 없습니다"),

    //cart
    CART_NOT_FOUND("CT001", HttpStatus.NOT_FOUND,"장바구니가 없습니다."),
    CART_INVALID_QUANTITY("CT002", HttpStatus.BAD_REQUEST,"수량은 1 이상이어야 합니다."),
    CART_DIFFERENT_STORE("CT003", HttpStatus.BAD_REQUEST, "다른 가게의 메뉴는 담을 수 없습니다."),
    CART_ITEM_NOT_FOUND ("CT004", HttpStatus.NOT_FOUND, "장바구니 아이템이 없습니다."),
    CART_ITEM_EMPTY ("CT005", HttpStatus.BAD_REQUEST, "장바구니 아이템이 비어있습니다."),

    //order
    ORDER_INVALID_TOTAL_AMOUNT("OD001", HttpStatus.BAD_REQUEST, "총 금액이 올바르지 않습니다."),

    //store
    STORE_NOT_FOUND("ST001", HttpStatus.NOT_FOUND, "가게가 존재하지 않습니다."),

    ;
    private final String code;
    private final HttpStatus status;
    private final String message;
}
