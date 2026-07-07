package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.command.cardProfile.GetAllCardProfilesQuery;
import com.sample.system.card.service.domain.command.cardProfile.GetCardProfileQuery;
import com.sample.system.card.service.domain.command.cardCategory.GetCardCategoryQuery;
import com.sample.system.card.service.domain.command.cardType.GetCardTypeQuery;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.CardIssueMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.enums.Reloadable;
import com.sample.system.card.service.domain.enums.SalesMethod;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileActivatedEvent;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileCreatedEvent;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileDeactivatedEvent;
import com.sample.system.card.service.domain.event.cardProfile.CardProfileUpdatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.*;
import com.sample.system.card.service.domain.ports.output.repository.CardProfileRepository;
import com.sample.system.card.service.domain.ports.output.repository.CardRepository;
import com.sample.system.card.service.domain.ports.output.repository.CardTypeCategoryRepository;
import com.sample.system.card.service.domain.ports.output.repository.FeeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class CardProfileServiceImpl implements CardProfileService {

    private final CardTypeCategoryRepository cardTypeCategoryRepository;
    private final CardProfileRepository repository;
    private final CardRepository cardRepository;
    private final CardCategoryService cardCategoryService;
    private final CardTypeService cardTypeService;
    private final BankService bankService;
    private final FeeProfileRepository feeProfileRepository;
    private final CardNumberPatternService patternService;

    @Override
    public CardProfileCreatedEvent createCardProfile(CardProfile cardProfile) throws CardDomainException {
        log.info("Starting CardProfile creation service - ProfileName: {}, BankId: {}",
                cardProfile.getProfileName(),
                cardProfile.getIssuingBankId()
        );
        if (repository.existsByProfileName(cardProfile.getProfileName())) {
            log.info("Duplicate profile name found for profile with name:{}", cardProfile.getProfileName());
            throw new CardDomainException("Duplicate profile name found",
                    StatusService.DUPLICATE_PROFILE_NAME_FOUND, HttpStatus.BAD_REQUEST);
        }

        log.debug("Validating business rules for CardProfile creation");
        CardProfile.validateCardProfileCreation(
                cardProfile.getCardCategoryId(),
                cardProfile.getCardTypeId()
        );
        log.debug("Business rules validation completed successfully");

        // Validate bank exists using Service
        if (cardProfile.getIssuingBankId() != null) {
            log.debug("Validating Bank existence - BankId: {}", cardProfile.getIssuingBankId());
            Bank bank = bankService.findById(cardProfile.getIssuingBankId());
            log.debug("Bank validation completed successfully- Bank Name:{}", bank.getName());
            cardProfile.setBank(bank);
        }
        // Validate CardCategory exists using Service
        if (cardProfile.getCardCategoryId() != null) {
            log.debug("Validating CardCategory existence - CardCategoryId: {}", cardProfile.getCardCategoryId());
            cardCategoryService.validateCardCategoryExists(cardProfile.getCardCategoryId());
            log.debug("CardCategory validation completed successfully");
        }

        // Validate CardType exists using Service
        if (cardProfile.getCardTypeId() != null) {
            log.debug("Validating CardType existence - CardTypeId: {}", cardProfile.getCardTypeId());
            cardTypeService.validateCardTypeExists(cardProfile.getCardTypeId());
            log.debug("CardType validation completed successfully");
        }

        // Validate CardTypeCategory exists
        CardTypeCategory cardTypeCategory = null;

        // Create or update CardTypeCategory if both cardCategoryId and cardTypeId are provided
        if (cardProfile.getCardCategoryId() != null && cardProfile.getCardTypeId() != null) {
            log.info("CardTypeCategory relationship will saved for CardCategoryId: {}, CardTypeId: {}",
                    cardProfile.getCardCategoryId(), cardProfile.getCardTypeId());
            cardTypeCategory = cardTypeCategoryRepository.
                    findByCardTypeAndCardCategory(cardProfile.getCardTypeId(),
                            cardProfile.getCardCategoryId()).orElseThrow(() ->
                            new CardDomainException("CardTypeCategory not found",
                                    StatusService.CARDTYPECATEGORY_NOT_FOUND, HttpStatus.BAD_REQUEST));
            log.debug("CardTypeCategory validation completed successfully. Id:{}", cardTypeCategory.getCardCategoryId());
            cardProfile.setCardTypeCategory(cardTypeCategory);
        }

        // Validate FeeProfile exists (optional)
        if (cardProfile.getFeeProfileId() != null) {
            log.debug("Validating FeeProfile existence - FeeProfileId: {}", cardProfile.getFeeProfileId());
            var feeProfile = feeProfileRepository.findById(cardProfile.getFeeProfileIdLong());
            if (feeProfile.isEmpty()) {
                log.info("FeeProfile not found - FeeProfileId: {}", cardProfile.getFeeProfileId());
                throw new CardDomainException(
                        "FeeProfile with ID " + cardProfile.getFeeProfileId() + " does not exist",
                        StatusService.FEE_PROFILE_NOT_FOUND,HttpStatus.BAD_REQUEST);
            }
            cardProfile.setFeeProfile(feeProfile.get());
            log.debug("FeeProfile validation completed successfully");
        }
        //check issue method and reloadable with category :
        assert cardTypeCategory != null;
        if("CREDIT".equalsIgnoreCase(cardTypeCategory.getCardCategory().getName()) ||
                "DEBIT".equalsIgnoreCase(cardTypeCategory.getCardCategory().getName())){
            if(!SalesMethod.DEFAULT.equals(cardProfile.getSalesMethod())){
                throw new CardDomainException("sales method should be null or default when category is credit, debit",
                        StatusService.INVALID_SALES_METHOD, HttpStatus.BAD_REQUEST);
            }
            if(!Reloadable.DEFAULT.equals(cardProfile.getReloadable())){
                throw new CardDomainException("invalid reloadable method should be null or default when category is credit, debit",
                        StatusService.INVALID_RELOADABLE_METHOD, HttpStatus.BAD_REQUEST);
            }
        }
        if("PREPAID".equalsIgnoreCase(cardTypeCategory.getCardCategory().getName())){
            if(SalesMethod.DEFAULT.equals(cardProfile.getSalesMethod())){
                throw new CardDomainException("invalid sales method when category is prepaid",
                        StatusService.INVALID_SALES_METHOD, HttpStatus.BAD_REQUEST);
            }
            if(Reloadable.DEFAULT.equals(cardProfile.getReloadable())){
                throw new CardDomainException("invalid reloadable method when category is prepaid",
                        StatusService.INVALID_RELOADABLE_METHOD, HttpStatus.BAD_REQUEST);
            }
        }
        if(CardIssueMethod.VIRTUAL.equals(cardProfile.getCardIssueMethod())){
            if (!"NONE".equalsIgnoreCase(cardTypeCategory.getCardType().getName())) {
                throw new CardDomainException("invalid relation between issue method and card type",
                        StatusService.INVALID_RELATION_BETWEEN_ISSUE_METHOD_AND_CARD_TYPE,
                        HttpStatus.BAD_REQUEST);
            }
        }
        if("PREPAID".equalsIgnoreCase(cardTypeCategory.getCardCategory().getName())){
            if (cardProfile.getMaximumAllowedAmount() == null)
                throw new CardDomainException("maximum allowed amount should be filled if cardCategory is prepaid",
                        StatusService.MAXIMUM_ALLOWED_AMOUNT_SHOULD_NOT_BE_NULL, HttpStatus.BAD_REQUEST);
        }
        // Validate pattern exists and is active (but don't attach it yet to avoid detached entity issue)
        Long patternId = null;
        if (cardProfile.getNumPatternId() != null) {
            log.debug("Validating pattern existence and active status - PatternId: {}", cardProfile.getNumPatternId());
            CardNumberPattern pattern = patternService.getCardNumberPattern(
                    GetCardNumberPatternQuery.builder().patternId(cardProfile.getNumPatternId()).build());
            if (pattern.getId() == null || !CardNumberPatternStatus.ACTIVE.equals(pattern.getPatternStatus())) {
                log.info("Pattern with Id: {} does not exist with Active status", cardProfile.getNumPatternId());
                throw new CardDomainException(String.format("Pattern with Id:%s does not exist with Active status", cardProfile.getNumPatternId()),
                        StatusService.ACTIVE_PATTERN_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST);
            }
            patternId = pattern.getId().getValue();
        }

        log.debug("Saving CardProfile to repository");
        CardProfile savedProfile = repository.save(cardProfile);
        if (savedProfile == null) {
            log.info("Failed to save CardProfile - ProfileName: {}", cardProfile.getProfileName());
            throw new CardDomainException("Could not save Card with id " + cardProfile.getProfileName(),
                    StatusService.CARD_PROFILE_NOT_FOUND,HttpStatus.OK);
        }
        log.info("CardProfile saved successfully - ProfileId: {}, ProfileName: {}",
                savedProfile.getId().getValue(), savedProfile.getProfileName());

        if (patternId != null) {
            log.debug("Updating pattern status to ASSIGN and attaching to profile - PatternId: {}, ProfileId: {}",
                    patternId, savedProfile.getId().getValue());
            CardNumberPattern pattern = patternService.getCardNumberPattern(
                    GetCardNumberPatternQuery.builder().patternId(patternId).build());
            pattern.setPatternStatus(CardNumberPatternStatus.ASSIGN);
            pattern.setCardProfile(savedProfile);
            pattern.setCardProfileId(savedProfile.getId());
            patternService.save(pattern);
            log.info("Pattern assigned to profile successfully - PatternId: {}, ProfileId: {}",
                    patternId, savedProfile.getId().getValue());
            savedProfile = repository.findById(savedProfile.getId().getValue())
                    .orElse(savedProfile);
        }
        log.debug("Creating CardProfileCreatedEvent");
        CardProfileCreatedEvent event = new CardProfileCreatedEvent(savedProfile);
        log.info("CardProfile creation process completed successfully - ProfileId: {}, ProfileName: {}",
                savedProfile.getId().getValue(), savedProfile.getProfileName());
        return event;
    }

    // ---------------------- UPDATE ----------------------
    @Override
    public CardProfileUpdatedEvent updateCardProfile(CardProfile cardProfile) throws CardDomainException {
        Long profileId = cardProfile.getId() != null ? cardProfile.getId().getValue() : null;
        if (profileId == null) {
            log.info("CardProfileId is required for update operation");
            throw new CardDomainException("CardProfileId is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        log.info("Starting CardProfile update service - ProfileId: {}", profileId);
        log.debug("Finding CardProfile by ID - ProfileId: {}", profileId);
        CardProfile profile = repository.findById(profileId)
                .orElseThrow(() -> {
                    log.info("CardProfile not found for update - ProfileId: {}", profileId);
                    return new CardDomainException("CardProfile not found: " + profileId,
                            StatusService.CARD_PROFILE_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });
        log.debug("CardProfile found successfully - ProfileId: {}, ProfileName: {}",
                profile.getId().getValue(), profile.getProfileName());

        if (StringUtils.isNotBlank(cardProfile.getProfileName()) &&
                !Objects.equals(profile.getProfileName(), cardProfile.getProfileName())) {
            if (repository.existsByProfileName(cardProfile.getProfileName())) {
                log.info("Duplicate profile name found for profile with Id:{}", profileId);
                throw new CardDomainException("Duplicate profile name found",
                        StatusService.DUPLICATE_PROFILE_NAME_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        // Resolve and validate reference entities before applying updates
        log.debug("Validating reference entities for CardProfile update");
        ReferenceEntities referenceEntities = validateReferenceEntities(cardProfile);
        log.debug("Reference entities validated successfully");

        applyReferenceEntities(cardProfile, referenceEntities);
        if (cardProfile.getNumPatternId() != null) {
            attachRequestedNumberPattern(profile, cardProfile);
        }

        // Check if any cards are linked to this profile
        log.debug("Checking if CardProfile has associated cards - ProfileId: {}", profileId);
        boolean hasLinkedCards = cardRepository.existsByProfileId(profileId);

        try {
            if (hasLinkedCards) {
                log.info("CardProfile has linked cards - applying restricted update - ProfileId: {}", profileId);
                // Use domain method that enforces immutability rules
                profile.updateWithLinkedCards(
                    cardProfile.getProfileName(),
                    cardProfile.getIssuingBankId(),
                    cardProfile.getCardCategoryId(),
                    cardProfile.getCardTypeId(),
                    cardProfile.getFeeProfileIdLong(),
                    cardProfile.getMinimumPasswordLength(),
                    cardProfile.getMaximumPasswordLength(),
                    cardProfile.getMinimumSecondaryPasswordLength(),
                    cardProfile.getMaximumSecondaryPasswordLength(),
                    cardProfile.getAllowedPinAttempts(),
                    cardProfile.getValidityPeriod(),
                    cardProfile.getValidityType(),
                    cardProfile.getMaxNumCreditCard(),
                    cardProfile.getMaximumAllowedAmount(),
                    cardProfile.getReloadable(),
                    cardProfile.getInitialCardStatus(),
                    cardProfile.getSalesMethod(),
                    cardProfile.getCvv2Length(),
                    cardProfile.getPin2GenMethod(),
                    cardProfile.getChangeCvv2OnExpirationDateRenewal(),
                    cardProfile.getAdditionalInfoIsRequired(),
                    cardProfile.getCardRenewalType(),
                    cardProfile.getAllowedPin2Attempts()
                );

                // Pattern handling for linked cards: APPEND
                if (cardProfile.getNumPattern() != null) {
                    profile.appendPattern(cardProfile.getNumPattern());
                }
            } else {
                log.debug("CardProfile has no associated cards - applying full update");
                // Use domain method that allows all fields to be updated
                profile.updateWithoutLinkedCards(
                    cardProfile.getProfileName(),
                    cardProfile.getIssuingBankId(),
                    cardProfile.getCardCategoryId(),
                    cardProfile.getCardTypeId(),
                    cardProfile.getFeeProfileIdLong(),
                    cardProfile.getMinimumPasswordLength(),
                    cardProfile.getMaximumPasswordLength(),
                    cardProfile.getMinimumSecondaryPasswordLength(),
                    cardProfile.getMaximumSecondaryPasswordLength(),
                    cardProfile.getAllowedPinAttempts(),
                    cardProfile.getValidityPeriod(),
                    cardProfile.getValidityType(),
                    cardProfile.getMaxNumCreditCard(),
                    cardProfile.getMaximumAllowedAmount(),
                    cardProfile.getReloadable(),
                    cardProfile.getInitialCardStatus(),
                    cardProfile.getSalesMethod(),
                    cardProfile.getCvv2Length(),
                    cardProfile.getPin2GenMethod(),
                    cardProfile.getChangeCvv2OnExpirationDateRenewal(),
                    cardProfile.getAdditionalInfoIsRequired(),
                    cardProfile.getCardRenewalType(),
                    cardProfile.getAllowedPin2Attempts()
                );

                // Pattern handling for unlinked: REPLACE
                if (cardProfile.getNumPattern() != null) {
                    profile.replacePattern(cardProfile.getNumPattern());
                }

                // Apply reference entities after domain update (infrastructure concern)
                applyReferenceEntities(profile, referenceEntities);
            }
            log.debug("Domain update logic completed successfully");
        } catch (CardDomainException e) {
            // If exception is about immutable fields, add status code and HTTP status
            if (e.getMessage() != null && e.getMessage().contains("Cannot update immutable fields")) {
                throw new CardDomainException(
                    e.getMessage(),
                    StatusService.CARD_PROFILE_HAS_CARDS,
                    HttpStatus.BAD_REQUEST
                );
            }
            throw e;
        }

        // Save updated CardProfile
        log.debug("Saving updated CardProfile to repository");
        CardProfile updated = repository.save(profile);
        log.info("CardProfile updated successfully - ProfileId: {}, ProfileName: {}",
                updated.getId().getValue(), updated.getProfileName());

        // Create domain event
        log.debug("Creating CardProfileUpdatedEvent");
        CardProfileUpdatedEvent event = new CardProfileUpdatedEvent(updated);
        log.info("CardProfile update process completed successfully - ProfileId: {}", updated.getId().getValue());
        return event;
    }

    private ReferenceEntities validateReferenceEntities(CardProfile updates) throws CardDomainException {
        Bank bank = null;
        if (updates.getIssuingBankId() != null) {
            bank = bankService.findById(updates.getIssuingBankId());
            if (!bank.isActive()) {
                throw new CardDomainException("Selected bank is inactive",
                        StatusService.ACTIVE_BANK_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        CardCategory cardCategory = null;
        if (updates.getCardCategoryId() != null) {
            cardCategory = cardCategoryService.getCardCategory(
                    GetCardCategoryQuery.builder()
                            .cardCategoryId(updates.getCardCategoryId())
                            .build());
            if (!cardCategory.isActive()) {
                throw new CardDomainException("Selected card category is inactive",
                        StatusService.ACTIVE_CARD_CATEGORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        CardType cardType = null;
        if (updates.getCardTypeId() != null) {
            cardType = cardTypeService.getCardType(
                    GetCardTypeQuery.builder()
                            .cardTypeId(updates.getCardTypeId())
                            .build());
            if (!cardType.isActive()) {
                throw new CardDomainException("Selected card type is inactive",
                        StatusService.ACTIVE_CARD_TYPE_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        CardTypeCategory cardTypeCategory = null;
        if (updates.getCardCategoryId() != null && updates.getCardTypeId() != null) {
            cardTypeCategory = cardTypeCategoryRepository
                    .findByCardTypeAndCardCategory(updates.getCardTypeId(), updates.getCardCategoryId())
                    .orElseThrow(() -> new CardDomainException(
                            "CardTypeCategory not found for the provided card type and card category",
                            StatusService.CARDTYPECATEGORY_NOT_FOUND, HttpStatus.BAD_REQUEST));
            if (!cardTypeCategory.isActive()) {
                throw new CardDomainException("Card type/category relation is inactive",
                        StatusService.CARDTYPECATEGORY_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        FeeProfile feeProfile = null;
        if (updates.getFeeProfileIdLong() != null) {
            feeProfile = feeProfileRepository.findById(updates.getFeeProfileIdLong())
                    .orElseThrow(() -> new CardDomainException(
                            "Fee profile not found with ID " + updates.getFeeProfileIdLong(),
                            StatusService.FEE_PROFILE_NOT_FOUND, HttpStatus.BAD_REQUEST));
            if (!Boolean.TRUE.equals(feeProfile.getIsActive())) {
                throw new CardDomainException("Selected fee profile is inactive",
                        StatusService.ACTIVE_FEE_PROFILE_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        return new ReferenceEntities(bank, cardCategory, cardType, cardTypeCategory, feeProfile);
    }

    private void applyReferenceEntities(CardProfile profile, ReferenceEntities references) {
        if (references.bank() != null) {
            profile.setBank(references.bank());
            profile.setBankId(references.bank().getId());
            profile.setIssuingBankId(references.bank().getId().getValue());
        }
        if (references.cardCategory() != null) {
            profile.setCardCategory(references.cardCategory());
        }
        if (references.cardType() != null) {
            profile.setCardType(references.cardType());
        }
        if (references.cardTypeCategory() != null) {
            profile.setCardTypeCategory(references.cardTypeCategory());
            profile.setCardTypeCategoryId(references.cardTypeCategory().getId());
            if (references.cardTypeCategory().getCardType() != null) {
                profile.setCardType(references.cardTypeCategory().getCardType());
            }
            if (references.cardTypeCategory().getCardCategory() != null) {
                profile.setCardCategory(references.cardTypeCategory().getCardCategory());
            }
        }
        if (references.feeProfile() != null) {
            profile.setFeeProfile(references.feeProfile());
        }
    }

    private void attachRequestedNumberPattern(CardProfile profile, CardProfile updates) throws CardDomainException {
        Long requestedPatternId = updates.getNumPatternId();
        if (requestedPatternId == null) {
            return;
        }

        List<CardNumberPattern> currentPatterns = profile.getCardNumberRangeList();
        boolean alreadyLinked = currentPatterns != null && currentPatterns.stream()
                .anyMatch(pattern -> pattern.getId() != null &&
                        Objects.equals(pattern.getId().getValue(), requestedPatternId));
        if (alreadyLinked) {
            log.debug("Card number pattern {} is already linked to profile {}", requestedPatternId, profile.getId().getValue());
            throw new CardDomainException("Card number pattern is already linked to profile",
                    StatusService.DUPLICATE_PATTERN,HttpStatus.BAD_REQUEST);
        }

        CardNumberPattern pattern = patternService.getCardNumberPattern(
                GetCardNumberPatternQuery.builder().patternId(requestedPatternId).build());
        if (pattern == null) {
            throw new CardDomainException("Card number pattern not found: " + requestedPatternId,
                    StatusService.CARD_NUMBER_PATTERN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        // Check if pattern is in ACTIVE status
        if (pattern.getPatternStatus() != CardNumberPatternStatus.ACTIVE) {
            log.info("Pattern with Id: {} does not have ACTIVE status. Current status: {}",
                    requestedPatternId, pattern.getPatternStatus());
            throw new CardDomainException(String.format("Pattern with Id:%s does not exist with Active status", requestedPatternId),
                    StatusService.ACTIVE_PATTERN_DOES_NOT_EXIST, HttpStatus.BAD_REQUEST);
        }

        if (pattern.getCardProfileId() != null &&
                !Objects.equals(pattern.getCardProfileId().getValue(), profile.getId().getValue())) {
            log.info("Card number pattern {} already belongs to profile {}", requestedPatternId,
                    pattern.getCardProfileId().getValue());
            throw new CardDomainException("Card number pattern already assigned to another profile",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        pattern.setCardProfile(profile);
        pattern.setCardProfileId(profile.getId());

        // Update pattern status to ASSIGN
        pattern.setPatternStatus(CardNumberPatternStatus.ASSIGN);
        log.debug("Updating pattern status to ASSIGN - PatternId: {}", pattern.getId().getValue());
        patternService.updateCardNumberPattern(pattern);
        log.info("New pattern {} attached to profile {} for domain processing",
                requestedPatternId, profile.getId().getValue());
    }

    private record ReferenceEntities(
            Bank bank,
            CardCategory cardCategory,
            CardType cardType,
            CardTypeCategory cardTypeCategory,
            FeeProfile feeProfile) {
    }

    @Override
    public CardProfileActivatedEvent activateCardProfile(CardProfile cardProfile) throws CardDomainException {
        Long profileId = cardProfile.getId() != null ? cardProfile.getId().getValue() : null;
        if (profileId == null) {
            log.info("CardProfileId is required for activation operation");
            throw new CardDomainException("CardProfileId is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        log.info("Starting CardProfile activation service - ProfileId: {}", profileId);
        log.debug("Finding CardProfile by ID - ProfileId: {}", profileId);
        CardProfile profile = repository.findById(profileId)
                .orElseThrow(() -> {
                    log.info("CardProfile not found for activation - ProfileId: {}", profileId);
                    return new CardDomainException("CardProfile not found: " + profileId);
                });
        log.debug("CardProfile found successfully - ProfileId: {}, ProfileName: {}, CurrentStatus: {}",
                profile.getId().getValue(), profile.getProfileName(), profile.getIsActive());

        // Activate CardProfile
        log.debug("Activating CardProfile - ProfileId: {}", profileId);
        profile.activate();
        log.debug("CardProfile activation completed");

        // Save activated CardProfile
        log.debug("Saving activated CardProfile to repository");
        CardProfile updated = repository.save(profile);
        log.info("CardProfile activated successfully - ProfileId: {}, ProfileName: {}",
                updated.getId().getValue(), updated.getProfileName());

        // Create domain event
        log.debug("Creating CardProfileActivatedEvent");
        CardProfileActivatedEvent event = new CardProfileActivatedEvent(updated);
        log.info("CardProfile activation process completed successfully - ProfileId: {}", updated.getId().getValue());
        return event;
    }

    // ---------------------- DEACTIVATE ----------------------
    @Override
    public CardProfileDeactivatedEvent deactivateCardProfile(CardProfile cardProfile) throws CardDomainException {
        Long profileId = cardProfile.getId() != null ? cardProfile.getId().getValue() : null;
        if (profileId == null) {
            log.info("CardProfileId is required for deactivation operation");
            throw new CardDomainException("CardProfileId is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        log.info("Finding CardProfile by ID - ProfileId: {}", profileId);
        CardProfile profile = repository.findById(profileId)
                .orElseThrow(() -> {
                    log.info("CardProfile not found for deactivation - ProfileId: {}", profileId);
                    return new CardDomainException("CardProfile not found: " + profileId, StatusService.ID_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });
        log.info("CardProfile found successfully - ProfileId: {}, ProfileName: {}, CurrentStatus: {}",
                profile.getId().getValue(), profile.getProfileName(), profile.getIsActive());
        // Deactivate CardProfile
        log.info("Deactivating CardProfile - ProfileId: {}", profileId);
        profile.deactivate();
        log.debug("CardProfile deactivation completed");

        // Save deactivated CardProfile
        CardProfile updated = repository.save(profile);
        log.info("CardProfile deactivated successfully - ProfileId: {}, ProfileName: {}",
                updated.getId().getValue(), updated.getProfileName());

        log.debug("Creating CardProfileDeactivatedEvent");
        CardProfileDeactivatedEvent event = new CardProfileDeactivatedEvent(updated);
        log.info("CardProfile deactivation process completed successfully - ProfileId: {}", updated.getId().getValue());
        return event;
    }

    // ---------------------- GET ONE ----------------------
    @Override
    public CardProfile getCardProfile(GetCardProfileQuery query) throws CardDomainException {
        log.info("Starting CardProfile retrieval service - ProfileId: {}", query.getCardProfileId());
        CardProfile cardProfile;
        cardProfile = repository.findById(query.getCardProfileId())
                .orElseThrow(() -> new CardDomainException("Card profile not found with id: " + query.getCardProfileId()));
        log.info("Card profile found: {}", cardProfile.getProfileName());
        return cardProfile;
    }

    // ---------------------- GET ALL ----------------------
    @Override
    public List<CardProfile> getAllCardProfiles(GetAllCardProfilesQuery query) throws CardDomainException {
        log.info("Starting CardProfiles retrieval service - ActiveOnly: {}", query.getActiveOnly());
        List<CardProfile> cardProfiles;
        cardProfiles = repository.findAll();
        return cardProfiles;
    }

    @Override
    public Page<CardProfile> listCardProfiles(Map<String, String> params, String caller, String ip) throws CardDomainException {
        log.info("Starting active CardProfiles retrieval service");
        try {
            Page<CardProfile> allProfiles = repository.findAllProfiles(params);
            return allProfiles;
        } catch (Exception e) {
            log.info("info occurred in active CardProfiles retrieval service - info: {}", e.getMessage(), e);
            throw new CardDomainException("", StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public CardProfile findById(Long id) throws CardDomainException {
        log.info("Finding CardProfile by ID - ProfileId: {}", id);
        CardProfile cardProfile = repository.findById(id)
                .orElseThrow(() -> {
                    log.info("CardProfile not found with id: {}", id);
                    return new CardDomainException("Card profile not found with id: " + id,
                            StatusService.CARD_PROFILE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
        log.info("CardProfile found successfully - ProfileId: {}, ProfileName: {}",
                id, cardProfile.getProfileName());
        return cardProfile;
    }
}
