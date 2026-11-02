package com.fintech.leanbackend.service.implementation;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.entites.Role;
import com.fintech.leanbackend.entites.Status;
import com.fintech.leanbackend.entites.Users;
import com.fintech.leanbackend.entites.Verification;
import com.fintech.leanbackend.repository.RoleRepository;
import com.fintech.leanbackend.repository.UserRepository;
import com.fintech.leanbackend.service.AuthService;
import com.fintech.leanbackend.utils.JwtUtils;
import com.fintech.leanbackend.utils.KeyGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtil;
    private static final SecureRandom random = new SecureRandom();


    @Override
    @Transactional
    public SignUpResponse SignUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadCredentialsException("Account already exists");
        }


        long count = this.userRepository.count();
        Users user = this.modelMapper.map(signUpRequest, Users.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerification(Verification.UN_VERIFIED);
        String roleName = (count == 0) ? "ADMIN" : "USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Critical: Role '{}' not found in database during registration", roleName);
                    return new RuntimeException("Role not found");
                });

        user.setRoles(List.of(role));
        String otp = KeyGenerator.getOtpKeys();
        user.setOTP(otp);
        user.setOTPExpiry(LocalDateTime.now().plusMinutes(5));
        Users savedUser = userRepository.save(user);
        List<String> roles = savedUser.getRoles()
                .stream()
                .map(Role::getName)
                .toList();
        log.info("New user registered successfully. Email: {}, UserId: {}, Role: {}",
                savedUser.getEmail(), savedUser.getId(), roleName);
        return new SignUpResponse(
                savedUser.getName(),
                savedUser.getEmail(),
                roles
        );
    }

    @Override
    public JwtResponse SignIn(SignInRequest request) {
        MDC.put("loginEmail", request.getEmail());
        log.debug("Processing login request.");
        Users user = this.userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getFailedAttempts() >= 3) {
            log.warn("Login BLOCKED: Account locked due to multiple failed attempts. Email: {}", request.getEmail());
            throw new LockedException("Your account is locked due to 3 failed login attempts. Please reset your password.");
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            user.setFailedAttempts(0);
            userRepository.save(user);
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            if (userDetails == null || !userDetails.getUsername().equals(user.getEmail())) {
                throw new AccessDeniedException("User is not authenticated");
            }
            String email = userDetails.getUsername();

            String token;
            try {
                token = jwtUtil.generateToken(email);

            } catch (Exception e) {
                log.error("Critical: Failed to generate JWT for UserId: {}", user.getId(), e);
                throw new RuntimeException("Token generation failed", e);
            }
            log.info("User logged in successfully. UserId: {}, Roles: {}", user.getId(),
                    userDetails.getAuthorities());
            return JwtResponse.builder()
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

        } catch (BadCredentialsException e) {
            int newAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(newAttempts);
            userRepository.save(user);

            log.warn("Failed login attempt {}/3 for email: {}", newAttempts, request.getEmail());
            if (newAttempts >= 3) {
                throw new LockedException("Your account is now locked due to 3 failed login attempts. Please reset your password.");
            }
            int attemptsLeft = 3 - newAttempts;
            throw new BadCredentialsException("Invalid email or password. " + attemptsLeft + " attempt(s) remaining.");

        } catch (Exception e) {
            log.error("Critical: Failed to process login request for email: {}", request.getEmail(), e);
            throw new RuntimeException("Login failed", e);
        } finally {
            MDC.remove("loginEmail");
        }
    }

    @Override
    @Transactional
    public void setupUserPin(PinRecord pinRecord) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
        "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("User must be authenticated to set a PIN.");
        }
        String email = authentication.getName();
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Critical Data Inconsistency: Authenticated user not found in DB. Email domain: {}",
                            extractDomain(email));
                    return new IllegalStateException("Authenticated user missing from database.");
                });
        validatePinStrength(pinRecord.pin());
        user.setPin(passwordEncoder.encode(pinRecord.pin()));
        user.setStatuses(Status.ACTIVE);
        userRepository.save(user);
        MDC.put("userId", String.valueOf(user.getId()));
        MDC.put("action", "PIN_SETUP");
        try {
            log.info("User PIN setup successfully and account activated.");
        } finally {
            MDC.remove("userId");
            MDC.remove("action");
        }
    }

    @Override
    public Boolean VerifyPin(String pin) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {

            throw new RuntimeException("User must be authenticated.");
        }

        String email = authentication.getName();

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return passwordEncoder.matches(pin, user.getPin());
    }

    private String extractDomain(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(email.indexOf("@"));
        }
        return "unknown";
    }
    private void validatePinStrength(String pin) {
        if (pin == null || pin.length() < 4 || pin.length() > 8) {
            throw new IllegalArgumentException("PIN must be between 4 and 8 digits.");
        }
    }


}
