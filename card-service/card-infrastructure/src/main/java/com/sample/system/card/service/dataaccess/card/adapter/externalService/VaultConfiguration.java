package com.sample.system.card.service.dataaccess.card.adapter.externalService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

/**
 * Configures HashiCorp Vault client when vault.uri and vault.token are set.
 * Enables encrypt/decrypt via Vault Transit (VaultTransitCryptoRepositoryImpl).
 */
@Configuration
@ConditionalOnProperty(name = "vault.uri")
public class VaultConfiguration {

    @Bean
    public VaultOperations vaultOperations(
            @Value("${vault.uri}") String uri,
            @Value("${vault.token:}") String token) {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(uri));
        TokenAuthentication auth = new TokenAuthentication(token);
        return new VaultTemplate(endpoint, auth);
    }
}
