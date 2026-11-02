package com.fintech.leanbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private String name;
    private String email;
    //    private Account phoneNumber;
//    private String password;
    private List<String> roles;
}
