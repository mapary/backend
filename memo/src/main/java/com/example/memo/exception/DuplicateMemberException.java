package com.example.memo.exception;

public class DuplicateMemberException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "이미 가입된 회원입니다.";

    public DuplicateMemberException(String email) {
        super(email + "(은)는 " + DEFAULT_MESSAGE);
    }
}

