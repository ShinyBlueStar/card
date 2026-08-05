package com.sample.system.card.service.domain.ports.input.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.ports.input.service.CardSecretService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.externalService.SSMService;
import com.sample.system.card.service.domain.ports.output.repository.CardRepository;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardSecretServiceImpl implements CardSecretService {

    private final CardRepository cardRepository;
    private final SSMService ssmService;
    private final ObjectMapper objectMapper;

    @Override
    public CardSecret generate(CardSecret cardSecret) throws CardDomainException {
        String methodName = "CardSecretServiceImpl.generate";
        log.info("{}: start, PAN={}, type={}", methodName, PanMaskingUtil.maskPan(cardSecret.getPan()),
                cardSecret.getSecretType());

        resolveAndSetCardId(cardSecret);
        log.info("{}: cardId resolved, cardId={}", methodName, cardSecret.getCardId());

        try {
            String plainValue = ssmService.callGenerate(cardSecret);
            log.info("{}: SSM generate call completed, type={}", methodName, cardSecret.getSecretType());
            cardSecret.setValue(plainValue);
            log.info("{}: secret generated successfully, type={}", methodName, cardSecret.getSecretType());
            return cardSecret;
        } catch (CardDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}: SSM generate failed: {}", methodName, e.getMessage(), e);
            throw new CardDomainException("SSM generate failed: " + e.getMessage(),
                    StatusService.SSM_SERVICE_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CardSecret validate(CardSecret cardSecret) throws CardDomainException {
        String methodName = "CardSecretServiceImpl.validate";
        log.info("{}: start, PAN={}, type={}", methodName, PanMaskingUtil.maskPan(cardSecret.getPan()),
                cardSecret.getSecretType());

        resolveAndSetCardId(cardSecret);
        log.info("{}: cardId resolved, cardId={}", methodName, cardSecret.getCardId());
        try {
            String ssmResponse = ssmService.callValidate(cardSecret);
            log.info("{}: SSM validate call completed", methodName);

            JsonNode root = objectMapper.readTree(ssmResponse);
            JsonNode data = root != null ? root.get("data") : null;
            if (data == null) {
                log.error("{}: SSM validate response missing data", methodName);
                throw new CardDomainException("SSM validate returned invalid response",
                        StatusService.SSM_SERVICE_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            boolean valid = data.has("valid") && data.get("valid").asBoolean();
            String message = data.has("message") ? data.get("message").asText() :
                    (valid ? "Verification succeeded" : "Verification failed");

            cardSecret.setValid(valid);
            cardSecret.setMessage(message);
            log.info("{}: validation result, valid={}, message={}", methodName, valid, message);

            return cardSecret;

        } catch (CardDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}: SSM validate failed: {}", methodName, e.getMessage(), e);
            throw new CardDomainException("SSM validate failed: " + e.getMessage(),
                    StatusService.SSM_SERVICE_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Resolve cardId and nationalId from PAN and set on cardSecret. */
    private void resolveAndSetCardId(CardSecret cardSecret) throws CardDomainException {
        log.info("CardSecretServiceImpl.resolveAndSetCardId: resolving cardId for PAN");
        Card card = cardRepository.findByPan(cardSecret.getPan())
                .orElseThrow(() -> {
                    log.error("Card not found for PAN: {}", PanMaskingUtil.maskPan(cardSecret.getPan()));
                    return new CardDomainException("Card not found for PAN: " + PanMaskingUtil.maskPan(cardSecret.getPan()),
                            StatusService.CARD_NOT_FOUND,
                            HttpStatus.NOT_FOUND);
                });

        if (card.getCardUuid() == null) {
            log.info("CARD_ID (UUID) not found for PAN: {}", cardSecret.getPan());
            throw new CardDomainException("Card UUID not found for PAN: " + cardSecret.getPan(),
                    StatusService.CARD_ID_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        cardSecret.setCardId(card.getCardUuid());
        if (card.getCustomer() != null && card.getCustomer().getNationalId() != null) {
            cardSecret.setNationalId(card.getCustomer().getNationalId());
        }
    }
}

