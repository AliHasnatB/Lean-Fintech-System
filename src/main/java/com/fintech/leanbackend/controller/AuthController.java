package com.fintech.leanbackend.controller;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.entites.Users;
import com.fintech.leanbackend.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        SignUpResponse response = authService.SignUp(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signIn(@RequestBody SignInRequest request) {
        JwtResponse response = authService.SignIn(request);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/pin")
    public ResponseEntity<Void> setupUserPin(
            @Valid @RequestBody PinRecord pinRecord) {
        authService.setupUserPin(pinRecord);
        return ResponseEntity.noContent().build();
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/verify-pin")
    public ResponseEntity<Boolean> verifyPin(@RequestBody PinRecord pin) {
        boolean verified = authService.VerifyPin(pin.pin());
        return ResponseEntity.ok(verified);
    }
}
