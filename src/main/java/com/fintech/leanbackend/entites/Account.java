package com.fintech.leanbackend.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends Base{
    @Column(length = 12)
    private String phoneNumber;
    @Column
    private String address;
    @Column
    private BigDecimal balance = BigDecimal.ZERO;
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Users receiver;

//    @OneToMany(mappedBy = "account",cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Transaction> transactions = new ArrayList<>();

//    @ManyToOne
//    @JoinColumn(name = "account_id")
//    private Account account;
}
