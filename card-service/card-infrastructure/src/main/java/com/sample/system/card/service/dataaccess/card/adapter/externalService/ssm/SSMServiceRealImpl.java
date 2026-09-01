package com.sample.system.card.service.dataaccess.card.adapter.externalService.ssm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.dataaccess.thirdparty.ssm.SsmChannel;
import com.sample.system.card.service.dataaccess.thirdparty.ssm.SsmSessionContext;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.entity.Session;
import com.sample.system.card.service.domain.enums.Channel;
import com.sample.system.card.service.domain.enums.SecretType;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.externalService.CryptoRepository;
import com.sample.system.card.service.domain.ports.output.externalService.SSMService;
import com.sample.system.card.service.domain.ports.output.repository.SessionRepository;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class SSMServiceRealImpl implements SSMService {

    private static final String VAULT_CIPHERTEXT_PREFIX = "vault:v1:";
    private static final long SESSION_DURATION_SECONDS = 1800; // 30 minutes

    private final SsmChannel ssmChannel;
    private final ObjectMapper objectMapper;
    private final SsmResponseDecryptor ssmResponseDecryptor;
    private final SessionRepository sessionRepository;

    @Value("${ssm.response.decrypt.vaultKeyName:}")
    private String ssmResponseVaultKeyName;

    @Value("${vault.pin-encrypt-key:}")
    private String vaultPinEncryptKey;
    @Value("${vault.cvv2-encrypt-key:}")
    private String vaultCvv2EncryptKey;
    @Value("${vault.otp-encrypt-key:}")
    private String vaultOtpEncryptKey;

    private CryptoRepository cryptoRepository;

    @Autowired(required = false)
    public void setCryptoRepository(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public String callGenerate(CardSecret cardSecret) throws CardDomainException {
        UUID cardId = cardSecret.getCardId();
        if (cardId == null) {
            throw new CardDomainException("CardId is required for generate", 6, HttpStatus.BAD_REQUEST);
        }
        UUID sessionId = resolveSessionId(cardId, "CARD_ISSUANCE");
        try {
            SsmSessionContext.setCurrentSessionId(sessionId);
            log.info("SSMServiceRealImpl.callGenerate: sessionId={}, cardId={}", sessionId, cardId);
            String response = ssmChannel.callGenerate(prepareCardSecretForGenerate(cardSecret));
            log.info("SSMServiceRealImpl.callGenerate: completed, sessionId={}", sessionId);
            return extractAndReturnPlainValueFromGenerateResponse(response, cardSecret);
        } catch (ThirdPartyException e) {
            log.error("SSM callGenerate failed, sessionId={}", sessionId, e);
            throw toCardDomainException(e, "SSM generate failed");
        } finally {
            SsmSessionContext.clear();
        }
    }

    @Override
    public String callValidate(CardSecret cardSecret) throws CardDomainException {
        UUID cardId = cardSecret.getCardId();
        if (cardId == null) {
            throw new CardDomainException("CardId is required for validate", StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        UUID sessionId = resolveSessionId(cardId, "VALIDATION");
        try {
            SsmSessionContext.setCurrentSessionId(sessionId);
            log.info("SSMServiceRealImpl.callValidate: sessionId={}, cardId={}", sessionId, cardId);
            CardSecret toSend = prepareCardSecretForValidate(cardSecret);
            String response = ssmChannel.callValidate(toSend);
            log.info("SSMServiceRealImpl.callValidate: completed, sessionId={}", sessionId);
            return ensurePlainValueInResponse(response, cardSecret);
        } catch (ThirdPartyException e) {
            log.error("SSM callValidate failed, sessionId={}", sessionId, e);
            throw toCardDomainException(e, "SSM validate failed");
        } catch (Exception e) {
            throw new CardDomainException("problem in decryption secret data", StatusService.SSM_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            SsmSessionContext.clear();
        }
    }

    private CardSecret prepareCardSecretForValidate(CardSecret cardSecret) {
        log.info("prepare card secret data for sending to validate for type: {}", cardSecret.getSecretType());
        SecretType type = cardSecret.getSecretType();
        if (type != SecretType.PIN && type != SecretType.OTP && type != SecretType.CVV) {
            return cardSecret;
        }
        if (cryptoRepository == null) {
            return cardSecret;
        }
        String keyName = getVaultKeyName(type);
        if (keyName == null || keyName.isBlank()) {
            return cardSecret;
        }
        String value = cardSecret.getValue();
        if (value == null || value.isBlank()) {
            return cardSecret;
        }
        try {
            byte[] plainBytes = value.getBytes(StandardCharsets.UTF_8);
            String ciphertext = cryptoRepository.encrypt(keyName, plainBytes);
            return CardSecret.builder()
                    .pan(cardSecret.getPan())
                    .secretType(cardSecret.getSecretType())
                    .value(ciphertext)
                    .channel(cardSecret.getChannel())
                    .sessionType(cardSecret.getSessionType())
                    .clientFingerprint(cardSecret.getClientFingerprint())
                    .cardId(cardSecret.getCardId())
                    .nationalId(cardSecret.getNationalId())
                    .build();
        } catch (Exception e) {
            log.warn("Vault encrypt failed for validate (type={}), sending plain value: {}", type, e.getMessage());
            return cardSecret;
        }
    }

    private CardSecret prepareCardSecretForGenerate(CardSecret cardSecret) {
        log.info("prepare card secret data for sending to generate for type: {}", cardSecret.getSecretType());
        SecretType type = cardSecret.getSecretType();
        if (cryptoRepository == null) {
            return cardSecret;
        }
        String keyName = getVaultKeyName(type);
        if (keyName == null || keyName.isBlank()) {
            return cardSecret;
        }
        String value = cardSecret.getEncryptedData();
        if (value == null || value.isBlank()) {
            return cardSecret;
        }
        try {
            byte[] plainBytes = value.getBytes(StandardCharsets.UTF_8);
            String ciphertext = cryptoRepository.encrypt(keyName, plainBytes);
            return CardSecret.builder()
                    .pan(cardSecret.getPan())
                    .secretType(cardSecret.getSecretType())
                    .encryptedData(ciphertext)
                    .channel(cardSecret.getChannel())
                    .sessionType(cardSecret.getSessionType())
                    .clientFingerprint(cardSecret.getClientFingerprint())
                    .cardId(cardSecret.getCardId())
                    .nationalId(cardSecret.getNationalId())
                    .build();
        } catch (Exception e) {
            log.warn("Vault encrypt failed for validate (type={}), sending plain value: {}", type, e.getMessage());
            return cardSecret;
        }
    }

    private String extractAndReturnPlainValueFromGenerateResponse(String response, CardSecret cardSecret) throws CardDomainException {
        if (response == null || response.isBlank()) {
            throw new CardDomainException("SSM generate returned empty response", StatusService.SSM_GENERATE_NULL_VALUE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        JsonNode root = null;
        try {
            root = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new CardDomainException("SSM generate returned invalid response: missing data.value", StatusService.SSM_GENERATE_NULL_VALUE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        JsonNode data = root != null ? root.get("data") : null;
        if (data == null || !data.has("value")) {
            log.error("SSM generate response missing data.value");
            throw new CardDomainException("SSM generate returned invalid response: missing data.value", StatusService.SSM_GENERATE_NULL_VALUE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String value = data.get("value").asText();
        if (value == null || value.isBlank()) {
            throw new CardDomainException("SSM generate returned empty value", StatusService.SSM_GENERATE_NULL_VALUE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        SecretType type = cardSecret != null ? cardSecret.getSecretType() : null;
        return decryptValueToPlain(type, value);
    }

    private UUID resolveSessionId(UUID cardId, String sessionType) throws CardDomainException {
        Optional<Session> existingOpt = sessionRepository.findActiveByCardId(cardId);
        Instant now = Instant.now();

        if (existingOpt.isPresent()) {
            Session existing = existingOpt.get();
            if (existing.getExpireAt() != null && existing.getExpireAt().isBefore(now)) {
                log.info("SSMServiceRealImpl: session expired for cardId={}, invalidating and creating new", cardId);
                invalidateSession(existing.getSessionId());
                sessionRepository.invalidate(existing.getSessionId());
                return createAndSaveNewSession(cardId, sessionType);
            }
            log.debug("SSMServiceRealImpl: using existing valid session for cardId={}, sessionId={}", cardId, existing.getSessionId());
            return existing.getSessionId();
        }

        return createAndSaveNewSession(cardId, sessionType);
    }

    private UUID createAndSaveNewSession(UUID cardId, String sessionType) throws CardDomainException {
        UUID sessionId = sessionRepository.createSessionInSsm(cardId);
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(SESSION_DURATION_SECONDS);
        Session session = Session.builder()
                .sessionId(sessionId)
                .cardId(cardId)
                .channel(Channel.API)
                .sessionType(sessionType != null ? sessionType : "CARD_ISSUANCE")
                .createdAt(now)
                .expireAt(expireAt)
                .active(true)
                .build();
        sessionRepository.save(session);
        log.info("SSMServiceRealImpl: new session created and saved for cardId={}, sessionId={}", cardId, sessionId);
        return sessionId;
    }

    @Override
    public Session getOrCreateSession(UUID cardId, String channel, String sessionType, String clientFingerprint) throws CardDomainException {
        UUID sessionId = resolveSessionId(cardId, sessionType != null ? sessionType : "CARD_ISSUANCE");
        return sessionRepository.findBySessionId(sessionId).orElseGet(() -> Session.builder()
                .sessionId(sessionId)
                .cardId(cardId)
                .channel(Channel.API)
                .sessionType(sessionType)
                .createdAt(Instant.now())
                .expireAt(Instant.now().plusSeconds(SESSION_DURATION_SECONDS))
                .active(true)
                .build());
    }

    @Override
    public UUID validateAndGetCardId(UUID sessionId) throws CardDomainException {
        return sessionRepository.findBySessionId(sessionId)
                .filter(s -> s.getExpireAt() == null || !s.getExpireAt().isBefore(Instant.now()))
                .filter(Session::isActive)
                .map(Session::getCardId)
                .orElseThrow(() -> new CardDomainException("Session invalid or expired: " + sessionId, 401, HttpStatus.UNAUTHORIZED));
    }

    @Override
    public void invalidateSession(UUID sessionId) {
        if (sessionId != null) {
            sessionRepository.invalidateSessionInSsm(sessionId);
            sessionRepository.invalidate(sessionId);
            log.info("SSMServiceRealImpl.invalidateSession: sessionId={}", sessionId);
        }
    }

    private String ensurePlainValueInResponse(String response, CardSecret cardSecret) throws Exception {
        if (response == null || response.isBlank()) return response;
        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root != null ? root.get("data") : null;
        if (data == null || !data.has("value")) return response;
        String value = data.get("value").asText();
        if (value == null || value.isBlank()) return response;
        SecretType type = cardSecret != null ? cardSecret.getSecretType() : null;
        String plain = decryptValueToPlain(type, value);
        if (plain.equals(value)) return response;
        ObjectNode rootNode = (ObjectNode) root;
        JsonNode dataNode = rootNode.get("data");
        if (dataNode != null && dataNode.isObject()) {
            ((ObjectNode) dataNode).put("value", plain);
        }
        return objectMapper.writeValueAsString(rootNode);
    }

    private String getVaultKeyName(SecretType type) {
        if (type == null) return ssmResponseVaultKeyName != null ? ssmResponseVaultKeyName.trim() : "";
        return switch (type) {
            case PIN -> vaultPinEncryptKey != null ? vaultPinEncryptKey.trim() : "";
            case CVV -> vaultCvv2EncryptKey != null ? vaultCvv2EncryptKey.trim() : "";
            case OTP -> vaultOtpEncryptKey != null ? vaultOtpEncryptKey.trim() : "";
        };
    }

    private String decryptValueToPlain(SecretType type, String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) return encryptedValue;
        String keyName = getVaultKeyName(type);
        if (encryptedValue.startsWith(VAULT_CIPHERTEXT_PREFIX) && cryptoRepository != null && !keyName.isBlank()) {
            try {
                byte[] plainBytes = cryptoRepository.decrypt(keyName, encryptedValue);
                return new String(plainBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("Vault decrypt failed for SSM value (type={}), falling back to SsmResponseDecryptor: {}", type, e.getMessage());
            }
        }
        return ssmResponseDecryptor.decrypt(encryptedValue);
    }

    private CardDomainException toCardDomainException(ThirdPartyException e, String fallbackMessage) {
        String body = e.getCompleteResponse();
        HttpStatus httpStatus = e.getHttpStatusCode() > 0
                ? HttpStatus.valueOf(e.getHttpStatusCode())
                : HttpStatus.BAD_GATEWAY;
        if (body != null && !body.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(body);
                JsonNode errorDetail = root != null ? root.get("errorDetail") : null;
                if (errorDetail != null) {
                    String code = errorDetail.has("code") ? errorDetail.get("code").asText() : null;
                    String message = errorDetail.has("message") ? errorDetail.get("message").asText() : null;
                    if (code != null || message != null) {
                        return new CardDomainException(
                                message != null ? message : fallbackMessage,
                                code != null ? code : String.valueOf(e.getResultCode()),
                                message != null ? message : e.getChannelMessage(),
                                httpStatus);
                    }
                }
            } catch (Exception parseEx) {
                log.debug("Could not parse SSM error body: {}", parseEx.getMessage());
            }
        }
        return new CardDomainException(
                fallbackMessage + ": " + e.getChannelMessage(),
                String.valueOf(e.getResultCode()),
                e.getChannelMessage() != null ? e.getChannelMessage() : fallbackMessage,
                httpStatus);
    }

    @Override
    public String getPin1(UUID cardId, String nationalCode, String cardNumber) throws CardDomainException {
        String methodName = "SSMServiceRealImpl.getPin1";
        log.info("{}: cardId={}, nationalCode=****, cardNumber={}", methodName, cardId, PanMaskingUtil.maskPan(cardNumber));
        CardSecret cardSecret = cardSecretForGenerate(cardId, cardNumber, SecretType.PIN, null, null);
        return callSsmGenerateAndDecrypt(methodName, cardId, cardSecret);
    }

    @Override
    public String getPin2(UUID cardId, String nationalCode, String cardNumber) throws CardDomainException {
        String methodName = "SSMServiceRealImpl.getPin2";
        log.info("{}: cardId={}, nationalCode=****, cardNumber={}", methodName, cardId, PanMaskingUtil.maskPan(cardNumber));
        CardSecret cardSecret = cardSecretForGenerate(cardId, cardNumber, SecretType.OTP, null, null);
        return callGenerate(cardSecret);
    }

    @Override
    public String getCvv2(UUID cardId, String cardNumber, int cvv2Length, String expTime, String serviceCode) throws CardDomainException {
        String methodName = "SSMServiceRealImpl.getCvv2";
        log.info("{}: cardId={}, cardNumber={}, cvv2Length={}", methodName, cardId, PanMaskingUtil.maskPan(cardNumber), cvv2Length);
        CardSecret cardSecret = cardSecretForGenerate(cardId, cardNumber, SecretType.CVV, expTime, serviceCode);
        return callGenerate(cardSecret);
    }

    private CardSecret cardSecretForGenerate(UUID cardId, String cardNumber, SecretType type, String expTime, String serviceCode) {
        return CardSecret.builder()
                .pan(cardNumber)
                .secretType(type)
                .expTime(expTime)
                .serviceCode(serviceCode)
                .cardId(cardId)
                .encryptedData(type == SecretType.CVV? cardNumber+expTime+serviceCode:null)
                .reqType("generate")
                .build();
    }

    private String callSsmGenerateAndDecrypt(String methodName, UUID cardId, CardSecret cardSecret) throws CardDomainException {
        UUID sessionId;
        try {
            sessionId = ssmChannel.createSession(cardId);
        } catch (ThirdPartyException e) {
            log.error("{}: SSM createSession failed, httpStatus={}, message={}", methodName, e.getHttpStatusCode(), e.getMessage());
            throw new RuntimeException("SSM session creation failed: " + e.getChannelMessage(), e);
        }
        try {
            SsmSessionContext.setCurrentSessionId(sessionId);
            String responseBody = ssmChannel.callGenerate(cardSecret);
            String plainValue = extractAndReturnPlainValueFromGenerateResponse(responseBody, cardSecret);
            log.info("{}: SSM value decrypted successfully, type={}", methodName, cardSecret.getSecretType());
            return plainValue;
        } catch (CardDomainException e) {
            throw new CardDomainException("SSM generate failed: " + e.getMessage(), StatusService.CARD_SECRET_GENERATION_FAILED, HttpStatus.BAD_REQUEST);
        } catch (ThirdPartyException e) {
            log.error("{}: SSM generate failed, type={}, httpStatus={}, message={}", methodName, cardSecret.getSecretType(), e.getHttpStatusCode(), e.getMessage());
            throw new RuntimeException("SSM generate failed: " + e.getChannelMessage(), e);
        } catch (Exception e) {
            log.error("{}: SSM generate error, type={}: {}", methodName, cardSecret.getSecretType(), e.getMessage(), e);
            throw new RuntimeException("SSM generate failed: " + e.getMessage(), e);
        } finally {
            SsmSessionContext.clear();
            try {
                ssmChannel.invalidateSession(sessionId);
            } catch (Exception e) {
                log.warn("{}: SSM session invalidation failed (non-fatal), sessionId={}: {}", methodName, sessionId, e.getMessage());
            }
        }
    }
}
