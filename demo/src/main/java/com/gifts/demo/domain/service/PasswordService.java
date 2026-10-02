package com.gifts.demo.domain.service;

public interface PasswordService {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}