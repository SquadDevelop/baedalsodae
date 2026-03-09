package com.project.baedalsodae.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    // cart
    CART_FOUND("CT200", HttpStatus.OK, "장바구니 조회 성공"),
    CART_ITEM_ADDED("CT201", HttpStatus.CREATED, "장바구니 아이템 추가 성공"),
    CART_ITEM_QUANTITY_UPDATED("CT202", HttpStatus.OK, "장바구니 아이템 수량 변경 성공"),
    CART_ITEM_REMOVED("CT203", HttpStatus.OK, "장바구니 아이템 삭제 성공"),
    CART_CLEARED("CT204", HttpStatus.OK, "장바구니 비우기 성공"),

    // order
    ORDER_CREATED("OR201", HttpStatus.CREATED, "주문 생성 성공"),
    ORDER_LIST("OR202", HttpStatus.OK, "주문 목록 조회 성공"),
    ORDER_DETAIL("OR203", HttpStatus.OK, "주문 상세 조회 성공"),
    ORDER_STATUS("OR204", HttpStatus.OK, "주문 상태 조회 성공"),
    ORDER_REQUESTED("OR205", HttpStatus.OK, "주문 요청 성공"),
    ORDER_ACCEPTED("OR206", HttpStatus.OK, "주문 수락 성공"),
    ORDER_REJECTED("OR207", HttpStatus.OK, "주문 거절 성공"),
    ORDER_COOKING_COMPLETED("OR208", HttpStatus.OK, "조리 완료 성공"),
    ORDER_DELIVERING("OR209", HttpStatus.OK, "배달 시작 성공"),
    ORDER_DELIVERED("OR210", HttpStatus.OK, "배달 완료 성공"),
    ORDER_CANCEL_REQUESTED("OR211", HttpStatus.OK, "주문 취소 요청 성공"),
    ORDER_CANCELED("OR212", HttpStatus.OK, "주문 취소 성공"),

    // store category
    STORE_CATEGORY_LIST_FOUND("SC200", HttpStatus.OK, "가게 카테고리 목록 조회 성공"),
    STORE_CATEGORY_DETAIL_FOUND("SC201", HttpStatus.OK, "가게 카테고리 상세 조회 성공"),
    STORE_CATEGORY_CREATED("SC202", HttpStatus.CREATED, "가게 카테고리 생성 성공"),
    STORE_CATEGORY_PATCHED("SC203", HttpStatus.OK, "가게 카테고리 수정 성공"),
    STORE_CATEGORY_DELETED("SC204", HttpStatus.OK, "가게 카테고리 삭제 성공"),

    // store
    STORE_LIST_FOUND("ST200", HttpStatus.OK, "가게 목록 조회 성공"),
    STORE_CREATED("ST201", HttpStatus.CREATED, "가게 등록 성공"),
    STORE_UPDATED("ST202", HttpStatus.OK, "가게 정보 수정 성공"),
    STORE_STATUS_UPDATED("ST203", HttpStatus.OK, "가게 영업 상태 변경 성공"),
    STORE_DELETED("ST204", HttpStatus.OK, "가게 삭제 성공"),
    STORE_DETAIL_FOUND("ST205", HttpStatus.OK, "가게 상세 조회 성공"),
    STORE_FOUND_FOR_OWNER("ST206", HttpStatus.OK, "가게 조회 성공"),

    // store-hours
    STORE_HOURS_FOUND("SH200", HttpStatus.OK, "영업시간 조회 성공"),
    STORE_HOURS_CREATED("SH201", HttpStatus.CREATED, "영업시간 등록 성공"),
    STORE_HOURS_UPDATED("SH202", HttpStatus.OK, "영업시간 수정 성공"),
    STORE_HOURS_DELETED("SH203", HttpStatus.OK, "영업시간 삭제 성공"),
    STORE_HOURS_NOT_SET("SH204", HttpStatus.OK, "설정된 영업시간이 없습니다."),

    // user
    USER_FOUND("UR200", HttpStatus.OK, "회원 정보 조회 성공"),
    USER_CREATED("UR201", HttpStatus.CREATED, "회원가입 성공"),
    USER_UPDATED("UR202", HttpStatus.OK, "회원 정보 수정 성공"),
    USER_DELETED("UR203", HttpStatus.OK, "회원 탈퇴 성공"),

    // user address
    USER_ADDRESS_CREATED("UA200", HttpStatus.CREATED, "회원 주소 등록 성공"),
    USER_ADDRESS_FOUND("UA201", HttpStatus.OK, "회원 주소 조회 성공"),
    USER_ADDRESS_UPDATED("UA202", HttpStatus.OK, "회원 주소 수정 성공"),
    USER_ADDRESS_BULK_UPDATED("UA203", HttpStatus.OK, "회원 주소 일괄 수정 성공"),
    USER_ADDRESS_DELETED("UA204", HttpStatus.OK, "회원 주소 삭제 성공"),
    USER_MAIN_ADDRESS_CHANGED("UA205", HttpStatus.OK, "회원 메인 주소지 변경 성공"),

    // auth
    LOGIN_SUCCESS("AU200", HttpStatus.OK, "로그인 성공"),
    LOGOUT_SUCCESS("AU201", HttpStatus.OK, "로그아웃 성공"),

    // menu category
    MENU_CATEGORY_CREATED("MC200", HttpStatus.CREATED, "메뉴 카테고리 등록 성공"),
    MENU_CATEGORY_UPDATED("MC201", HttpStatus.OK, "메뉴 카테고리 수정 성공"),
    MENU_CATEGORY_DELETED("MC202", HttpStatus.OK, "메뉴 카테고리 삭제 성공"),
    MENU_CATEGORY_ORDER_UPDATED("MC203", HttpStatus.OK, "메뉴 카테고리 순서 변경 성공"),
    MENU_CATEGORY_NAME_DUPLICATE_CHECKED("MC204", HttpStatus.OK, "메뉴 카테고리 이름 중복 체크 성공"),
    MENU_CATEGORY_LIST_FOUND("MC205", HttpStatus.OK, "메뉴 카테고리 목록 조회 성공"),

    // menu item
    MENU_ITEM_CREATED("MI200", HttpStatus.CREATED, "메뉴 아이템 등록 성공"),
    MENU_ITEM_UPDATED("MI201", HttpStatus.OK, "메뉴 아이템 수정 성공"),
    MENU_ITEM_DELETED("MI202", HttpStatus.OK, "메뉴 아이템 삭제 성공"),
    MENU_ITEM_ORDER_UPDATED("MI203", HttpStatus.OK, "메뉴 아이템 순서 변경 성공"),
    MENU_ITEM_NAME_DUPLICATE_CHECKED("MI204", HttpStatus.OK, "메뉴 아이템 이름 중복 체크 성공"),
    MENU_ITEM_LIST_FOUND("MI205", HttpStatus.OK, "메뉴 아이템 목록 조회 성공"),

    // payment
    PAYMENT_HISTORY_FOUND("PY200", HttpStatus.OK, "결제 내역 조회 성공"),
    PAYMENT_DETAIL_FOUND("PY201", HttpStatus.OK, "결제 상세 조회 성공"),

    // review
    REVIEW_LIST_FOUND("RV200", HttpStatus.OK, "리뷰 목록 조회 성공"),
    REVIEW_DETAIL_FOUND("RV201", HttpStatus.OK, "리뷰 상세 조회 성공"),
    REVIEW_CREATED("RV202", HttpStatus.CREATED, "리뷰 작성 성공"),
    REVIEW_UPDATED("RV203", HttpStatus.OK, "리뷰 수정 성공"),
    REVIEW_DELETED("RV204", HttpStatus.OK, "리뷰 삭제 성공"),
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;
}
