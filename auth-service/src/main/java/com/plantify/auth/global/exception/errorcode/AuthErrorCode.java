package com.plantify.auth.global.exception.errorcode;

import com.plantify.auth.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_TOKEN("A001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("A002", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_TOKEN("A003", "지원되지 않는 토큰 형식입니다.", HttpStatus.BAD_REQUEST),
    TOKEN_CLAIMS_EMPTY("A004", "토큰의 클레임이 비어 있습니다.", HttpStatus.BAD_REQUEST),
    ACCESS_TOKEN_NULL("A005", "액세스 토큰이 비어 있습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
