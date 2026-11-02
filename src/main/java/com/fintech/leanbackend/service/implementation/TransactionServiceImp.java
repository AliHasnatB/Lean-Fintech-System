package com.fintech.leanbackend.service.implementation;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.entites.Account;
import com.fintech.leanbackend.entites.Transaction;
import com.fintech.leanbackend.entites.TransactionType;
import com.fintech.leanbackend.entites.Users;
import com.fintech.leanbackend.repository.AccountRepository;
import com.fintech.leanbackend.repository.TransactionRepository;
import com.fintech.leanbackend.repository.UserRepository;
import com.fintech.leanbackend.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Override
    @Transactional
    public SendingReqDto sendMoneyByEmail(SendingReqDto req) {
//        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <=0){
//            throw new IllegalArgumentException("Transfer amount must be greater than zero");
//        }
        validateTransferAmount(req.getAmount(),req.getEmail());
        String senderEmail = getAuthenticatedUserEmail();

        Users receiveUser = this.userRepository.findByEmail(req.getEmail())
                .orElseThrow(()-> new RuntimeException("Receiver User not available"));
        Users senderUser = this.userRepository.findByEmail(senderEmail)
                .orElseThrow(()-> new RuntimeException("Sender not found"));

        if(senderUser.getId().equals(receiveUser.getId())){
            throw new IllegalArgumentException("Cannot transfer money to yourself");
        }
        Account receiverAccount = this.accountRepository.findByUser(receiveUser)
                .orElseThrow(() -> new RuntimeException("UserAccount not found"));

//        int compareTo = senderUser.getAccount().getBalance().compareTo(BigDecimal.ZERO);

        Account senderAccount = senderUser.getAccount();
        if (senderAccount.getBalance().compareTo(BigDecimal.ZERO) < 0){
            log.warn("Insufficient balance for user {}. Attempted: {}, Available: {}",
                    senderEmail, req.getAmount(), senderAccount.getBalance());
            throw new RuntimeException("Insufficient Balance");
        }
        senderAccount.setBalance(senderAccount.getBalance().subtract(req.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(req.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setReceiver(receiveUser);
        transaction.setSender(senderUser);
        transaction.setAmount(req.getAmount());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transactionRepository.save(transaction);

        SendingReqDto mapping = modelMapper.map(transaction, SendingReqDto.class);
        mapping.setEmail(req.getEmail());
        return mapping;
    }

    @Override
    @Transactional
    public DepositDTO deposit(DepositDTO req) {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
//        String Email = authentication.getName();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        Users company = this.userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Users receiver = this.userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("receiver user not found"));
        Account companyAccount = this.accountRepository.findByUser(company).orElseThrow(() -> new RuntimeException("company account not found"));
        Account receiverAccount = this.accountRepository.findByUser(receiver).orElseThrow(() -> new RuntimeException("receiver user not found"));

        receiverAccount.setBalance(receiverAccount.getBalance().add(req.amount()));
        Transaction transaction = new Transaction();
        transaction.setReceiver(receiver);
        transaction.setSender(company);
        transaction.setAmount(req.amount());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transactionRepository.save(transaction);
        accountRepository.save(receiverAccount);
        accountRepository.save(companyAccount);
        return new DepositDTO(
                transaction.getAmount(),
                receiver.getEmail()
        );
    }

    @Override
    @Transactional
    public void processTransfer(SendMoneyDTO dto) {
        if(StringUtils.hasText(dto.email())) {
            sendMoneyByEmail(dto.email(), BigDecimal.valueOf(dto.amount()));
        } else if (StringUtils.hasText(dto.number())) {
            sendMoneyByNumber(dto.number(), BigDecimal.valueOf(dto.amount()));
        }else if(StringUtils.hasText(dto.CreditCardNumber())){
            sendMoneyCreditCardNumber(dto.CreditCardNumber(), BigDecimal.valueOf(dto.amount()));
        }
    }

    @Override
    @Transactional
    public void sendMoneyByEmail(String email, BigDecimal amount) {
        validateTransferAmount(amount,email);
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String senderEmail = authentication.getName();
        Users senderUser = this.userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Users receiverUser = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account receiverAccount = this.accountRepository.findByUser(receiverUser)
                .orElseThrow(() -> new RuntimeException("receiver user not found"));
        Account senderAccount = this.accountRepository.findByUser(senderUser)
                .orElseThrow(() -> new RuntimeException("sender user not found"));

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for UserId: {}. Attempted: {}, Available: {}",
                    senderUser.getId(), amount, senderAccount.getBalance());
            throw new RuntimeException("Insufficient balance to complete this transfer");
        }

        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));

        Transaction transaction = new Transaction();
        transaction.setReceiver(receiverUser);
        transaction.setSender(senderUser);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);

        transactionRepository.save(transaction);
        accountRepository.save(receiverAccount);
        accountRepository.save(senderAccount);

        log.info("Transfer successful");

    }

    @Override
    @Transactional
    public void sendMoneyByNumber(String number, BigDecimal amount) {
        validateTransferAmount(amount,number);
        String Email = getAuthenticatedUserEmail();

        Users user = this.userRepository.findByEmail(Email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account payee = this.accountRepository.findByPhoneNumber(number)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account payer = this.accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("user not found"));

        payee.setBalance(payer.getBalance().add(amount));
        payer.setBalance(payer.getBalance().subtract(amount));

        Transaction transaction = new Transaction();
        transaction.setSender(user);
        transaction.setReceiver(payee.getUser().getAccount().getReceiver());
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);

        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void sendMoneyCreditCardNumber(String number, BigDecimal amount) {
        validateTransferAmount(amount,number);
        String email = getAuthenticatedUserEmail();
        Users payeeUser = this.userRepository.findByMi(number)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Users payerUser = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account payeeAccount = this.accountRepository.findByUser(payeeUser)
                .orElseThrow(() -> new RuntimeException("user not found"));
        Account payerAccount = this.accountRepository.findByUser(payerUser)
                .orElseThrow(() -> new RuntimeException("user not found"));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setSender(payeeUser);
        transaction.setReceiver(payerUser);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);

        payeeAccount.setBalance(payeeAccount.getBalance().add(amount));
        payerAccount.setBalance(payerAccount.getBalance().subtract(amount));

        transactionRepository.save(transaction);
        log.info("Transfer successful by send " +
                "by card number with balance {}", payeeAccount.getBalance());

    }

    @Override
    public List<TransactionSummaryDto> transactionHistory(TransactionHistoryDTO req) {
            Users userMail = this.userRepository.findByEmail(req.email())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + req.email()));
            return this.transactionRepository.findLastFiveTransactionsByUserId(userMail.getId());
        }
        
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return authentication.getName();
    }
    private void validateTransferAmount(BigDecimal amount, String recipientIdentifier) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Transfer amount must be greater than zero. Recipient: {}", recipientIdentifier);
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
    }

}
