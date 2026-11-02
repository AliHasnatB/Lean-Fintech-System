package com.fintech.leanbackend.controller;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/send-by-email")
    public ResponseEntity<SendingReqDto> sendMoneyByEmail(
            @RequestBody SendingReqDto sendingReqDto) {
        SendingReqDto response = transactionService.sendMoneyByEmail(sendingReqDto);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/deposit")
    public ResponseEntity<DepositDTO> deposit(@RequestBody DepositDTO request) {
        DepositDTO response = transactionService.deposit(request);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/send")
    public ResponseEntity<String> sendMoney(@Valid @RequestBody SendMoneyDTO dto) {
        transactionService.processTransfer(dto);
        return ResponseEntity.ok("Money transfer initiated successfully.");
    }
    @GetMapping("/history")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<TransactionSummaryDto>> getTransactionHistory(
            @RequestParam("email") String email) {
        TransactionHistoryDTO requestDto = new TransactionHistoryDTO(email);
        List<TransactionSummaryDto> history = transactionService.transactionHistory(requestDto);

        return ResponseEntity.ok(history);
    }
}
