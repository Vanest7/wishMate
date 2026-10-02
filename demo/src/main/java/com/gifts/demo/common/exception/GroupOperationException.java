package com.gifts.demo.common.exception;

public class GroupOperationException extends RuntimeException {
    public enum Reason {
        INVALID_INPUT, NOT_FOUND, FORBIDDEN, CONFLICT
    }

    private final Reason reason;

    public GroupOperationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}