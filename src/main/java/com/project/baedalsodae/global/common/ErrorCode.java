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
    USER_DUPLICATED_USERNAME("U004", HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    USER_DUPLICATED_NICKNAME("U005", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    USER_ADDRESS_NOT_FOUND("U006", HttpStatus.NOT_FOUND, "해당 회원의 주소지를 찾을 수 없습니다."),
    USER_ADDRESS_CANNOT_DELETE("U007", HttpStatus.BAD_REQUEST, "회원 주소를 더 이상 삭제할 수 없습니다."),
    USER_ADDRESS_DUPLICATED("U008", HttpStatus.BAD_REQUEST, "중복된 회원주소가 존재합니다."),

    // store-category
    STORE_CATEGORY_NOT_FOUND("SC001", HttpStatus.NOT_FOUND, "가게 카테고리가 존재하지 않습니다"),

    // store
    STORE_NOT_FOUND("S001", HttpStatus.NOT_FOUND, "가게가 존재하지 않습니다."),
    STORE_DUPLICATED_BUSINESS_NUMBER("S002", HttpStatus.CONFLICT, "이미 가입된 사업자입니다"),
    STORE_FORBIDDEN("S002", HttpStatus.FORBIDDEN, "해당 가게에 대한 수정 권한이 없습니다."),
    STORE_STATUS_CHANGE_FORBIDDEN("S003", HttpStatus.FORBIDDEN, "가게 상태를 변경할 권한이 없습니다."),

    // menu-category
    MENU_CATEGORY_NOT_FOUND("MC001", HttpStatus.NOT_FOUND, "메뉴 카테고리가 없습니다"),
    INVALID_MENU_CATEGORY_ORDER("MC002", HttpStatus.BAD_REQUEST, "메뉴 카테고리 순서가 잘못됐습니다."),
    DUPLICATE_MENU_CATEGORY_NAME("MC003", HttpStatus.CONFLICT, "같은 가게에 같은 이름의 메뉴 카테고리가 존재합니다"),
    MENU_CATEGORY_ORDER_CONFLICT("MC004", HttpStatus.CONFLICT, "메뉴 카테고리 순서가 충돌했습니다. 다시 시도해주세요."),

    // menuItem
    MENU_ITEM_NOT_FOUND("MI001", HttpStatus.NOT_FOUND, "메뉴 아이템이 없습니다"),
    INVALID_MENU_ITEM_ORDER("MI002", HttpStatus.BAD_REQUEST, "메뉴 순서가 유효하지 않습니다"),
    DUPLICATE_MENU_ITEM_NAME("MI003", HttpStatus.CONFLICT, "같은 카테고리에 같은 이름의 메뉴 아이템이 존재합니다"),
    MENU_ITEM_ORDER_CONFLICT("MI004", HttpStatus.CONFLICT, "메뉴 아이템 순서가 충돌했습니다. 다시 시도해주세요."),

    // cart
    CART_NOT_FOUND("CT001", HttpStatus.NOT_FOUND, "장바구니가 없습니다."),
    CART_INVALID_QUANTITY("CT002", HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다."),
    CART_DIFFERENT_STORE("CT003", HttpStatus.BAD_REQUEST, "다른 가게의 메뉴는 담을 수 없습니다."),
    CART_ITEM_NOT_FOUND("CT004", HttpStatus.NOT_FOUND, "장바구니 아이템이 없습니다."),
    CART_ITEM_EMPTY("CT004", HttpStatus.NOT_FOUND, "장바구니 아이템이 비어있습니다."),

    // order
    ORDER_INVALID_TOTAL_AMOUNT("OD001", HttpStatus.BAD_REQUEST, "총 메뉴 금액이 올바르지 않습니다."),
    ORDER_INVALID_FINAL_AMOUNT("OD002", HttpStatus.BAD_REQUEST, "총 결제 금액이 올바르지 않습니다."),

    // payment
    PAYMENT_NOT_FOUND("PY001", HttpStatus.NOT_FOUND, "결제 목록 정보가 없습니다."),
    PAYMENT_DETAIL_NOT_FOUND("PY002", HttpStatus.NOT_FOUND, "결제 상세 정보가 없습니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}
