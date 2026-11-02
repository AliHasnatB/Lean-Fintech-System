package com.fintech.leanbackend.entites;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionMessages {
    SENDER_TRANSACTION_MESSAGE(1L,"Money Transaction","Money has been sent to the account"),
    RECEIVER_TRANSACTION_MESSAGE(2L,"Money Transaction", "Money has been received"),
    TRANSACTION_FAILED(3L,"Transaction Failed","Transaction failed"),
    TRANSACTION_DEPOSIT(4L,"Deposit","Account has been debited"),
    TRANSACTION_WITHDRAW(5L,"Withdraw","Account has been credited by withdraw");
    private final Long status;
    private final String subject;
    private final String message;
}
