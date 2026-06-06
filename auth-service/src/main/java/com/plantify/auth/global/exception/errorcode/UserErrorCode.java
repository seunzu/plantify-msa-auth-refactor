package com.plantify.auth.global.exception.errorcode;

import com.plantify.auth.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_INFO_NULL("U002", "사용자 정보가 비어 있습니다.", HttpStatus.NOT_FOUND),
    INVALID_USERNAME("U003", "유효하지 않은 사용자 이름입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
