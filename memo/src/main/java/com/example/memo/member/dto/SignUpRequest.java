package com.example.memo.member.dto;

import com.example.memo.config.validator.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank
    @Email
    private String email;

    @PasswordConstraint
    private String password;
}