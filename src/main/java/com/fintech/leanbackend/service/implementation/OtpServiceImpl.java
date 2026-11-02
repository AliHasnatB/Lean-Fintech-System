package com.fintech.leanbackend.service.implementation;

import com.fintech.leanbackend.dto.JwtResponse;
import com.fintech.leanbackend.dto.VerifyOtpRequestDto;
import com.fintech.leanbackend.dto.VerifyOtpResponseDto;
import com.fintech.leanbackend.entites.Role;
import com.fintech.leanbackend.entites.Users;
import com.fintech.leanbackend.entites.Verification;
import com.fintech.leanbackend.repository.UserRepository;
import com.fintech.leanbackend.service.OtpService;
import com.fintech.leanbackend.utils.JwtUtils;
import com.fintech.leanbackend.utils.KeyGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    @Transactional
    @Override
    public VerifyOtpResponseDto verifyOtp(VerifyOtpRequestDto request) {
        Users user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("User not found: {}", request.email());
                    return new RuntimeException("User not found");
                });
        if (user.getVerification() == Verification.VERIFIED) {
            log.info("user already verified, {}", user.getVerification());
            return new VerifyOtpResponseDto(
                    true,
                    "User is already verified.",
                    null
            );
        }
        // Check if OTP is expired
        if (user.getOTPExpiry() == null || LocalDateTime.now().isAfter(user.getOTPExpiry())) {
            throw new RuntimeException("OTP has expired");
        }

        // Check OTP
        if (!request.otp().equals(user.getOTP())) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException("Invalid OTP");
        }

        // Success
        user.setFailedAttempts(0);
        user.setVerification(Verification.VERIFIED); // if you have such an enum
        user.setOTP(null);
        user.setOTPExpiry(null);

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());

        JwtResponse jwtResponse = JwtResponse.builder()
                .id(user.getId())
                .username(user.getName())
                .email(user.getEmail())
                .token(token)
                .type("Bearer")
                .verification(user.getVerification().name())
                .role(user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList())
                .build();

        return new VerifyOtpResponseDto(
                true,
                "OTP verified successfully",
                jwtResponse
        );
    }

    @Override
    public void regenerateOTP(String email) {
        Users users = this.userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User doesnt exist: , {}", email);
                    return new RuntimeException("Can't regenerate otp");
                });
        String otpKeys = KeyGenerator.getOtpKeys();
        users.setOTP(otpKeys);
        userRepository.save(users);
    }
}
