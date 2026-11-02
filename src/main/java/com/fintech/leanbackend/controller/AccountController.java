package com.fintech.leanbackend.controller;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.service.AccountService;
import com.fintech.leanbackend.service.implementation.AccountServiceImp;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/create")
    public ResponseEntity<AccountResponseDto> createAccount(
            @RequestBody AccountReqDto accountReqDto
    ) {
        AccountResponseDto response = accountService.createAccount(accountReqDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/balance")
    public ResponseEntity<BalanceDTO> getBalance() {
        BalanceDTO response = accountService.Balance(new BalanceDTO(null));

        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/verify")
    public ResponseEntity<UserDetailByPhoneDTO> verifyUser(@Valid @RequestBody FindUserByPhoneDTO req) {
        UserDetailByPhoneDTO response = accountService.verifyUserByPhone(req);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/user-account/{id}")
    public ResponseEntity<UserAccountProjection> getUserAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.bankDetails(id));
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/login-details/{id}")
    public ResponseEntity<UserLoginResponseDetail>getUserLoginDetails(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.getUserLoginDetails(id)
        );
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/graph/stats")
    public ResponseEntity<BankStats> getBankStats() {
        return ResponseEntity.ok(accountService.getBankStats());
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/total/stats")
    public ResponseEntity<BankAdminStatsProjection> getAdminBankStats() {
        return ResponseEntity.ok(accountService.getAdminBankStats());
    }
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/status/{email}")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable String email,
            @RequestBody UpdateUserStatusRequest request) {

        accountService.updateUserStatus(email, request.status());

        return ResponseEntity.ok("User status updated successfully");
    }
    @PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {

        String imagePath = accountService.saveImage(userId, file);

        return ResponseEntity.ok(
                new ImageUploadResponse(
                        "Profile image uploaded successfully.",
                        imagePath
                )
        );
    }

}



//
//POST
///api/auth/verify-pin
//
//  "pin": "416151"
//          }
//true


//{
//        "email": "user@example.com",
//        "otp": "100270"
//        } POST
/// api/v1/otp/verify
//
//{
//  "verified": true,
//  "message": "string",
//  "accessToken": {
//    "id": 0,
//    "email": "string",
//    "username": "string",
//    "token": "string",
//    "type": "string",
//    "verification": "string",
//    "role": [
//      "string"
//    ]
//  }
//}
