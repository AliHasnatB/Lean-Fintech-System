package com.fintech.leanbackend.service;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.entites.Status;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AccountService {
    AccountResponseDto createAccount(AccountReqDto accountReqDto);
    BalanceDTO Balance(BalanceDTO req);
    UserDetailByPhoneDTO verifyUserByPhone(FindUserByPhoneDTO req);
    UserAccountProjection bankDetails(Long id);
    String saveImage(Long userId,MultipartFile file) throws IOException;
    public UserLoginResponseDetail getUserLoginDetails(Long id);
    public void updateUserStatus(String email, Status status);
    BankStats getBankStats();
    BankAdminStatsProjection getAdminBankStats();
}
