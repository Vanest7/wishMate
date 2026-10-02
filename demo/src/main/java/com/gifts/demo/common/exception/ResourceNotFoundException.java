package com.gifts.demo.common.exception;

public class ResourceNotFoundException extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "Resource not found";

    public ResourceNotFoundException(String message) {

      super(DEFAULT_MESSAGE + ". "+ message);
    }
}
