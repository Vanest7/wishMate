package com.gifts.demo.controller.webmodel.auth;

public record CsrfResponse(
        String headerName,
        String token
) {
}