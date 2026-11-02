package com.fintech.leanbackend.controller;

import com.fintech.leanbackend.dto.VerifyOtpRequestDto;
import com.fintech.leanbackend.dto.VerifyOtpResponseDto;
import com.fintech.leanbackend.service.OtpService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/verify")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VerifyOtpResponseDto> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request) {

        VerifyOtpResponseDto response = otpService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        otpService.regenerateOTP(email);
        return ResponseEntity.ok("OTP regenerated successfully.");
    }
}
