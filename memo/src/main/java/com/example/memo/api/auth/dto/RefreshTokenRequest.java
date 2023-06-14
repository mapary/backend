package com.example.memo.api.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenRequest {
    @NotEmpty
    private String refreshToken;

    @Builder
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
