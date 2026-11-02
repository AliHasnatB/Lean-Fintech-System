package com.fintech.leanbackend.dto;

import java.math.BigDecimal;

public record UserDetailByPhoneDTO(
        String UserName,
        String AccountNumber
) {
}
