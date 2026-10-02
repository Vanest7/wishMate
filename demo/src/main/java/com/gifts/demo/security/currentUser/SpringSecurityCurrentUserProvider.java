package com.gifts.demo.security.currentUser;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Integer getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || !jwtAuthentication.isAuthenticated()) {
            throw new InsufficientAuthenticationException(
                    "An authenticated JWT is required"
            );
        }

        String subject = jwtAuthentication.getToken().getSubject();

        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException(
                    "The token subject is missing"
            );
        }

        try {
            return Integer.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException(
                    "The token subject must be a valid user ID",
                    exception
            );
        }
    }
}