package com.example.memo.config.exception;

public class UnsupportedOAuth2ProviderException extends RuntimeException {

    private static final String MESSAGE = "지원하지 않는 OAuth2 공급자입니다.";

    public UnsupportedOAuth2ProviderException(String provider) {
        super(MESSAGE + " (" + provider + ")");
    }
}