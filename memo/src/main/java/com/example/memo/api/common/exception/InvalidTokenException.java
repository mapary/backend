package com.example.memo.api.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApiException {

    private static final String DEFAULT_MESSAGE = "토큰이 유효하지 않습니다.";

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}
