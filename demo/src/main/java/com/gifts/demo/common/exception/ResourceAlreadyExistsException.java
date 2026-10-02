package com.gifts.demo.common.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Resource already exists";

    public ResourceAlreadyExistsException(String message) {
        super(DEFAULT_MESSAGE + ". " +message);
    }
}
