package com.gifts.demo.common.exception;

public class ForbiddenOperationException
        extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "Operation not allowed";

    public ForbiddenOperationException(String message) {
        super(DEFAULT_MESSAGE + ". " + message);
    }
}