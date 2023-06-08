package com.example.memo.config.exception;

public class MemberNotFoundException extends RuntimeException {

    private static final String MESSAGE = "멤버가 존재하지 않습니다.";

    public MemberNotFoundException() {
        super(MESSAGE);
    }
}
