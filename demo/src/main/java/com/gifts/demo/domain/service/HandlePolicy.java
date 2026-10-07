package com.gifts.demo.domain.service;

import com.gifts.demo.common.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class HandlePolicy {

    private static final Pattern VALID_HANDLE = Pattern.compile("[a-z0-9._]{3,30}");

    private HandlePolicy() {
    }

    public static String normalize(String handle) {
        if (handle == null) {
            throw new ValidationException("Handle is required");
        }

        String normalized = handle.strip().toLowerCase(Locale.ROOT);
        if (!VALID_HANDLE.matcher(normalized).matches()) {
            throw new ValidationException(
                    "Handle must contain 3 to 30 lowercase ASCII letters, numbers, dots or underscores"
            );
        }

        return normalized;
    }
}
