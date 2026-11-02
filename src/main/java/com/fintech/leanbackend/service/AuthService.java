package com.fintech.leanbackend.service;

import com.fintech.leanbackend.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    SignUpResponse SignUp(SignUpRequest signUpRequest);
    JwtResponse SignIn(SignInRequest request);
    void setupUserPin(PinRecord pinRecord);
    Boolean VerifyPin(String pin);
}
