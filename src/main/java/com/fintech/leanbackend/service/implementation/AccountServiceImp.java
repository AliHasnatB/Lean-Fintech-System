package com.fintech.leanbackend.service.implementation;

import com.fintech.leanbackend.dto.*;
import com.fintech.leanbackend.entites.Account;
import com.fintech.leanbackend.entites.Status;
import com.fintech.leanbackend.entites.Users;
import com.fintech.leanbackend.entites.Verification;
import com.fintech.leanbackend.exceptions.ResourceNotFoundException;
import com.fintech.leanbackend.repository.AccountRepository;
import com.fintech.leanbackend.repository.UserRepository;
import com.fintech.leanbackend.service.AccountService;
import com.fintech.leanbackend.utils.Imageutils;
import com.fintech.leanbackend.utils.FileUploadConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.fintech.leanbackend.utils.KeyGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class AccountServiceImp implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final Imageutils imageutils;

    @Override
    @Transactional
    public AccountResponseDto createAccount(AccountReqDto accountReqDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String email = auth.getName();
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });
        if (accountRepository.existsByUserId(user.getId())) {
            log.error("Account with id {} already exists", user.getId());
            throw new ResourceNotFoundException("Account", "id", user.getId());
        }

        Account account = new Account();
        account.setBalance(BigDecimal.ZERO);
        account.setAddress(accountReqDto.getAddress());
        account.setPhoneNumber(accountReqDto.getPhoneNumber());
        user.setStatuses(Status.ACTIVE);
        user.setMi(KeyGenerator.generateCustomKey());
        user.setVerification(Verification.VERIFIED);
        account.setUser(user);
        Account saved = accountRepository.save(account);
        log.info("Account created successfully. AccountId: {}, UserId: {}", saved.getId(), user.getId());
        return modelMapper.map(saved, AccountResponseDto.class);
    }

    @Override
    public BalanceDTO Balance(BalanceDTO req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String email = auth.getName();
        Users user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User"));
        Account account = this.accountRepository.findByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("User"));
        BigDecimal balance = account.getBalance();
        return new BalanceDTO(balance);
    }

    @Override
    public UserDetailByPhoneDTO verifyUserByPhone(FindUserByPhoneDTO req) {
        Account account = null;
        Users detail = null;

        if (req.phoneNumber() != null && !req.phoneNumber().trim().isEmpty()) {
            account = this.accountRepository.findByPhoneNumber(req.phoneNumber()).orElse(null);
            if (account != null) {
                detail = this.userRepository.findById(account.getId()).orElse(null);
            }
        }

        if (account == null && req.email() != null && !req.email().trim().isEmpty()) {
            detail = this.userRepository.findByEmail(req.email()).orElse(null);

            if (detail != null) {
                account = this.accountRepository.findById(detail.getId()).orElse(null);
            }
        }
        if (account == null || detail == null) {
            throw new UsernameNotFoundException("User not found with the provided phone number or email");
        }

        String actualPhoneNumber = account.getPhoneNumber();
        if (actualPhoneNumber == null || actualPhoneNumber.trim().isEmpty()) {
            throw new IllegalStateException("Phone number is missing for this account. Cannot generate reversed username.");
        }
        String reversedPhoneAccount = new StringBuilder(actualPhoneNumber).reverse().toString();
        return new UserDetailByPhoneDTO(
                detail.getName(),
                reversedPhoneAccount
        );
    }

    @Override
    public UserAccountProjection bankDetails(Long id) {
        return this.accountRepository.getUserAccountDetails(id);
    }

    @Override
    public String saveImage(Long userId, MultipartFile file) throws IOException {

        // Find account
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Validate uploaded file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No image was uploaded.");
        }

        // Validate file size
        if (file.getSize() > FileUploadConstants.MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image size must not exceed 5 MB.");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        // Validate extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null) {
            int index = originalFilename.lastIndexOf('.');
            if (index != -1) {
                extension = originalFilename.substring(index).toLowerCase();
            }
        }

        if (!FileUploadConstants.ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported image format. Allowed formats: JPG, JPEG, PNG, GIF, WEBP."
            );
        }

        // Verify image content
        try (InputStream validationStream = file.getInputStream()) {
            if (ImageIO.read(validationStream) == null) {
                throw new IllegalArgumentException("The uploaded file is not a valid image.");
            }
        }

        // Create upload directory
        Path uploadDirectory = Paths.get(imageutils.getUploadDir(), "profiles");
        Files.createDirectories(uploadDirectory);

        // Delete old profile image if it exists
        if (account.getUser().getUserImage() != null && !account.getUser().getUserImage().isBlank()) {

            Path oldImage = Paths.get(imageutils.getUploadDir(), account.getUser().getUserImage());

            try {
                Files.deleteIfExists(oldImage);
            } catch (IOException e) {
                // Optional: log the exception instead of failing the upload
                e.printStackTrace();
            }
        }

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + extension;

        Path destination = uploadDirectory.resolve(fileName);

        // Save image
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        // Relative path to store in database
        String imagePath = "profiles/" + fileName;
        account.getUser().setUserImage(imagePath);
        accountRepository.save(account);

        return imagePath;
    }

    @Override
    public UserLoginResponseDetail getUserLoginDetails(Long id) {
        return userRepository.findUserLoginDetails(id);
    }

    @Override
    public void updateUserStatus(String email, Status status) {
        int updated = userRepository.updateStatusByEmail(email, status);

        if (updated == 0) {
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    @Override
    public BankStats getBankStats() {
        return userRepository.getBankStats();    }

    @Override
    public BankAdminStatsProjection getAdminBankStats() {
        return userRepository.getAdminankStats();
    }
}

