package com.fintech.leanbackend.service;

import com.fintech.leanbackend.dto.VerifyOtpRequestDto;
import com.fintech.leanbackend.dto.VerifyOtpResponseDto;

public interface OtpService {
    VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto request);
    void regenerateOTP(String email);
}
