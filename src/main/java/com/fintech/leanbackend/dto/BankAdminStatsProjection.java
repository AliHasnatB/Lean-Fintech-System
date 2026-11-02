package com.fintech.leanbackend.dto;

import java.math.BigDecimal;

public interface BankAdminStatsProjection {

    BigDecimal getTotalBalance();

    Long getTotalUsers();

    Long getTotalSuspendedUsers();

    Long getTotalUnverifiedUsers();

    Long getTotalVerifiedUsers();
}