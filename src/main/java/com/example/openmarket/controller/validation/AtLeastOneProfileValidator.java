package com.example.openmarket.controller.validation;

import com.example.openmarket.controller.dto.request.CreateAccountRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneProfileValidator implements ConstraintValidator<AtLeastOneProfile, CreateAccountRequest> {

    @Override
    public boolean isValid(CreateAccountRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        return request.getBuyerProfile() != null || request.getSellerProfile() != null;
    }
}
