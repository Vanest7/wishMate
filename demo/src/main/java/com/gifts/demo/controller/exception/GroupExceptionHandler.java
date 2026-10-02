package com.gifts.demo.controller.exception;

import com.gifts.demo.common.exception.GroupOperationException;
import com.gifts.demo.controller.GroupController;
import com.gifts.demo.controller.GroupMemberController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {GroupController.class, GroupMemberController.class})
public class GroupExceptionHandler {
    public record ErrorResponse(String code, String message) {}

    @ExceptionHandler(GroupOperationException.class)
    public ResponseEntity<ErrorResponse> handleGroupOperation(GroupOperationException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status).body(
                new ErrorResponse(exception.getReason().name(), exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                "DATA_CONFLICT", "The operation conflicts with stored data"));
    }
}
