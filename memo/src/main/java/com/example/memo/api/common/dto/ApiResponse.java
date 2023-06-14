package com.example.memo.api.common.dto;

import lombok.Builder;

public record ApiResponse(String status, String message) {
    @Builder
    public ApiResponse {
    }
}
