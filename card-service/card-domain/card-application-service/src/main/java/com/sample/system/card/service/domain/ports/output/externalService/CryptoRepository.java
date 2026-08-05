package com.sample.system.card.service.domain.ports.output.externalService;

public interface CryptoRepository {

    String encrypt(String keyName, byte[] plaintext);
    byte[] decrypt(String keyName, String ciphertext);
}
