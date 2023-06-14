package com.example.memo.api.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException apiException) {
        return ResponseEntity
                .status(apiException.getHttpStatus())
                .body(Map.of("error", apiException.getErrorCode(), "message", apiException.getMessage()));
    }
}
