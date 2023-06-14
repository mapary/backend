package com.example.memo.api.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public record ApiResponse(String status, String message) {
    @Builder
    public ApiResponse {
    }
}
