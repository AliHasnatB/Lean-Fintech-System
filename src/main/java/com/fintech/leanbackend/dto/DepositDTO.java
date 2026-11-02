package com.fintech.leanbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
//import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;

public record DepositDTO(
        @NotNull @Positive(message = "Amount must be greater than 0") BigDecimal amount,
        @NotBlank @Email String email
) {}
