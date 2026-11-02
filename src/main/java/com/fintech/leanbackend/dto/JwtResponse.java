package com.fintech.leanbackend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JwtResponse {
    private Long id;
    private String email;
    private String username;
    private String token;
    private String type = "Bearer";
    private String verification;
    private List<String> role;
}
