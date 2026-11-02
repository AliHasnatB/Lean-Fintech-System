package com.fintech.leanbackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintech.leanbackend.validation.ValidSendMoneyRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@ValidSendMoneyRequest
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMoneyDTO(
        String email,
        String number,
        String CreditCardNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Double amount
) {
}
