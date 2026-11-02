package com.fintech.leanbackend.dto;

import com.fintech.leanbackend.entites.Verification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserLoginResponseDetail {
    private Long id;
    private String email;
    private String name;
    private Verification verification;
    private String roleName;

    public UserLoginResponseDetail(
            Long id,
            String email,
            String name,
            Verification verification,
            String roleName
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.verification = verification;
        this.roleName = roleName;
    }
}
