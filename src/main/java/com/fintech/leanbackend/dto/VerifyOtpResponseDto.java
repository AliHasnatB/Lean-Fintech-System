package com.fintech.leanbackend.dto;

public record VerifyOtpResponseDto(
        boolean verified,
        String message,
        JwtResponse accessToken
) {
}
