package com.fintech.leanbackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintech.leanbackend.entites.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SignUpRequest {
    private String name;
    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@fintech\\.com$",
            message = "Email must be a valid @fintech.com email address"
    )
    private String email;
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8}$",
            message = "Password must be exactly 8 characters and contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;
}
