package com.fintech.leanbackend.dto;

import java.math.BigDecimal;

public record BankStats (
        BigDecimal totalBalance,
        Long totalUsers,
        Long totalSuspendedUsers,
        Long totalUnverifiedUsers,
        Long totalVerifiedUsers
){
}
