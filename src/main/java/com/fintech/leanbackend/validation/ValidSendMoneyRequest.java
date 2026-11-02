package com.fintech.leanbackend.validation;

import jakarta.validation.Payload;

public @interface ValidSendMoneyRequest {
    String message() default "Either email or phone number must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
