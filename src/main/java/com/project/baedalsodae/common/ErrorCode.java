package com.project.baedalsodae.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // common
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR_500", HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 오류가 발생했습니다"),
    INVALID_REQUEST("INVALID_REQUEST_400", HttpStatus.BAD_REQUEST, "요청값이 잘못됐습니다"),
    UNAUTHORIZED("UNAUTHORIZED_401", HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN("FORBIDDEN_403", HttpStatus.FORBIDDEN, "권한이 없니다."),

    // database
    DATABASE_UNAVAILABLE("DATABASE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스 연결이 불가능합니다"),
    DATA_ACCESS_ERROR("DATA_ACCESS_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 작업 중 오류가 발생했습니다"),
    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION", HttpStatus.CONFLICT, "데이터 무결성 제약 조건을 위반했습니다"),

    // user/signup
    USER_NOT_FOUND("USER_NOT_FOUND_404", HttpStatus.NOT_FOUND, "유저가 없습니다"),
    USER_DUPLICATED_EMAIL("USER_DUPLICATED_EMAIL_409", HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_DUPLICATED_PHONE_NUMBER("USER_DUPLICATED_PHONE_NUMBER_409", HttpStatus.CONFLICT, "이미 가입된 전화번호입니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
