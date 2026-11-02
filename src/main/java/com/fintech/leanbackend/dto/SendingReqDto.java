package com.fintech.leanbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendingReqDto {
    private BigDecimal amount = BigDecimal.ZERO;
    private String email;
}
