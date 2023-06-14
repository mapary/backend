package com.example.memo.api.v1.auth.dto;

import com.example.memo.validator.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class SignUpRequest {
    @NotEmpty
    @Email
    @Length(max = 32)
    private String email;

    @PasswordConstraint
    private String password;

    @Builder
    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
