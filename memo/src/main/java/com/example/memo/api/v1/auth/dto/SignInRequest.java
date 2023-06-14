package com.example.memo.api.v1.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignInRequest {
    @NotEmpty
    @Email
    @Length(max = 32)
    private String email;

    @NotEmpty
    @Length(min = 8, max = 20)
    private String password;
}
