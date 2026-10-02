package com.gifts.demo.controller;

import com.gifts.demo.controller.mapper.UserWebMapper;
import com.gifts.demo.controller.webmodel.auth.CsrfResponse;
import com.gifts.demo.controller.webmodel.auth.LoginRequest;
import com.gifts.demo.controller.webmodel.auth.LoginResponse;
import com.gifts.demo.controller.webmodel.auth.RegisterRequest;
import com.gifts.demo.controller.webmodel.user.UserResponse;
import com.gifts.demo.domain.model.User;
import com.gifts.demo.domain.usecase.auth.AuthenticateUserUseCase;
import com.gifts.demo.domain.usecase.auth.RegisterUserUseCase;
import com.gifts.demo.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = registerUserUseCase.execute(
                request.userName(),
                request.email(),
                request.password()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserWebMapper.toResponse(user));
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> getCsrfToken(CsrfToken csrfToken) {
        CsrfResponse response = new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getToken()
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Integer userId = authenticateUserUseCase.execute(
                request.email(),
                request.password()
        );

        Jwt jwt = jwtService.createAccessToken(userId);

        long expiresIn = Duration.between(
                jwt.getIssuedAt(),
                jwt.getExpiresAt()
        ).getSeconds();

        LoginResponse response = new LoginResponse(
                jwt.getTokenValue(),
                "Bearer",
                expiresIn
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}