package com.fintech.leanbackend.service;

import com.fintech.leanbackend.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    SendingReqDto sendMoneyByEmail(SendingReqDto req);
    DepositDTO deposit(DepositDTO req);
    void processTransfer(SendMoneyDTO dto);
    void sendMoneyByEmail(String email, BigDecimal amount);
    void sendMoneyByNumber(String number, BigDecimal amount);
    void sendMoneyCreditCardNumber(String number, BigDecimal amount);
    List<TransactionSummaryDto> transactionHistory(TransactionHistoryDTO req);
}
