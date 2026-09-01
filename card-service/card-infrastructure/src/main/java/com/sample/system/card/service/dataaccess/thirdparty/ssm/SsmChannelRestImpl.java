package com.sample.system.card.service.dataaccess.thirdparty.ssm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.dataaccess.thirdparty.WebCallUtils;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.enums.SecretType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.sample.system.card.service.dataaccess.thirdparty.WebCallUtils.sendRequest;

/**
 * پیاده‌سازی SsmChannel با REST؛ تنها لایهٔ HTTP به SSM (session، generate، validate).
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class SsmChannelRestImpl implements SsmChannel {

    private static final int[] SUCCESS_HTTP_STATUSES = {200, 201};
    private static final String SESSION_TYPE_ISSUANCE = "CARD_ISSUANCE";
    private static final String CHANNEL_API = "API";

    private final ObjectMapper objectMapper;

    @Value("${ssm.base.url:https://192.168.104.20:8009}")
    private String ssmBaseUrl;

    @Value("${ssm.timeout-seconds:1000}")
    private int timeoutSeconds;

    @Override
    public UUID createSession(UUID cardId) throws ThirdPartyException {
        log.info("SsmChannelRestImpl.createSession: cardId={}", cardId);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("cardId", cardId.toString());
            body.put("channel", CHANNEL_API);
            body.put("sessionType", SESSION_TYPE_ISSUANCE);
            body.put("clientFingerprint", "card-service-issuance");
            String requestBody = objectMapper.writeValueAsString(body);
            String responseBody = callSsm("SsmChannelRestImpl.createSession", HttpMethod.POST, "/api/v1/ssm/sessions", requestBody);
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root != null ? root.get("data") : null;
            if (data == null || !data.has("sessionId")) {
                throw new ThirdPartyException("SSM createSession returned invalid response: missing sessionId",
                        WebCallUtils.GENERAL_ERROR, "missing sessionId", -1, responseBody);
            }
            UUID sessionId = UUID.fromString(data.get("sessionId").asText());
            log.info("SsmChannelRestImpl.createSession: sessionId={}", sessionId);
            return sessionId;
        } catch (ThirdPartyException e) {
            throw e;
        } catch (Exception e) {
            log.error("SsmChannelRestImpl.createSession error: {}", e.getMessage(), e);
            throw new ThirdPartyException("SSM createSession failed: " + e.getMessage(),
                    WebCallUtils.GENERAL_ERROR, e.getMessage(), -1, "");
        }
    }

    @Override
    public void invalidateSession(UUID sessionId) throws ThirdPartyException {
        log.info("SsmChannelRestImpl.invalidateSession: sessionId={}", sessionId);
        try {
            callSsm("SsmChannelRestImpl.invalidateSession", HttpMethod.POST,
                    "/api/v1/ssm/sessions/" + sessionId + "/invalidate", null);
        } catch (Exception e) {
            log.warn("SsmChannelRestImpl.invalidateSession failed (non-fatal), sessionId={}: {}", sessionId, e.getMessage());
            throw new ThirdPartyException("SSM invalidateSession failed: " + e.getMessage(),
                    WebCallUtils.GENERAL_ERROR, e.getMessage(), -1, "");
        }
    }

    @Override
    public String callGenerate(CardSecret cardSecret) throws ThirdPartyException {
        UUID sessionId = SsmSessionContext.getCurrentSessionId();
        if (sessionId == null) {
            throw new ThirdPartyException("No session in context for SSM generate",
                    WebCallUtils.GENERAL_ERROR, "SsmSessionContext not set", -1, "");
        }
        log.info("Calling SSM generate - sessionId: {}, type: {}", sessionId, cardSecret != null ? cardSecret.getSecretType() : null);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("type", cardSecret.getSecretType().name());
            if (SecretType.CVV.equals(cardSecret.getSecretType())) {
                body.put("encryptedData", cardSecret.getEncryptedData());
            }
            String requestBody = objectMapper.writeValueAsString(body);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/vnd.api.v1+json");
            headers.put("X-Session-Id", sessionId.toString());

            URIBuilder uriBuilder = new URIBuilder(ssmBaseUrl + "/api/v1/ssm/card/generate");
            StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
            int timeoutMillis = timeoutSeconds * 1000;

            String response = sendRequest("SsmChannelRestImpl.callGenerate", HttpMethod.POST,
                    uriBuilder, timeoutMillis, headers, new HashMap<>(), entity, SUCCESS_HTTP_STATUSES);
            log.info("SsmChannelRestImpl.callGenerate: SSM generate response received");
            return response;
        } catch (ThirdPartyException e) {
            log.error("ThirdParty exception calling SSM generate: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SSM generate request build/call error: {}", e.getMessage(), e);
            throw new ThirdPartyException("SSM request failed: " + e.getMessage(),
                    WebCallUtils.GENERAL_ERROR, e.getMessage(), -1, "");
        }
    }

    @Override
    public String callValidate(CardSecret cardSecret) throws ThirdPartyException {
        UUID sessionId = SsmSessionContext.getCurrentSessionId();
        if (sessionId == null) {
            throw new ThirdPartyException("No session in context for SSM validate",
                    WebCallUtils.GENERAL_ERROR, "SsmSessionContext not set", -1, "");
        }
        log.info("Calling SSM validate - sessionId: {}, type: {}", sessionId, cardSecret != null ? cardSecret.getSecretType() : null);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("type", cardSecret.getSecretType().name());
            body.put("value", cardSecret.getValue());
            String requestBody = objectMapper.writeValueAsString(body);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/vnd.api.v1+json");
            headers.put("X-Session-Id", sessionId.toString());

            URIBuilder uriBuilder = new URIBuilder(ssmBaseUrl + "/api/v1/ssm/card/validate");
            StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
            int timeoutMillis = timeoutSeconds * 1000;

            String response = sendRequest("SsmChannelRestImpl.callValidate", HttpMethod.POST,
                    uriBuilder, timeoutMillis, headers, new HashMap<>(), entity, SUCCESS_HTTP_STATUSES);
            log.info("SsmChannelRestImpl.callValidate: SSM validate response received");
            return response;
        } catch (ThirdPartyException e) {
            log.error("ThirdParty exception calling SSM validate: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SSM validate request build/call error: {}", e.getMessage(), e);
            throw new ThirdPartyException("SSM request failed: " + e.getMessage(),
                    WebCallUtils.GENERAL_ERROR, e.getMessage(), -1, "");
        }
    }

    private String callSsm(String methodName, HttpMethod httpMethod, String endpoint, String requestBody) throws ThirdPartyException {
        try {
            URIBuilder uriBuilder = new URIBuilder(ssmBaseUrl + endpoint);
            StringEntity entity = requestBody != null ? new StringEntity(requestBody, StandardCharsets.UTF_8) : null;
            int timeoutMillis = timeoutSeconds * 1000;
            Map<String, String> headers = defaultHeaders();
            return sendRequest(methodName, httpMethod, uriBuilder, timeoutMillis, headers, new HashMap<>(), entity, SUCCESS_HTTP_STATUSES);
        } catch (Exception e) {
            log.error("{}: request build/call error: {}", methodName, e.getMessage(), e);
            throw new ThirdPartyException("SSM request failed: " + e.getMessage(),
                    WebCallUtils.GENERAL_ERROR, e.getMessage(), -1, "");
        }
    }

    private static Map<String, String> defaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/vnd.api.v1+json");
        return headers;
    }
}
