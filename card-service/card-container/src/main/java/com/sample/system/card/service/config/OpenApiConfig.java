package com.sample.system.card.service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String OAUTH2_SCHEME = "oauth2Password";

    @Bean
    public OpenAPI swaggerOpenApi(
            @Value("${swagger.oauth.token-url}") String tokenUrl,
            @Value("${swagger.oauth.client-id:}") String clientId,
            @Value("${swagger.oauth.client-secret:}") String clientSecret) {
        OAuthFlow passwordFlow = new OAuthFlow()
                .tokenUrl(tokenUrl)
                .scopes(new Scopes().addString("", ""));
        passwordFlow.addExtension("x-client-id", clientId);
        passwordFlow.addExtension("x-client-secret", clientSecret);

        SecurityScheme passwordScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().password(passwordFlow));

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(OAUTH2_SCHEME, passwordScheme))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME, ""));
    }

}
