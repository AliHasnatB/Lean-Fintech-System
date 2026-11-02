package com.fintech.leanbackend.dto;

import jakarta.validation.constraints.AssertTrue;

import java.math.BigDecimal;

public record FindUserByPhoneDTO(
        String phoneNumber,
        String email
) {
    @AssertTrue(message = "Either phoneNumber or email must be provided")
    public boolean isAtLeastOneIdentifierProvided() {
        return (phoneNumber != null && !phoneNumber.trim().isEmpty()) ||
                (email != null && !email.trim().isEmpty());
    }
}
