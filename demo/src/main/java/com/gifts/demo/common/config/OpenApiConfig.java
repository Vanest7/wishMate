package com.gifts.demo.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String CSRF_TOKEN = "csrfToken";

    @Bean
    public OpenAPI giftsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WishMate API")
                        .description(
                                "API for managing groups, events, wishes "
                                        + "and gift reservations."
                        )
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Access token returned by POST /api/auth/login."
                                        )
                        )
                        .addSecuritySchemes(
                                CSRF_TOKEN,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-XSRF-TOKEN")
                                        .description(
                                                "Token returned by GET /api/auth/csrf. "
                                                        + "The matching CSRF cookie must "
                                                        + "also accompany the request."
                                        )
                        ));
    }

    @Bean
    public OpenApiCustomizer operationSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) -> {
                if (!path.startsWith("/api/")) {
                    return;
                }

                pathItem.readOperationsMap().forEach((method, operation) -> {
                    boolean registration =
                            method == PathItem.HttpMethod.POST
                                    && "/api/auth/register".equals(path);

                    boolean csrfEndpoint =
                            method == PathItem.HttpMethod.GET
                                    && "/api/auth/csrf".equals(path);

                    boolean login =
                            method == PathItem.HttpMethod.POST
                                    && "/api/auth/login".equals(path);

                    if (registration || csrfEndpoint) {
                        operation.setSecurity(List.of());
                        return;
                    }

                    SecurityRequirement requirement =
                            new SecurityRequirement();

                    if (!login) {
                        requirement.addList(BEARER_AUTH);
                    }

                    boolean safeMethod = switch (method) {
                        case GET, HEAD, OPTIONS, TRACE -> true;
                        default -> false;
                    };

                    if (!safeMethod) {
                        requirement.addList(CSRF_TOKEN);
                    }

                    operation.setSecurity(List.of(requirement));
                });
            });
        };
    }
}