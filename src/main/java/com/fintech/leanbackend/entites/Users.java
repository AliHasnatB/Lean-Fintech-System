package com.fintech.leanbackend.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users extends Base{
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private int failedAttempts = 0;

    private String mi;

    private String userImage;

    private LocalDateTime OTPExpiry;

    private String OTP;

    @Enumerated(EnumType.STRING)
    private Status statuses;

    @Enumerated(EnumType.STRING)
    private Verification verification;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Account account;

    @Column(name = "pin", length = 100)
    private String Pin;
 //
//    @OneToMany(mappedBy = "sender")
//    private List<Transaction> sentTran = new ArrayList<>();
//
//    @OneToMany(mappedBy = "receiver")
//    private List<Transaction> recTran = new ArrayList<>();

}
