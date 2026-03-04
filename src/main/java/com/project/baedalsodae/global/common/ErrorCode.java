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

    MENU_CATEGORY_NOT_FOUND("MC001", HttpStatus.NOT_FOUND, "메뉴 카테고리가 없습니다"),

    // MenuItem
    MENU_ITEM_NOT_FOUND("MI001", HttpStatus.NOT_FOUND, "메뉴 아이템이 없습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
