package com.gifts.demo.controller;

import com.gifts.demo.controller.exception.ApiExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name = "Authentication")
@RestController
@RequestMapping(
        value = "/api/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final JwtService jwtService;

    @Operation(summary = "Register a user",
            description = "Creates a user. This endpoint is exempt from CSRF validation.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered",
                    useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                    description = "The operation conflicts with stored data or a domain constraint. See code and message for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = registerUserUseCase.execute(
                request.userName(),
                request.handle(),
                request.email(),
                request.password()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserWebMapper.toResponse(user));
    }

    @Operation(summary = "Get a CSRF token",
            description = "Use the returned token as X-XSRF-TOKEN and retain the matching browser cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CSRF token returned",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Cache-Control", description = "Response must not be stored.",
                            schema = @Schema(type = "string", example = "no-store"))),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> getCsrfToken(
            @Parameter(hidden = true) CsrfToken csrfToken
    ) {
        CsrfResponse response = new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getToken()
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @Operation(summary = "Log in",
            description = "Requires the CSRF header and matching cookie. Returns a Bearer access token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Access token issued",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Cache-Control", description = "Response must not be stored.",
                            schema = @Schema(type = "string", example = "no-store"))),
        @ApiResponse(responseCode = "400",
                    description = "Invalid request body, identifier or business input. See code, message and fieldErrors for details.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                    description = "Invalid login credentials return ErrorResponse with code INVALID_CREDENTIALS. An invalid Bearer header can instead be rejected by Spring Security with a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "403",
                    description = "Access is denied or the CSRF token/cookie is missing or invalid. Application permission failures return ErrorResponse; Spring Security failures may have a different body.",
                    content = @Content),
        @ApiResponse(responseCode = "500",
                    description = "Unexpected application error handled by ApiExceptionHandler. Error code: INTERNAL_ERROR.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
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