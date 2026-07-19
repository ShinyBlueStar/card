package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.CardDomainService;
import com.sample.system.card.service.domain.command.numberPattern.GetAllCardNumberPatternsQuery;
import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.event.cardNumberPattern.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.CardNumberPatternService;
import com.sample.system.card.service.domain.ports.output.repository.CardNumberPatternRepository;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class CardNumberPatternServiceImpl implements CardNumberPatternService {

    private final CardNumberPatternRepository repository;
    private final CardDomainService cardDomainService;

    @Override
    public CardNumberPatternCreatedEvent createCardNumberPattern(CardNumberPattern cardNumberPattern) throws CardDomainException {
        log.info("Starting CardNumberPattern creation service - From: {}, To: {}, ProductCode: {}",
                cardNumberPattern.getCardNumberFrom(), cardNumberPattern.getCardNumberTo(), cardNumberPattern.getProductCode());

        try {
            // Validation 1: Profile ID should not be set during creation
            Long profileId = cardNumberPattern.getCardProfile() != null && cardNumberPattern.getCardProfile().getId() != null
                    ? cardNumberPattern.getCardProfile().getId().getValue()
                    : (cardNumberPattern.getCardProfileId() != null ? cardNumberPattern.getCardProfileId().getValue() : null);
            if (profileId != null) {
                log.error("CardProfileId should not be set during pattern creation. ProfileId: {}", profileId);
                throw new CardDomainException(
                        "Rejecting card number pattern creation because profileId was provided",
                        StatusService.DUPLICATE_PATTERN,
                        HttpStatus.BAD_REQUEST);
            }

            // Validation 2: Check if product code exists and validate generation method consistency
            String productCode = cardNumberPattern.getProductCode();

            List<CardNumberPattern> existingPatterns = repository.findByProductCode(productCode);
            if (!existingPatterns.isEmpty()) {
                // Check if generation methods are consistent
                CardNumGenerationMethod newMethod = cardNumberPattern.getCardNumGenerationMethod();
                if (newMethod != null) {
                    for (CardNumberPattern existingPattern : existingPatterns) {
                        if (existingPattern.getCardNumGenerationMethod() != null &&
                            !existingPattern.getCardNumGenerationMethod().equals(newMethod)) {
                            log.error("Different generation method detected for product code {}. Existing: {}, New: {}",
                                    productCode, existingPattern.getCardNumGenerationMethod(), newMethod);
                            throw logAndBuildException(
                                    "Rejecting card number pattern due to conflicting generation method for productCode " + productCode,
                                    String.format("برای کد محصول %s قبلاً متد تولید %s تعریف شده است. نمی‌توان متد تولید متفاوت (%s) تعریف کرد.",
                                            productCode,
                                            existingPattern.getCardNumGenerationMethod().getDescription(),
                                            newMethod.getDescription()),
                                    StatusService.INPUT_PARAMETER_NOT_VALID,
                                    HttpStatus.BAD_REQUEST);
                        }
                        if (existingPattern.getCardNumGenerationMethod() != null &&
                                existingPattern.getCardNumGenerationMethod().equals(newMethod)
                                && existingPattern.getProductCode().equals(cardNumberPattern.getProductCode())
                        ) {
                            throw new CardDomainException(
                                    "Rejecting card number pattern due to unique generation method and productCode " ,
                                    StatusService.DUPLICATE_PATTERN,
                                    HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            }

            // Validation 3: Check for range overlaps by product code
            var overlaps = repository.findOverlappingPatternsByProductCode(
                    null,
                    productCode,
                    cardNumberPattern.getCardNumberFrom(),
                    cardNumberPattern.getCardNumberTo());
            if (overlaps != null && !overlaps.isEmpty()) {
                log.error("Overlapping card number pattern detected for product code {}: {}-{}",
                        productCode, cardNumberPattern.getCardNumberFrom(), cardNumberPattern.getCardNumberTo());
                throw logAndBuildException(
                        "Rejecting card number pattern due to overlapping range for productCode " + productCode,
                        String.format("بازه شماره کارت %s تا %s برای کد محصول %s با بازه‌های موجود هم‌پوشانی دارد.",
                                cardNumberPattern.getCardNumberFrom(),
                                cardNumberPattern.getCardNumberTo(),
                                productCode),
                        StatusService.INPUT_PARAMETER_NOT_VALID,
                        HttpStatus.BAD_REQUEST);
            }

            cardDomainService.validateAndInitiateCardNumberPattern(cardNumberPattern);
            cardNumberPattern.setPatternStatus(CardNumberPatternStatus.ACTIVE);
            CardNumberPattern savedPattern = repository.save(cardNumberPattern);
            if (savedPattern == null) {
                log.error("Could not save CardNumberPattern: {} to {}", cardNumberPattern.getCardNumberFrom(), cardNumberPattern.getCardNumberTo());
                throw logAndBuildException(
                        "Repository returned null while saving card number pattern",
                        "Could not save CardNumberPattern: " + cardNumberPattern.getCardNumberFrom() + " to " + cardNumberPattern.getCardNumberTo(),
                        StatusService.GENERAL_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CardNumberPatternCreatedEvent event = new CardNumberPatternCreatedEvent(savedPattern);
            log.info("CardNumberPattern creation process completed successfully - PatternId: {}, From: {}, To: {}",
                    savedPattern.getId().getValue(), savedPattern.getCardNumberFrom(), savedPattern.getCardNumberTo());
            return event;

        } catch (CardDomainException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred during CardNumberPattern creation - From: {}, To: {}, Error: {}",
                    cardNumberPattern.getCardNumberFrom(), cardNumberPattern.getCardNumberTo(), e.getMessage(), e);
            throw logAndBuildException(
                    "Unexpected error while creating CardNumberPattern",
                    "خطا در ایجاد الگوی شماره کارت: " + e.getMessage(),
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CardNumberPatternUpdatedEvent updateCardNumberPattern(CardNumberPattern cardNumberPattern) throws CardDomainException {
        Long patternId = cardNumberPattern.getId() != null ? cardNumberPattern.getId().getValue() : null;
        if (patternId == null) {
            log.error("CardNumberPatternId is required for update operation");
            throw logAndBuildException(
                    "CardNumberPattern update rejected because id is null",
                    "CardNumberPatternId is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        log.info("Starting CardNumberPattern update service - PatternId: {}", patternId);

        try {
            CardNumberPattern current = repository.findById(patternId)
                    .orElseThrow(() -> logAndBuildException(
                            "CardNumberPattern not found for update. Id=" + patternId,
                            "CardNumberPattern not found: " + patternId,
                            StatusService.CARD_NUMBER_PATTERN_NOT_FOUND,
                            HttpStatus.NOT_FOUND));

            // Update current pattern with new values (only name, cardNumberFrom, cardNumberTo)
            updateCardNumberPatternFromModel(current, cardNumberPattern);

            // Persist updated pattern
            CardNumberPattern updated = repository.save(current);

            CardNumberPatternUpdatedEvent event = new CardNumberPatternUpdatedEvent(updated);
            log.info("CardNumberPattern update process completed successfully - PatternId: {}", updated.getId().getValue());
            return event;

        } catch (Exception e) {
            log.error("Error occurred during CardNumberPattern update - PatternId: {}, Error: {}",
                    patternId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CardNumberPatternUpdatedEvent deactivateCardNumberPattern(CardNumberPattern cardNumberPattern) throws CardDomainException {
        Long patternId = cardNumberPattern.getId() != null ? cardNumberPattern.getId().getValue() : null;
        if (patternId == null) {
            log.error("CardNumberPatternId is required for deactivate operation");
            throw new CardDomainException(
                    "CardNumberPattern deactivate rejected because id is null",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        log.info("Starting CardNumberPattern deactivate service - PatternId: {}", patternId);

        try {
            CardNumberPattern current = repository.findById(patternId)
                    .orElseThrow(() -> new CardDomainException(
                            "CardNumberPattern not found for deactivate. Id=" + patternId,
                            StatusService.CARD_NUMBER_PATTERN_NOT_FOUND,
                            HttpStatus.NOT_FOUND));

            // Set status to INACTIVE
            current.setPatternStatus(CardNumberPatternStatus.INACTIVE);

            // Persist deactivated pattern
            CardNumberPattern updated = repository.save(current);

            CardNumberPatternUpdatedEvent event = new CardNumberPatternUpdatedEvent(updated);
            log.info("CardNumberPattern deactivate process completed successfully - PatternId: {}", updated.getId().getValue());
            return event;

        } catch (Exception e) {
            log.error("Error occurred during CardNumberPattern deactivate - PatternId: {}, Error: {}",
                    patternId, e.getMessage(), e);
            throw e;
        }
    }

    private void updateCardNumberPatternFromModel(CardNumberPattern current, CardNumberPattern updates) {
        if (updates.getCardNumberFrom() != null) {
            current.setCardNumberFrom(updates.getCardNumberFrom());
        }
        if (updates.getCardNumberTo() != null) {
            current.setCardNumberTo(updates.getCardNumberTo());
        }
        if (updates.getName() != null) {
            current.setName(updates.getName());
        }
        if (updates.getPatternStatus() != null)
            current.setPatternStatus(updates.getPatternStatus());
        if (updates.getCardProfile() != null)
            current.setCardProfile(updates.getCardProfile());
        if (updates.getPatternStatus() !=null)
            current.setPatternStatus(updates.getPatternStatus());
    }

    @Override
    public CardNumberPattern getCardNumberPattern(GetCardNumberPatternQuery query) throws CardDomainException {
        log.info("Starting CardNumberPattern retrieval service - PatternId: {}", query.getPatternId());
        CardNumberPattern pattern = repository.findById(query.getPatternId())
                .orElseThrow(() -> new CardDomainException("CardNumberPattern not found: " + query.getPatternId(),
                        StatusService.CARD_NUMBER_PATTERN_NOT_FOUND,HttpStatus.BAD_REQUEST));
        log.info("CardNumberPattern retrieval completed successfully - PatternId: {}", query.getPatternId());
        return pattern;
    }

    @Override
    public List<CardNumberPattern> getAllCardNumberPatterns(GetAllCardNumberPatternsQuery query) throws CardDomainException {
        log.info("Starting CardNumberPatterns retrieval service - Status: {}, CardProfileId: {}, BelongTo: {}",
                query.getStatus(), query.getCardProfileId(), query.getBelongTo());

        try {
            Map<String, String> searchCriteria = new HashMap<>();
            if (query.getStatus() != null && !query.getStatus().isEmpty()) {
                searchCriteria.put("status", query.getStatus());
            }
            if (query.getCardProfileId() != null) {
                searchCriteria.put("cardProfileId", query.getCardProfileId().toString());
            }
            // Get all patterns without pagination for this query
            searchCriteria.put("size", "10000"); // Large size to get all
            Page<CardNumberPattern> allPatternsPage = repository.findAll(searchCriteria);
            List<CardNumberPattern> filteredPatterns = allPatternsPage.getContent();

            log.info("CardNumberPatterns retrieval completed successfully - Count: {}", filteredPatterns.size());
            return filteredPatterns;
        } catch (Exception e) {
            log.error("Error occurred in CardNumberPatterns retrieval service - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<CardNumberPattern> listCardNumberPatterns(Map<String, String> map, String caller, String ip) throws CardDomainException {
        log.info("Starting CardNumberPatterns list service with params: {}", map);

        try {
            Page<CardNumberPattern> pageResult = repository.findAll(map);
            log.info("CardNumberPatterns list service completed successfully - Total: {}, Page: {}",
                    pageResult.getTotalElements(), pageResult.getNumber());
            return pageResult;
        } catch (Exception e) {
            log.error("Error occurred in CardNumberPatterns list service - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<CardNumberPattern> getPatternsContainingCardNumber(String cardNumber) throws CardDomainException {
        log.info("Finding patterns containing card number: {}", cardNumber);

        try {
            Map<String, String> searchCriteria = new HashMap<>();
            // Get all patterns without pagination for this search
            searchCriteria.put("size", "10000"); // Large size to get all
            Page<CardNumberPattern> allPatternsPage = repository.findAll(searchCriteria);
            List<CardNumberPattern> allPatterns = allPatternsPage.getContent();
            List<CardNumberPattern> matchingPatterns = allPatterns.stream()
                    .filter(pattern -> pattern.contains(cardNumber))
                    .toList();

            log.info("Found {} patterns containing card number: {}", matchingPatterns.size(), cardNumber);
            return matchingPatterns;
        } catch (Exception e) {
            log.error("Error occurred in getPatternsContainingCardNumber service - CardNumber: {}, Error: {}",
                    cardNumber, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CardNumberPatternsFetchedEvent createCardNumberPatterns(
            List<CardNumberPattern> cardNumberPatterns) throws CardDomainException {
        log.info("Bulk creating {} CardNumberPatterns", cardNumberPatterns.size());

        try {
            for (CardNumberPattern pattern : cardNumberPatterns) {
                cardDomainService.validateAndInitiateCardNumberPattern(pattern);
            }
            List<CardNumberPattern> saved = repository.saveAll(cardNumberPatterns);

            log.info("Bulk created {} CardNumberPatterns", saved.size());
            return new CardNumberPatternsFetchedEvent(saved);
        } catch (Exception e) {
            log.error("Error occurred during bulk CardNumberPattern creation - Error: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean existByStatus(Long numPatternId, CardNumberPatternStatus cardNumberPatternStatus) {
        if (numPatternId == null || cardNumberPatternStatus == null) {
            log.warn("existByStatus called with invalid parameters. numPatternId={}, status={}",
                    numPatternId, cardNumberPatternStatus);
            return false;
        }
        try {
            return repository.findById(numPatternId)
                    .map(pattern -> cardNumberPatternStatus.equals(pattern.getPatternStatus()))
                    .orElse(false);
        } catch (Exception e) {
            log.error("Failed to check CardNumberPattern status. id={}, status={}, error={}",
                    numPatternId, cardNumberPatternStatus, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public CardNumberPattern save(CardNumberPattern cardNumberPattern) throws CardDomainException {
        if (cardNumberPattern == null) {
            throw logAndBuildException("CardNumberPattern entity is null during save",
                    "CardNumberPattern entity is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        try {
            return repository.save(cardNumberPattern);
        } catch (Exception e) {
            log.error("Failed to save CardNumberPattern - Id: {}", cardNumberPattern.getId(), e);
            throw logAndBuildException("Failed to save CardNumberPattern in repository",
                    "Failed to save CardNumberPattern: " + e.getMessage(),
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<CardNumberPattern> findByCardProfileId(Long cardProfileId) throws CardDomainException {
        if (cardProfileId == null) {
            throw logAndBuildException("cardProfileId is null in findByCardProfileId",
                    "cardProfileId is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }
        try {
            return repository.findByCardProfileId(cardProfileId);
        } catch (Exception e) {
            log.error("Failed to load CardNumberPatterns for cardProfileId: {}", cardProfileId, e);
            throw logAndBuildException("Failed to load CardNumberPatterns for cardProfileId " + cardProfileId,
                    "Failed to load card number patterns for profileId: "
                            + cardProfileId,
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<CardNumberPattern> findAll() throws CardDomainException {
        try {
            return repository.findAll();
        } catch (Exception e) {
            log.error("Failed to load all card number patterns", e);
            throw logAndBuildException("Failed to load all card number patterns",
                    "Failed to load card number patterns",
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<CardNumberPattern> findAll(Map<String, String> searchCriteria) throws CardDomainException {
        try {
            return repository.findAll(searchCriteria);
        } catch (Exception e) {
            log.error("Failed to load card number patterns", e);
            throw logAndBuildException("Failed to load card number patterns",
                    "Failed to load card number patterns",
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CardDomainException logAndBuildException(String logMessage,
                                                     String userMessage,
                                                     int statusCode,
                                                     HttpStatus httpStatus) {
        log.info(logMessage);
        return new CardDomainException(userMessage, statusCode, httpStatus);
    }

    public CardNumberPattern getActivePatternForProfile(CardProfile profile) throws CardDomainException{
        return profile.getCardNumberRangeList().stream()
                .filter(p -> p.getPatternStatus() == CardNumberPatternStatus.ASSIGN)
                .findFirst()
                .orElseThrow(() -> new CardDomainException(
                        "Assign pattern not found",
                        StatusService.ASSIGN_PATTERN_DOES_NOT_EXIST,
                        HttpStatus.BAD_REQUEST));
    }

    public String getNextAndIncrement(CardNumberPattern patt) throws CardDomainException {

        long seq = (patt.getCardNumGenerationMethod() == CardNumGenerationMethod.RANGE_BASED)
                ? nextForRange(patt)
                : nextForIncremental(patt);

        return String.format("%07d", seq);
    }

    private long nextForRange(CardNumberPattern patt) throws CardDomainException {

        long seq;

        if (patt.getFirstCardNumber() == null) {
            // اولین بار → از FROM شروع کن
            seq = Long.parseLong(patt.getCardNumberFrom());
            patt.setFirstCardNumber(String.valueOf(seq));

        } else if (patt.getLastCardNumber() == null) {
            // دومین بار → last = first + 1
            seq = Long.parseLong(patt.getFirstCardNumber()) + 1;
            patt.setLastCardNumber(String.valueOf(seq));

        } else {
            // دفعات بعدی → last + 1
            seq = Long.parseLong(patt.getLastCardNumber()) + 1;
            patt.setLastCardNumber(String.valueOf(seq));
        }

        long to = Long.parseLong(patt.getCardNumberTo());
        if (seq > to) {
            patt.setPatternStatus(CardNumberPatternStatus.EXHAUSTED);
            repository.save(patt);

            throw new CardDomainException(
                    "Pattern range exhausted",
                    StatusService.CARD_NUMBER_PATTERN_EXHAUSTED,
                    HttpStatus.BAD_REQUEST
            );
        }

        return seq;
    }

    private long nextForIncremental(CardNumberPattern patt) throws CardDomainException {

        long seq;

        if (patt.getFirstCardNumber() == null) {
            // شروع از صفر
            seq = 0;
            patt.setFirstCardNumber("0000000");

        } else if (patt.getLastCardNumber() != null) {
            // اگر last داشت، افزایش بده
            seq = Long.parseLong(patt.getLastCardNumber()) + 1;
            patt.setLastCardNumber(String.valueOf(seq));

        } else {
            // اگر فقط first داشت
            seq = Long.parseLong(patt.getFirstCardNumber()) + 1;
            patt.setLastCardNumber(String.valueOf(seq));
        }

        if (seq > 9_999_999L) {
            patt.setPatternStatus(CardNumberPatternStatus.EXHAUSTED);
            repository.save(patt);

            throw new CardDomainException(
                    "Incremental pattern exhausted",
                    StatusService.CARD_NUMBER_PATTERN_EXHAUSTED,
                    HttpStatus.BAD_REQUEST
            );
        }

        return seq;
    }
}
