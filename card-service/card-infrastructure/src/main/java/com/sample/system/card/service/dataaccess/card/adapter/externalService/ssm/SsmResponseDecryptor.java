package com.sample.system.card.service.dataaccess.card.adapter.externalService.ssm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decrypts SSM response values (PIN/CVV/OTP) using the same shared key used by SSM/Vault for encryption.
 * <p>
 * Uses AES/CBC/PKCS5Padding. Expected ciphertext format from SSM: base64(IV_16_bytes + encryptedPayload).
 * Key must be the same symmetric key (base64-encoded, 16 or 32 bytes) used to encrypt on SSM side.
 * If no key is configured (e.g. dev), the value is returned as-is.
 * </p>
 */
@Slf4j
@Component
public class SsmResponseDecryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH_BYTES = 16;

    @Value("${ssm.response.decrypt.key:}")
    private String base64Key;

    /**
     * Decrypts the encrypted value from SSM into plain text.
     * If no key is configured, returns the value as-is (e.g. for dev when SSM returns plain).
     *
     * @param encryptedBase64 Base64-encoded ciphertext (IV + payload)
     * @return Decrypted plain string
     */
    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) {
            throw new IllegalArgumentException("Encrypted value must not be null or blank");
        }
        if (base64Key == null || base64Key.isBlank()) {
            log.debug("SSM decrypt key not configured; returning value as-is (plain or already decrypted)");
            return encryptedBase64;
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            if (keyBytes.length != 16 && keyBytes.length != 32) {
                throw new IllegalStateException("SSM decrypt key must be 16 (AES-128) or 32 (AES-256) bytes after base64 decode");
            }
            byte[] combined = Base64.getDecoder().decode(encryptedBase64.trim());
            if (combined.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Ciphertext too short (expected IV + payload)");
            }
            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTES];
            System.arraycopy(combined, IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] plain = cipher.doFinal(ciphertext);
            String plainText = new String(plain, StandardCharsets.UTF_8);
            log.debug("SSM response decrypted successfully");
            return plainText;
        } catch (Exception e) {
            log.error("SSM response decryption failed: {}", e.getMessage());
            throw new IllegalStateException("SSM response decryption failed: " + e.getMessage(), e);
        }
    }
}
