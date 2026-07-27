package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.card.GetAllCardsQuery;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.event.card.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.*;

import com.sample.system.card.service.domain.ports.output.exporter.CardPrintExporterRepository;
import com.sample.system.card.service.domain.ports.output.externalService.*;
import com.sample.system.card.service.domain.ports.output.repository.CardPanCacheRepository;
import com.sample.system.card.service.domain.ports.output.repository.CardProfileRepository;
import com.sample.system.card.service.domain.ports.output.repository.CardRepository;
import com.sample.system.card.service.domain.ports.output.repository.CustomerRepository;
import com.sample.system.card.service.domain.response.accessfile.AccessFileResponse;
import com.sample.system.card.service.domain.response.accessfile.result.AccessFileResult;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import com.sample.system.card.service.domain.valueObject.CardNumberDto;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sample.system.card.service.domain.utility.date.DateUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class CardServiceImpl implements CardService {

    private final CardRepository repository;
    private final CardNumberGeneratorService cardNumberGeneratorService;
    private final CardProfileRepository cardProfileRepository;
    private final PartyGateway partyService;
    private final CreditService creditService;
    private final SSMService ssmService;
    private final TrackGenerationService trackGenerationService;
    private final ReasonService reasonService;
    private final ProductService productService;
    private final CardPanCacheRepository cardPanCacheRepository;
    private final CustomerRepository customerRepository;
    private final CardPrintExporterRepository cardPrintExporter;
    private final CustomerService customerService;
    private static final String DEFAULT_SERVICE_CODE = "120";

    @Override
    @Transactional
    public CardCreatedEvent createCard(Card card) throws CardDomainException {
        log.info("Starting Card creation service");
        Long profileId = card.getCardProfileId().getValue();
        CardProfile profile = cardProfileRepository.findById(profileId)
                .orElseThrow(() -> {
                    log.error("CardProfile not found: {}", profileId);
                    return new CardDomainException("CardProfile not found: " + profileId,
                            StatusService.CARD_PROFILE_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });

        String pan = cardNumberGeneratorService.process(new CardNumberDto(profile));
        if (pan.length() != 16) {
            log.error("Generated PAN length is invalid: {}", pan);
            throw new CardDomainException("Generated PAN is not 16 digits: " + pan);
        }

        String nationalId = card.getCustomer().getNationalId();
        if (nationalId == null || nationalId.isBlank()) {
            log.error("NationalId is missing for card creation");
            throw new CardDomainException("NationalId is required to create card");
        }

        UUID cardUuid = UUID.randomUUID();
        log.info("Generated card_id (UUID) for new card: {}", cardUuid);

        // Calculate dates
        LocalDateTime now = LocalDateTime.now();
        String expDate = calculateExpirationDate(now, profile.getValidityPeriod() != null ?
                profile.getValidityPeriod() : 36); // Default 36 months - returns Persian date in yyyy-MM-dd format
        String expDateYYMM = expDate.substring(2, 4) + expDate.substring(5, 7); // YYMM format for tracks
        int cvv2Length = resolveCvv2Length(profile);
        String cvv2 = ssmService.getCvv2(cardUuid, pan, cvv2Length,expDateYYMM, DEFAULT_SERVICE_CODE);
        ensureCvv2Length(cvv2, cvv2Length);

        // Get data from Credit module via GRPC
        String macnaCode = creditService.getMacnaCode(nationalId);

        // Get PINs from SSM module
        String pin1 = ssmService.getPin1(cardUuid, nationalId, pan);
//        String pin2 = ssmService.getPin2(cardUuid, nationalId, pan);

        // Generate tracks
        String track1 = trackGenerationService.generateTrack1(pan, expDateYYMM);
        String track2 = trackGenerationService.generateTrack2(cardUuid, pan, expDateYYMM);
        String track3 = trackGenerationService.generateTrack3(pan, null);

        Track track = Track.builder()
                .track1(track1)
                .track2(track2)
                .track3(track3)
                .pin1(pin1)
                .pin2(null)
                .build();

        // Build complete Card with all fields (cardUuid stored as CARD_ID in DB)
        Card newCard = Card.builder()
                .cardUuid(cardUuid)
                .pan(pan)
                .issueDate(now)
                .cardProfileId(card.getCardProfileId())
                .cardStatus(profile.getInitialCardStatus() ? CardStatus.ACTIVE : CardStatus.ISSUED)
                .caseNumber(card.getCaseNumber())
                .cvv2(cvv2)
                .expDate(expDate)
                .firstStatementDate(null)
                .receivedDate(null)
                .sentDate(null)
                .unitCode(card.getUnitCode())
                .unitName(card.getUnitName())
                .issuingBank(profile.getBank().getName())
                .cifNo(nationalId) // CIFNO = nationalNo
                .crn("1") // CRN set to 1
                .crmNo(null) // CRMNO = null
                .accountType(null)
                .cardAccountGroupName(null)
                .creditLimit(profile.getMaximumAllowedAmount())
                .wrongPassRetryCount(profile.getAllowedPinAttempts())
                .isSecondPassHot(null)//ToDo:
                .wrongSecondPasswordRetryCount(null)//ToDO:
                .isVirtualCard(CardIssueMethod.convertToBoolean(profile.getCardIssueMethod()))
                .macnaCode(macnaCode)//ToDo:
                .bonCardText(null) // BonCardText = null
                .deliveryPersonId(null)
                .issuerPersonId(card.getIssuerPersonId())
                .changeStateDescription(null)
                .maskedPan(PanMaskingUtil.maskPan(pan))
                .build();
        newCard.setCustomer(card.getCustomer());
        newCard.setTrack(track);
        customerService.checkOrSave(card.getCustomer());
        Card savedCard = repository.save(newCard);
        if (savedCard == null) {
            log.error("Could not save Card");
            throw new CardDomainException("Could not save Card");
        }

        // Add PAN to Redis cache after successful save
        if (savedCard.getPan() != null && !savedCard.getPan().isBlank()) {
            try {
                cardPanCacheRepository.addPan(savedCard.getPan());
                log.debug("PAN added to Redis cache after card creation: {}", savedCard.getMaskedPan());
            } catch (Exception e) {
                log.error("Error adding PAN to Redis cache after card creation: {}", savedCard.getMaskedPan(), e);
                // Don't throw exception - cache is for optimization, database is source of truth
            }
        }

        CardCreatedEvent event = new CardCreatedEvent(savedCard, null);
        log.info("Card creation process completed successfully - CardId: {}",
                savedCard.getId() != null ? savedCard.getId().getValue() : null);
        return event;
    }

    private String calculateExpirationDate(LocalDateTime issueDate, int validityPeriodInMonths) {
        LocalDateTime expDate = issueDate.plusMonths(validityPeriodInMonths);
        Date expirationDate = Date.from(expDate.atZone(ZoneId.systemDefault()).toInstant());
        // Return expiration date in Jalali (Persian) calendar
        return DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, expirationDate, "yyyy-MM-dd", false);
    }

    @Override
    @Transactional(readOnly = true)
    public Card getCard(Long id) throws CardDomainException {
        log.info("Starting Card retrieval service - CardId: {}", id);
        try {
            Card card = repository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Card not found with id: {}", id);
                        return new CardDomainException("Card not found with id: " + id
                        ,StatusService.CARD_NOT_FOUND,HttpStatus.BAD_REQUEST);
                    });
            log.info("Card retrieval completed successfully - CardId: {}", id);
            return card;
        } catch (Exception e) {
            log.error("Error occurred during Card retrieval - CardId: {}, Error: {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getAllCards(GetAllCardsQuery query) throws CardDomainException {
        log.info("Starting Cards retrieval service - ActiveOnly: {}, CustomerNumber: {}",
                query.getActiveOnly(), query.getCustomerNumber());
        try {
            List<Card> allCards = repository.findAll();

            // Apply filters if provided
            List<Card> filteredCards = allCards.stream()
                    .filter(card -> {
                        // Filter by activeOnly if provided
                        if (query.getActiveOnly() != null && query.getActiveOnly()) {
                            // Assuming cards have an active status - adjust based on your domain model
                            // For now, we'll return all if no active field is available
                        }
                        return true;
                    })
                    .toList();

            log.info("Cards retrieval completed successfully - Count: {}", filteredCards.size());
            return filteredCards;
        } catch (Exception e) {
            log.error("Error occurred during Cards retrieval - ActiveOnly: {}, CustomerNumber: {}, Error: {}",
                    query.getActiveOnly(), query.getCustomerNumber(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Card> listCards(Map<String, String> map, String caller, String ip) throws CardDomainException {
        log.info("Starting Cards listing service with pagination - Caller: {}, IP: {}", caller, ip);
        try {
            Page<Card> cardsPage = repository.findAllCards(map);
            log.info("Cards listing completed successfully - Count: {}",
                    cardsPage != null ? cardsPage.getContent().size() : 0);
            return cardsPage;
        } catch (Exception e) {
            log.error("Error occurred during Cards listing - Caller: {}, IP: {}, Error: {}",
                    caller, ip, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CardBlockedEvent blockCard(Card card, Long reasonId, String blockedBy) throws CardDomainException {
        Long cardId = requireCardId(card, "block");
        log.info("Starting Card blocking service - CardId: {}, ReasonId: {}",
                cardId, reasonId);
            Card existing = getCard(cardId);
            Reason reason = reasonService.findById(reasonId);
            log.info("card current status is:{} for blocking",existing.getCardStatus());
            // Validate card status - must be ACTIVE to block
            if (existing.getCardStatus() == null || !existing.getCardStatus().canBeBlocked()) {
                if (existing.getCardStatus() == CardStatus.HOT) {
                    log.error("Cannot block card {} because status is HOT", cardId);
                    throw new CardDomainException("last status is hot", StatusService.CARD_STATUS_IS_HOT, HttpStatus.BAD_REQUEST);
                }
                log.error("Cannot block card {} because last status is not acceptable: {}", cardId, existing.getCardStatus());
                throw new CardDomainException("last status should be active",
                        StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST);
            }

            existing.setCardStatus(CardStatus.HOT);
            existing.setChangeStateDescription("دلیل مسدودسازی: "
                    + reason.getReason() +
                            " | مسدود شده توسط: " + blockedBy +
                            " | تاریخ: " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Card savedCard = repository.save(existing);
            if (savedCard == null) {
                log.error("Could not save blocked Card");
                throw new CardDomainException("Could not save blocked Card");
            }
            CardBlockedEvent event = new CardBlockedEvent(
                    savedCard,
                    java.time.ZonedDateTime.now()
            );

            log.info("Card blocking completed successfully - CardId: {}, Status: {}, Reason: {}",
                    savedCard.getId() != null ? savedCard.getId().getValue() : null,
                    savedCard.getCardStatus(),
                    reason.getReason());

            // TODO: Send notification to customer (SMS/Email)
            log.info("Notification should be sent to customer for card blocking");
            // Action history persistence should be handled in infrastructure via event listener

            return event;
    }

    @Override
    public CardUnblockedEvent unblockCard(Card card, Long reasonId, String unblockedBy) throws CardDomainException {
        Long cardId = requireCardId(card, "unblock");
        log.info("Starting Card unblocking service - CardId: {}, Reason: {}, UnblockedBy: {}",
                cardId, reasonId, unblockedBy);

            // Validate required fields
            Reason reason = reasonService.findById(reasonId);

            // Fetch card from repository
            Card existing = repository.findById(cardId)
                    .orElseThrow(() -> {
                        log.error("Card not found with id: {}", cardId);
                        return new CardDomainException("Card not found with id: " + cardId);
                    });

            // Validate card status - must be HOT to unblock
            if (existing.getCardStatus() != CardStatus.HOT) {
                log.error("Unblock rejected; status {} cannot be converted to active for card {}",
                        existing.getCardStatus(), cardId);
                throw new CardDomainException(String.format("card status: (%s) cannot be converted to active",
                        existing.getCardStatus()),
                        StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST);
            }

            // Build updated card with ACTIVE status
            existing.setCardStatus(CardStatus.ACTIVE);
            existing.setChangeStateDescription("دلیل رفع مسدودی: " + reason.getReason() +
                            " | رفع مسدودی شده توسط: " + unblockedBy +
                            " | تاریخ: "
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Save unblocked card
            Card savedCard = repository.save(existing);
            if (savedCard == null) {
                log.error("Could not save unblocked Card");
                throw new CardDomainException("Could not save unblocked Card");
            }

            // Create event
            CardUnblockedEvent event = new CardUnblockedEvent(
                    savedCard
            );

            log.info("Card unblocking completed successfully - CardId: {}, Status: {}, Reason: {}",
                    savedCard.getId() != null ? savedCard.getId().getValue() : null,
                    savedCard.getCardStatus(),
                    reason);

            // TODO: Send notification to customer (SMS/Email)
            log.info("Notification should be sent to customer for card unblocking");
            // Action history persistence should be handled in infrastructure via event listener

            return event;
    }

    @Override
    @Transactional
    public CardActivatedEvent activateCard(Long cardId, Long reasonId) throws CardDomainException {
        log.info("Starting Card activation service - CardId: {}", cardId);
        try {
            Card existing = getCard(cardId);
            if (CardStatus.ACTIVE == existing.getCardStatus()) {
                log.error("Activation rejected; card {} is already ACTIVE", cardId);
                throw new CardDomainException("card is active",StatusService.CARD_STATUS_IS_ACTIVE,
                        HttpStatus.BAD_REQUEST);
            }

            if (existing.getCardStatus() != null && existing.getCardStatus().canNotBeActivated()) {
                log.error("Activation rejected; status {} cannot be converted to active for card {}",
                        existing.getCardStatus(), cardId);
                throw new CardDomainException(String.format("card status: (%s) cannot be converted to active",
                        existing.getCardStatus()),
                        StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST);
            }

            if(existing.isVirtual() ||
                    (!existing.isVirtual() && CardPhysicalStatus.DELIVERED.equals(existing.getCardPhysicalStatus()))){
                existing.setCardStatus(CardStatus.ACTIVE);
            }
            // Save activated card
            Card savedCard = repository.save(existing);
            if (savedCard == null) {
                log.error("Could not save activated Card");
                throw new CardDomainException("Could not save activated Card");
            }

            // Create event
            CardActivatedEvent event = new CardActivatedEvent(
                    savedCard,
                    java.time.ZonedDateTime.now(),
                    null
            );
            log.info("Card activation completed successfully - CardId: {}, Status: {}",
                    savedCard.getId() != null ? savedCard.getId().getValue() : null,
                    savedCard.getCardStatus());
            // TODO: Send notification to customer (SMS/Email)
            log.info("Notification should be sent to customer for card activation");
            return event;
        } catch (CardDomainException e) {
            log.error("Error occurred during Card activation - CardId: {}, Error: {}",
                    cardId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred during Card activation - CardId: {}, Error: {}",
                    cardId, e.getMessage(), e);
            throw new CardDomainException("Failed to activate card: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CardDeactivatedEvent deactivateCard(Long cardId, Long reasonId, String deactivatedBy) throws CardDomainException {
        log.info("Starting Card deactivation service - CardId: {}, Reason: {}, DeactivatedBy: {}",
                cardId, reasonId, deactivatedBy);
        if (reasonId == null) {
            log.error("Deactivation rejected; reasonId is null for card {}", cardId);
            throw new CardDomainException("Reason is needed");
        }
        Card existing = getCard(cardId);
        if (existing.getCardStatus() == CardStatus.INACTIVE) {
            log.error("Deactivation rejected; card {} already INACTIVE", cardId);
            throw new CardDomainException("last status is INACTIVE", StatusService.LAST_STATUS_NOT_ACCEPTABLE,
                    HttpStatus.BAD_REQUEST);
        }
        if (existing.getCardStatus() != null && existing.getCardStatus().isTerminalState()) {
            log.error("Deactivation rejected; card {} in terminal state {}", cardId, existing.getCardStatus());
            throw new CardDomainException(String.format("Card cannot be deactivated in current state. Current status: " +
                    existing.getCardStatus()),
                    StatusService.LAST_STATUS_NOT_ACCEPTABLE, HttpStatus.BAD_REQUEST);
        }
        if (productService.isInactivableCard(cardId)) {
            log.debug("Card {} marked as inactivable by product service", cardId);
        } else {
            log.warn("Card {} is not inactivable according to product service", cardId);
            throw new CardDomainException(String.format("card is not inactivable. CardId: %s", cardId),
                    StatusService.CARD_INQUIRY_ERROR, HttpStatus.BAD_REQUEST);
        }
        Reason reason = reasonService.findById(reasonId);

        // Update existing card in-place with INACTIVE status (avoid creating a new card row)
        existing.setCardStatus(CardStatus.INACTIVE);
        existing.setChangeStateDescription("دلیل غیرفعال‌سازی: " + reason.getReason() +
                " | غیرفعال شده توسط: " + deactivatedBy +
                " | تاریخ: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Save deactivated card (update existing row)
        Card savedCard = repository.save(existing);
        if (savedCard == null) {
            log.error("Could not save deactivated Card");
            throw new CardDomainException("Could not save deactivated Card");
        }

        // Create event
        CardDeactivatedEvent event = new CardDeactivatedEvent(
                savedCard,
                java.time.ZonedDateTime.now(),
                deactivatedBy,
                reason.getReason()
        );

        log.info("Card deactivation completed successfully - CardId: {}, Status: {}, Reason: {}",
                savedCard.getId() != null ? savedCard.getId().getValue() : null,
                savedCard.getCardStatus(),
                reason);

        // TODO: Send notification to customer (SMS/Email)
        log.info("Notification should be sent to customer for card deactivation");
        return event;
    }

    @Override
    public void validateCardStatusForReplacement(Card card) throws CardDomainException {
        if (card == null || card.getCardStatus() == null) {
            log.info("validateCardStatusForReplacement failed: card or status is null");
            throw new CardDomainException(
                    "Card status is required to create replacement request",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST);
        }

        if (!(CardStatus.HOT.equals(card.getCardStatus()) || CardStatus.INACTIVE.equals(card.getCardStatus()))) {
            log.info("validateCardStatusForReplacement failed: status {} not allowed for cardId {}",
                    card.getCardStatus(), card.getId() != null ? card.getId().getValue() : null);
            throw new CardDomainException(
                    String.format("Replacement not allowed for cardId %s with status %s",
                            card.getId() != null ? card.getId().getValue() : null,
                            card.getCardStatus()),
                    StatusService.LAST_STATUS_NOT_ACCEPTABLE,
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public CardCreatedEvent replaceCard(Long cardId, String userId) throws CardDomainException {
        log.info("Starting card replacement - CardId: {}", cardId);
        Card existing = getCard(cardId);
        validateCardStatusForReplacement(existing);
        if (existing.getCardProfileId() == null) {
            log.info("replaceCard failed: profileId is null for cardId {}", cardId);
            throw new CardDomainException("Card profile is required to replace card id: " + cardId,
                    StatusService.CARD_PROFILE_NOT_FOUND,HttpStatus.OK);
        }

        // Get or create customer for replacement
        Customer customer = getOrCreateCustomer(existing.getCustomer().getNationalId());

        // Deactivate old card before issuing replacement
        LocalDateTime now = LocalDateTime.now();
        existing.setCardStatus(CardStatus.INACTIVE);
        existing.setChangeStateDescription("Deactivated for replacement at " +
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Card savedOldCard = repository.save(existing);
        CardProfile profile = loadCardProfile(existing.getCardProfileId());
        Integer validityPeriod = profile != null ? profile.getValidityPeriod() : null;
        String newExpDate = calculateExpirationDate(now, validityPeriod != null ? validityPeriod : 36);
        String expDateYyMm = resolveExpDateYyMm(newExpDate);

        UUID replacementCardUuid = UUID.randomUUID();
        log.info("Replacement card_id (UUID) generated: {}", replacementCardUuid);
        int cvv2Length = resolveCvv2Length(profile);
        String newCvv2 = ssmService.getCvv2(replacementCardUuid, existing.getPan(), cvv2Length, expDateYyMm, DEFAULT_SERVICE_CODE);
        ensureCvv2Length(newCvv2, cvv2Length);

        String track1 = trackGenerationService.generateTrack1(existing.getPan(), expDateYyMm);
        String track2 = trackGenerationService.generateTrack2(replacementCardUuid, existing.getPan(), expDateYyMm);
        String track3 = trackGenerationService.generateTrack3(existing.getPan(), null);

        Card replacement = Card.builder()
                .cardUuid(replacementCardUuid)
                .pan(existing.getPan())
                .maskedPan(existing.getMaskedPan() != null ? existing.getMaskedPan() : PanMaskingUtil.maskPan(existing.getPan()))
                .cvv2(newCvv2)
                .issueDate(now)
                .expDate(newExpDate)
                .cardProfileId(existing.getCardProfileId())
                .cardStatus(CardStatus.ACTIVE)
                .caseNumber(existing.getCaseNumber())
                .unitCode(existing.getUnitCode())
                .unitName(existing.getUnitName())
                .wrongPassRetryCount(profile != null ? profile.getAllowedPinAttempts() : null)
                .customer(customer)
                .oldCardId(savedOldCard.getId() != null ? savedOldCard.getId().getValue() : null)
                .issuerPersonId(userId)
                .macnaCode(savedOldCard.getMacnaCode())
                .crn(savedOldCard.getCrn())
                .issuingBank(savedOldCard.getIssuingBank())
                .isVirtualCard(savedOldCard.getIsVirtualCard())
                .creditLimit(savedOldCard.getCreditLimit())
                .build();

        CardCreatedEvent newCardEvent = new CardCreatedEvent(repository.save(replacement), ZonedDateTime.now());
        log.info("Card replacement completed - OriginalCardId: {}, NewCardId: {}",
                cardId,
                newCardEvent.getCard() != null && newCardEvent.getCard().getId() != null
                        ? newCardEvent.getCard().getId().getValue() : null);
        return newCardEvent;
    }

    @Override
    @Transactional
    public CardCreatedEvent reissueCard(Card card) throws CardDomainException {
        Long cardId = requireCardId(card, "reissue");
        log.info("Starting card reissue - CardId: {}", cardId);
        Card existing = getCard(cardId);
        if (existing.getPan() == null || existing.getExpDate() == null) {
            log.info("reissueCard failed: PAN or expDate is null for cardId {}", cardId);
            throw new CardDomainException("Card PAN and expiration date are required for reissue - CardId: " + cardId);
        }

        CardProfile profile = loadCardProfile(existing.getCardProfileId());
        UUID reissueCardUuid = UUID.randomUUID();
        log.info("Reissue card_id (UUID) generated: {}", reissueCardUuid);

        String expDateYyMm = resolveExpDateYyMm(existing.getExpDate());
        LocalDateTime now = LocalDateTime.now();

        int cvv2Length = resolveCvv2Length(profile);
        String newCvv2 = ssmService.getCvv2(reissueCardUuid, existing.getPan(), cvv2Length,expDateYyMm, DEFAULT_SERVICE_CODE);
        ensureCvv2Length(newCvv2, cvv2Length);

        String nameEn = existing.getCustomer().getFirstName() != null ? existing.getCustomer().getFirstName() : "";
        String track1 = trackGenerationService.generateTrack1(existing.getPan(), expDateYyMm);
        String track2 = trackGenerationService.generateTrack2(reissueCardUuid, existing.getPan(), expDateYyMm);
        String track3 = trackGenerationService.generateTrack3(existing.getPan(), null);

        // Get or create customer for reissue
//        Customer customer = getOrCreateCustomer(existing.getNationalId());

        Card newCard = Card.builder()
                .cardUuid(reissueCardUuid)
                .issueDate(now)
                .cardStatus(CardStatus.ACTIVE)
                .cvv2(newCvv2)
                .maskedPan(existing.getMaskedPan() != null ? existing.getMaskedPan() : PanMaskingUtil.maskPan(existing.getPan()))
                .wrongPassRetryCount(profile != null ? profile.getAllowedPinAttempts() : null)
                .createdDate(null)
                .lastModifiedDate(null)
                .build();
        newCard.setId(null);

        Card savedCard = repository.save(newCard);
        log.info("Card reissue completed - OriginalCardId: {}, NewCardId: {}",
                cardId,
                savedCard.getId() != null ? savedCard.getId().getValue() : null);
        return new CardCreatedEvent(savedCard, ZonedDateTime.now());
    }

    @Override
    @Transactional
    public CardCreatedEvent renewCard(Card card) throws CardDomainException {
        Long cardId = requireCardId(card, "renew");
        log.info("Starting card renewal - CardId: {}", cardId);
        Card existing = getCard(cardId);

        if (existing.getCardStatus() == CardStatus.HOT) {
            log.error("Renewal rejected; card {} is HOT", cardId);
            throw new CardDomainException("Hot cards cannot be renewed - CardId: " + cardId);
        }

        CardProfile profile = loadCardProfile(existing.getCardProfileId());

        LocalDate currentDate = LocalDate.now();
        LocalDate existingExpiry = parseExpiration(existing.getExpDate());
        if (existingExpiry == null) {
            log.error("Renewal rejected; invalid expiration date for card {}", cardId);
            throw new CardDomainException("Card expiration date is invalid for renewal - CardId: " + cardId);
        }

        if (existingExpiry.isAfter(currentDate.plusMonths(3))) {
            log.warn("Renewal rejected; card {} has more than 3 months to expire ({}).", cardId, existingExpiry);
            throw new CardDomainException("Card has more than 3 months before expiration - CardId: " + cardId);
        }

        int validityMonths = resolveValidityMonths(profile);
        LocalDate baseDate = existingExpiry.isAfter(currentDate) ? existingExpiry : currentDate;
        LocalDate newExpiry = baseDate.plusMonths(validityMonths);
        String newExpiryIso = newExpiry.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String newExpiryYyMm = newExpiry.format(DateTimeFormatter.ofPattern("yyMM"));

        UUID renewalCardUuid = UUID.randomUUID();
        log.info("Renewal card_id (UUID) generated: {}", renewalCardUuid);
        int cvv2Length = resolveCvv2Length(profile);
        String newCvv2 = ssmService.getCvv2(renewalCardUuid, existing.getPan(), cvv2Length, newExpiryYyMm, DEFAULT_SERVICE_CODE);
        ensureCvv2Length(newCvv2, cvv2Length);

        String pin1 = ssmService.getPin1(renewalCardUuid, existing.getCustomer().getNationalId(), existing.getPan());
        String pin2 = ssmService.getPin2(renewalCardUuid, existing.getCustomer().getNationalId(), existing.getPan());

        String nameEn = existing.getCustomer().getFirstName()!= null ? existing.getCustomer().getFirstName() : "";
        String track1 = trackGenerationService.generateTrack1(existing.getPan(), newExpiryYyMm);
        String track2 = trackGenerationService.generateTrack2(renewalCardUuid, existing.getPan(), newExpiryYyMm);
        String track3 = trackGenerationService.generateTrack3(existing.getPan(), null);

        // Get or create customer for renewal
        Customer customer = getOrCreateCustomer(existing.getCustomer().getNationalId());

        LocalDateTime now = LocalDateTime.now();

        Card newCard = Card.builder()
                .cardUuid(renewalCardUuid)
                .issueDate(now)
                .expDate(newExpiryIso)
                .cardStatus(CardStatus.ACTIVE)
                .cvv2(newCvv2)
                .maskedPan(existing.getMaskedPan() != null ? existing.getMaskedPan() : PanMaskingUtil.maskPan(existing.getPan()))
                .wrongPassRetryCount(profile != null ? profile.getAllowedPinAttempts() : null)
                .customer(customer)
                .createdDate(null)
                .lastModifiedDate(null)
                .build();
        newCard.setId(null);

        Card savedCard = repository.save(newCard);
        log.info("Card renewal completed - OriginalCardId: {}, NewCardId: {}",
                cardId,
                savedCard.getId() != null ? savedCard.getId().getValue() : null);
        return new CardCreatedEvent(savedCard, ZonedDateTime.now());
    }

    @Override
    public AccessFileResponse generateAccessFile() throws CardDomainException {
        log.info("Starting Access file generation for cards, tracks, and customers");
        List<Card> cards = repository.findPrintableCards();
        if (cards == null || cards.isEmpty()) {
            log.warn("No card data available for Access export");
            throw new CardDomainException("No card data available to export",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        // Delegate to infrastructure adapter via port (Hexagonal Architecture)
        AccessFileResult result = cardPrintExporter.export(cards);

        log.info("Access file generation completed successfully. File: {}, Records: {}",
                result.getFileName(), result.getRecordCount());

        // Convert result to response (Domain to Application layer)
        return AccessFileResponse.from(result);
    }

    private CardProfile loadCardProfile(CardProfileId cardProfileId) throws CardDomainException {
        if (cardProfileId == null) {
            log.error("Card profile id is null for profile load");
            throw new CardDomainException("Card profile is required for this operation");
        }
        return cardProfileRepository.findById(cardProfileId.getValue())
                .orElseThrow(() -> {
                    log.error("CardProfile not found: {}", cardProfileId.getValue());
                    return new CardDomainException("CardProfile not found: " + cardProfileId.getValue());
                });
    }

    private int resolveCvv2Length(CardProfile profile) {
        if (profile != null && profile.getCvv2Length() != null && profile.getCvv2Length() > 0) {
            return profile.getCvv2Length();
        }
        return 4;
    }

    private int resolveValidityMonths(CardProfile profile) {
        if (profile != null && profile.getValidityPeriod() != null && profile.getValidityPeriod() > 0) {
            return profile.getValidityPeriod();
        }
        return 36;
    }

    private void ensureCvv2Length(String cvv2, int expectedLength) throws CardDomainException {
        if (cvv2 == null) {
            log.error("PAN part5 (CVV2) generation returned null value");
            throw new CardDomainException("PAN part5 (CVV2) generation returned null value");
        }
        if (expectedLength <= 0) {
            return;
        }
        if (cvv2.length() != expectedLength) {
            log.error("PAN part5 length mismatch. Expected: {} but received length: {}", expectedLength, cvv2.length());
            throw new CardDomainException(String.format("PAN part5 length mismatch. Expected: "
                    + expectedLength + " but received: " + cvv2.length()),
                    StatusService.INVALID_CVV2_LENGTH,
                    HttpStatus.OK);
        }
    }

    private LocalDate parseExpiration(String expDate) {
        if (expDate == null || expDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(expDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            log.warn("Could not parse expiration date: {}", expDate, e);
            return null;
        }
    }

    private String resolveExpDateYyMm(String expDate) throws CardDomainException {
        LocalDate parsed = parseExpiration(expDate);
        if (parsed == null) {
            log.error("Invalid expiration date format: {}", expDate);
            throw new CardDomainException("Invalid expiration date format: " + expDate);
        }
        return parsed.format(DateTimeFormatter.ofPattern("yyMM"));
    }

    private Long requireCardId(Card card, String operation) throws CardDomainException {
        if (card == null || card.getId() == null) {
            log.error("Card id is required to {} card", operation);
            throw new CardDomainException("Card id is required to " + operation + " card");
        }
        return card.getId().getValue();
    }

    private Customer getOrCreateCustomer(String nationalId) throws CardDomainException {
        log.debug("Getting or creating customer for nationalId: {}", nationalId);

        // Check if customer already exists
        Customer existingCustomer = customerRepository.findByNationalId(nationalId).orElse(null);
        if (existingCustomer != null && existingCustomer.getId() != null) {
            log.debug("Customer already exists - CustomerId: {}, NationalId: {}",
                    existingCustomer.getId(), nationalId);
            return existingCustomer;
        }

        Customer customer = partyService.loadCustomerByNationalId(nationalId);
        Customer savedCustomer = customerRepository.save(customer);
        if (savedCustomer == null || savedCustomer.getId() == null) {
            log.error("Failed to save customer for nationalId: {}", nationalId);
            throw new CardDomainException(
                    "Failed to save customer for nationalId: " + nationalId,
                    StatusService.PARTY_SERVICE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Customer created successfully - CustomerId: {}, NationalId: {}",
                savedCustomer.getId(), nationalId);
        return savedCustomer;
    }

    @Override
    public Boolean checkExistCardByCardNumber(String cardNumber) throws CardDomainException {
        log.debug("Checking if card exists by card number: {}", PanMaskingUtil.maskPan(cardNumber));
        if(repository.existsByCardNumber(cardNumber))
            return true;
        log.warn("Card does not exist with pan {}", PanMaskingUtil.maskPan(cardNumber));
        throw new CardDomainException(String.format("card with pan %s does not exist.", cardNumber),
                StatusService.CARD_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST);
    }

    @Override
    public Boolean existsByProfileId(Long cardProfileId) throws CardDomainException {
        log.debug("Checking if any card exists for profile ID: {}", cardProfileId);
        if(repository.existsByProfileId(cardProfileId))
            return true;
        log.warn("Card does not exist with profile Id {}", cardProfileId);
        throw new CardDomainException(String.format("card with profile Id  %s does not exist.", cardProfileId),
                StatusService.CARD_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST);
    }

}
