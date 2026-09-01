package com.sample.system.card.service.dataaccess.card.adapter.externalService.ssm;

import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.entity.Session;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.output.externalService.SSMService;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * Mock implementation of SSMService for testing/development.
 */
@Slf4j
@Service
public class SSMServiceMockImpl implements SSMService {

    private final Random random = new Random();

    @Override
    public String getPin1(UUID cardId, String nationalCode, String cardNumber) {
        log.info("Mock: Getting PIN1 for cardId={}, nationalCode=****, cardNumber={}", cardId, PanMaskingUtil.maskPan(cardNumber));
        return String.format("%04d", random.nextInt(10000));
    }

    @Override
    public String getPin2(UUID cardId, String nationalCode, String cardNumber) {
        log.info("Mock: Getting PIN2 for cardId={}, nationalCode=****, cardNumber={}", cardId, PanMaskingUtil.maskPan(cardNumber));
        return String.format("%04d", random.nextInt(10000));
    }

    @Override
    public String getCvv2(UUID cardId, String cardNumber,int cvv2Length, String serviceCode, String expTime) {
        log.info("Mock: Getting CVV1 for cardId={}, cardNumber={}, serviceCode={}, expTime={}",
                cardId, PanMaskingUtil.maskPan(cardNumber), serviceCode, expTime);
        if (cardNumber == null || cardNumber.isBlank()) {
            cardNumber = "0000000000000000";
        }
        if (expTime == null) {
            expTime = "";
        }
        String data = cardNumber + "|" + serviceCode + "|" + expTime;
        int hash = Math.abs(data.hashCode());
        int cvv1 = hash % 1000;
        return String.format("%03d", cvv1);
    }

    @Override
    public String callGenerate(CardSecret cardSecret) throws Exception {
        return "";
    }

    @Override
    public String callValidate(CardSecret cardSecret) throws Exception {
        return "";
    }

    @Override
    public Session getOrCreateSession(UUID cardId, String channel, String sessionType, String clientFingerprint) throws CardDomainException {
        return null;
    }

    @Override
    public UUID validateAndGetCardId(UUID sessionId) throws CardDomainException {
        return null;
    }

    @Override
    public void invalidateSession(UUID sessionId) {
    }
}

