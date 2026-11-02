package com.fintech.leanbackend.dto;

import lombok.Data;

@Data
public class AccountResponseDto {
    private String phoneNumber;
    private String address;
    private SignInRequest signInRequest;
}
