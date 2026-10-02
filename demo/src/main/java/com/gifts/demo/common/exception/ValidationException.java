package com.gifts.demo.common.exception;

public class ValidationException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Validation error";
    public ValidationException(String message) {

        super(DEFAULT_MESSAGE +". "+message);
    }
}
