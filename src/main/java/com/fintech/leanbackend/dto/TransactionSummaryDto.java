package com.fintech.leanbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionSummaryDto {
    Long getTransaction_id();
    Double getAmount();
    java.time.LocalDateTime getCreated_at();
    String getTransaction_type();

    String getSender_name();
    String getSender_mi();
    String getReceiver_name();
    String getReceiver_mi();

    // Existing field
    String getUser_role();
}
