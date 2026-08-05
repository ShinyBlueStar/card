package com.sample.system.card.service.dataaccess.card.adapter.externalService;

import com.sample.system.card.service.domain.ports.output.externalService.CryptoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnBean(VaultOperations.class)
@RequiredArgsConstructor
public class VaultTransitCryptoRepositoryImpl implements CryptoRepository {

    @Value("${vault.transitMount:transit}")
    private String transitMount;
    private final VaultOperations vaultOperations;

    @Override
    public String encrypt(String keyName, byte[] plaintext) {
        Assert.hasText(keyName, "keyName must not be empty");
        Assert.notNull(plaintext, "plaintext must not be null");

        String base64Plaintext = Base64.getEncoder().encodeToString(plaintext);
        String path = buildTransitPath("encrypt", keyName);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("plaintext", base64Plaintext);
        VaultResponse response = vaultOperations.write(path, requestBody);

        if (response == null || response.getData() == null || response.getData().get("ciphertext") == null) {
            throw new IllegalStateException("Vault encrypt operation failed for key: " + keyName);
        }

        return response.getData().get("ciphertext").toString();
    }

    @Override
    public byte[] decrypt(String keyName, String ciphertext) {
        Assert.hasText(keyName, "keyName must not be empty");
        Assert.hasText(ciphertext, "ciphertext must not be empty");

        String path = buildTransitPath("decrypt", keyName);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("ciphertext", ciphertext);
        VaultResponse response = vaultOperations.write(path, requestBody);

        if (response == null || response.getData() == null || response.getData().get("plaintext") == null) {
            throw new IllegalStateException("Vault decrypt operation failed for key: " + keyName);
        }

        String base64Plaintext = response.getData().get("plaintext").toString();
        return Base64.getDecoder().decode(base64Plaintext);
    }

    private String buildTransitPath(String operation, String keyName) {
        return transitMount + "/" + operation + "/" + keyName;
    }
}

