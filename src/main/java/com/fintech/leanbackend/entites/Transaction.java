package com.fintech.leanbackend.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends Base{
    private BigDecimal amount = BigDecimal.ZERO;
    private String email;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Users sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Users receiver;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionMessages transactionMessages;

    @Column(name = "transaction_id", nullable = false, unique = true, updatable = false)
    private String transactionID;

    @PrePersist
    public void generateTransactionID() {
        if (transactionID == null || transactionID.isBlank()) {
            transactionID = "TNX" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        }
    }
}


