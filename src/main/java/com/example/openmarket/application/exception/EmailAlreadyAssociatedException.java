package com.example.openmarket.application.exception;

public class EmailAlreadyAssociatedException extends DomainException{
    private static final String BASE_MESSAGE = "Email already registered: ";

    public EmailAlreadyAssociatedException(String email) {
        super(BASE_MESSAGE + email);

    }
}
