package com.fintech.leanbackend.dto;

public record UserLoginResponse(
        Long id,
        String email,
        String username,
        String verification,
        String role
) {}