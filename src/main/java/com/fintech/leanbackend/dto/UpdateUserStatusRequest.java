package com.fintech.leanbackend.dto;

import com.fintech.leanbackend.entites.Status;

public record UpdateUserStatusRequest(
        Status status
) {
}
