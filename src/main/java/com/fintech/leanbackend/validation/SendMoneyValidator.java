package com.fintech.leanbackend.validation;

import com.fintech.leanbackend.dto.SendMoneyDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class SendMoneyValidator implements ConstraintValidator<ValidSendMoneyRequest, SendMoneyDTO> {
    @Override
    public boolean isValid(SendMoneyDTO value, ConstraintValidatorContext context) {
        if(value == null){
            return true;
        }
        boolean email = StringUtils.hasText(value.email());
        boolean number = StringUtils.hasText(value.number());
        boolean creditCardNumber = StringUtils.hasText(value.CreditCardNumber());
        return email || number || creditCardNumber;
    }
}
