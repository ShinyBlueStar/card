package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardProfile.ActivateCardProfileCommand;
import com.sample.system.card.service.domain.command.cardProfile.CreateCardProfileCommand;
import com.sample.system.card.service.domain.command.cardProfile.DeactivateCardProfileCommand;
import com.sample.system.card.service.domain.command.cardProfile.UpdateCardProfileCommand;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.enums.*;
import com.sample.system.card.service.domain.response.bank.BankResponse;
import com.sample.system.card.service.domain.response.feeProfile.FeeProfileResponse;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.response.base.EnumResponse;
import com.sample.system.card.service.domain.response.profile.CardProfileListResponse;
import com.sample.system.card.service.domain.response.profile.CreateCardProfileResponse;
import com.sample.system.card.service.domain.response.profile.GetCardProfileResponse;
import com.sample.system.card.service.domain.response.profile.UpdateCardProfileResponse;
import com.sample.system.card.service.domain.valueObject.BankId;
import com.sample.system.card.service.domain.valueObject.CardProfileId;
import com.sample.system.card.service.domain.valueObject.FeeProfileId;
import com.sample.system.card.service.domain.utility.date.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.response.cardCategory.GetCardCategoryResponse;
import com.sample.system.card.service.domain.response.cardType.GetCardTypeResponse;

/**
 * Mapper for CardProfile domain entity and DTOs
 * Following DDD patterns for domain layer mapping
 */
@Component
public class CardProfileDataMapper {

    private final CardNumberPatternDataMapper  cardNumberPatternDataMapper;

    public CardProfileDataMapper(CardNumberPatternDataMapper cardNumberPatternDataMapper) {
        this.cardNumberPatternDataMapper = cardNumberPatternDataMapper;
    }

    public CardProfile createCommandToProfile(CreateCardProfileCommand createCardCommand) {
        return CardProfile.builder()
                .profileName(createCardCommand.getProfileName())
                .issuingBankId(createCardCommand.getIssuingBankId())
                .bankId(createCardCommand.getIssuingBankId() != null ? new BankId(createCardCommand.getIssuingBankId()) : null)
                .feeProfileId(createCardCommand.getFeeProfileId() != null ? new FeeProfileId(createCardCommand.getFeeProfileId()) : null)
                .feeProfileIdLong(createCardCommand.getFeeProfileId())
                .cardCategoryId(createCardCommand.getCardCategoryId())
                .cardTypeId(createCardCommand.getCardTypeId())
                .maxNumCreditCard(createCardCommand.getMaxNumCreditCard())
                .maximumAllowedAmount(createCardCommand.getMaximumAllowedAmount())
                .minimumPasswordLength(createCardCommand.getMinimumPasswordLength())
                .maximumPasswordLength(createCardCommand.getMaximumPasswordLength())
                .minimumSecondaryPasswordLength(createCardCommand.getMinimumSecondaryPasswordLength())
                .maximumSecondaryPasswordLength(createCardCommand.getMaximumSecondaryPasswordLength())
                .allowedPinAttempts(createCardCommand.getAllowedPinAttempts())
                .allowedPin2Attempts(createCardCommand.getAllowedPin2Attempts())
                .validityPeriod(createCardCommand.getValidityPeriod())
                .validityType(createCardCommand.getValidityType())
                .reloadable(resolveReloadable(createCardCommand.getReloadable()))
                .salesMethod(resolveSalesMethod(createCardCommand.getSalesMethod()))
                .cardIssueMethod(createCardCommand.getCardIssueMethod()!= null?CardIssueMethod.fromCode(createCardCommand.getCardIssueMethod()):null)
                .cardRenewalType(createCardCommand.getCardRenewalType() != null
                        ? CardRenewalType.fromCode(createCardCommand.getCardRenewalType())
                        : null)
                .changeCvv2OnExpirationDateRenewal(createCardCommand.getChangeCvv2OnExpirationDateRenewal())
                .isActive(createCardCommand.getIsActive())
                .additionalInfoIsRequired(createCardCommand.getAdditionalInfoIsRequired())
                .cvv2Length(createCardCommand.getCvv2Length())
                .pin2GenMethod(parsePin2GenMethod(createCardCommand.getPin2GenMethod()))
                .NumPatternId(createCardCommand.getCardNumberPatternId())
                .initialCardStatus(createCardCommand.getInitialCardStatus())
                .build();
    }
    public CreateCardProfileResponse profileToCreateResponse(CardProfile profile, String message) {
        return new CreateCardProfileResponse(
                profile.getId().getValue(),
                message
        );
    }

    private Reloadable resolveReloadable(Integer reloadable) {
        return reloadable != null ? Reloadable.fromCode(reloadable) : Reloadable.DEFAULT;
    }

    private SalesMethod resolveSalesMethod(Integer salesMethodCode) {
        return salesMethodCode != null
                ? SalesMethod.fromCode(salesMethodCode)
                : SalesMethod.DEFAULT;
    }

    public GetCardProfileResponse profileToGetResponse(CardProfile profile) {
        com.sample.system.card.service.domain.entity.CardTypeCategory cardTypeCategoryEntity = profile.getCardTypeCategory();
        CardType displayCardType = Optional.ofNullable(cardTypeCategoryEntity)
                .map(com.sample.system.card.service.domain.entity.CardTypeCategory::getCardType)
                .orElse(profile.getCardType());

        return new GetCardProfileResponse(
                profile.getId().getValue(),
                profile.getProfileName(),
                resolveCardTypeDisplay(displayCardType),
                mapCardTypeToResponse(profile),
                mapCardCategoryToResponse(profile),
                mapBankIdToResponse(profile.getBank()),
                mapFeeProfileToResponse(profile.getFeeProfile()),
                Optional.ofNullable(profile.getCardNumberRangeList())
                        .orElseGet(List::of)
                        .stream()
                        .map(cardNumberPatternDataMapper::patternToGetResponse)
                        .toList(),
                profile.getMaxNumCreditCard(),
                profile.getMaximumAllowedAmount(),
                profile.getMinimumPasswordLength(),
                profile.getMaximumPasswordLength(),
                profile.getMinimumSecondaryPasswordLength(),
                profile.getMaximumSecondaryPasswordLength(),
                profile.getAllowedPinAttempts(),
                profile.getAllowedPin2Attempts(),
                EnumResponse.fromEnum(profile.getReloadable()),
                profile.getValidityPeriod(),
                profile.getValidityType(),
                EnumResponse.fromEnum(profile.getCardRenewalType()),
//                profile.getProductCodeList(),
                resolveInitialCardStatus(profile.getInitialCardStatus()),
                EnumResponse.fromEnum(profile.getSalesMethod()),
                profile.getChangeCvv2OnExpirationDateRenewal(),
                profile.getCvv2Length(),
                profile.getIsActive(),
                profile.getAdditionalInfoIsRequired(),
                EnumResponse.fromEnum(profile.getPin2GenMethod()),
                profile.getCreatedBy(),
                convertToPersianDate(profile.getCreatedDate()),
                profile.getLastModifiedBy(),
                convertToPersianDate(profile.getLastModifiedDate()),
                null,
                EnumResponse.fromEnum(profile.getCardIssueMethod())
        );
    }

    public List<GetCardProfileResponse> profilesToGetResponses(List<CardProfile> profiles) {
        return profiles.stream()
                .map(this::profileToGetResponse)
                .toList();
    }

    public CardProfileListResponse profilesToGetListResponse(Page<CardProfile> profiles) {
        if (profiles == null) return null;

        CardProfileListResponse cardProfileListResponse = new CardProfileListResponse();
        cardProfileListResponse.setList(profiles.getContent().stream()
                .map(this::profileToGetResponse).toList());
        cardProfileListResponse.setNumber(profiles.getNumber());
        cardProfileListResponse.setSize(profiles.getSize());
        cardProfileListResponse.setTotalElements(profiles.getTotalElements());
        cardProfileListResponse.setTotalPages(profiles.getTotalPages());
        return cardProfileListResponse;
    }

    // Helper methods for mapping Domain Entities to Response DTOs
    private BankResponse mapBankIdToResponse(Bank bank) {
        if (bank == null || bank.getId() == null) {
            return null;
        }
        return new BankResponse(
                bank.getId().getValue(),
                bank.getName(),
                bank.getBinCode(),
                bank.getIsActive(),
                null);
    }

    private GetCardTypeResponse mapCardTypeToResponse(CardProfile profile) {
        if (profile == null) {
            return null;
        }

        com.sample.system.card.service.domain.entity.CardTypeCategory typeCategory = profile.getCardTypeCategory();
        CardType cardType = Optional.ofNullable(typeCategory)
                .map(com.sample.system.card.service.domain.entity.CardTypeCategory::getCardType)
                .orElse(profile.getCardType());
        Long id = null;
        String code = null;
        String name = null;
        String description = null;
        Boolean isActive = null;

        if (cardType != null) {
            if (cardType.getId() != null) {
                id = cardType.getId().getValue();
            }
            if (cardType.getCode() != null) {
                code = String.valueOf(cardType.getCode());
            }
            name = cardType.getName();
            description = cardType.getDescription();
            isActive = cardType.getIsActive();
        } else if (profile.getCardTypeId() != null) {
            id = profile.getCardTypeId();
        }

        if (id == null && code == null && name == null && description == null && isActive == null) {
            return null;
        }

        return new GetCardTypeResponse(id, code, name, description, isActive);
    }

    private GetCardCategoryResponse mapCardCategoryToResponse(CardProfile profile) {
        if (profile == null) {
            return null;
        }

        com.sample.system.card.service.domain.entity.CardTypeCategory typeCategory = profile.getCardTypeCategory();
        CardCategory cardCategory = Optional.ofNullable(typeCategory)
                .map(com.sample.system.card.service.domain.entity.CardTypeCategory::getCardCategory)
                .orElse(profile.getCardCategory());
        Long id = null;
        String code = null;
        String name = null;
        String description = null;
        Boolean isActive = null;

        if (cardCategory != null) {
            if (cardCategory.getId() != null) {
                id = cardCategory.getId().getValue();
            }
            if (cardCategory.getCode() != null) {
                code = String.valueOf(cardCategory.getCode());
            }
            name = cardCategory.getName();
            description = cardCategory.getDescription();
            isActive = cardCategory.getIsActive();
        } else if (profile.getCardCategoryId() != null) {
            id = profile.getCardCategoryId();
        }

        if (id == null && code == null && name == null && description == null && isActive == null) {
            return null;
        }

        return new GetCardCategoryResponse(id, code, name, description, isActive);
    }

    private FeeProfileResponse mapFeeProfileToResponse(com.sample.system.card.service.domain.entity.FeeProfile feeProfile) {
        if (feeProfile == null || feeProfile.getId() == null) {
            return null;
        }
        return new FeeProfileResponse(
                feeProfile.getId().getValue(),
                feeProfile.getName(),
                null,
                feeProfile.getIsActive(),
                null,
                null);
    }

    private Boolean resolveInitialCardStatus(Boolean initialCardStatus) {
        return initialCardStatus != null ? initialCardStatus : Boolean.FALSE;
    }

    private String convertToPersianDate(Timestamp timestamp) {
        return Optional.ofNullable(timestamp)
                .map(ts -> DateUtils.getLocaleDate(
                        DateUtils.FARSI_LOCALE,
                        ts,
                        DateUtils.PERSIAN_DATE_FORMAT,
                        false))
                .orElse(null);
    }

    public CardProfile updateCommandToProfile(UpdateCardProfileCommand command) throws CardDomainException {
        CardProfile cardProfile = new CardProfile();
        cardProfile.setId(new CardProfileId(command.getCardProfileId()));

        cardProfile.setProfileName(command.getProfileName());
        cardProfile.setIssuingBankId(command.getIssuingBankId());
        cardProfile.setBankId(command.getIssuingBankId() != null ? new BankId(command.getIssuingBankId()) : null);
        cardProfile.setCardCategoryId(command.getCardCategoryId());
        cardProfile.setCardTypeId(command.getCardTypeId());
        cardProfile.setFeeProfileId(command.getFeeProfileId() != null ? new FeeProfileId(command.getFeeProfileId()) : null);
        cardProfile.setFeeProfileIdLong(command.getFeeProfileId());
        if (command.getCardNumberPatternId() != null) {
            cardProfile.setNumPatternId(command.getCardNumberPatternId());
        }
        if (command.getMaxNumCreditCard() != null) {
            cardProfile.setMaxNumCreditCard(command.getMaxNumCreditCard());
        }
        if (command.getMaximumAllowedAmount() != null) {
            cardProfile.setMaximumAllowedAmount(command.getMaximumAllowedAmount());
        }
        if (command.getMinimumPasswordLength() != null) {
            cardProfile.setMinimumPasswordLength(command.getMinimumPasswordLength());
        }
        if (command.getMaximumPasswordLength() != null) {
            cardProfile.setMaximumPasswordLength(command.getMaximumPasswordLength());
        }
        if (command.getMinimumSecondaryPasswordLength() != null) {
            cardProfile.setMinimumSecondaryPasswordLength(command.getMinimumSecondaryPasswordLength());
        }
        if (command.getMaximumSecondaryPasswordLength() != null) {
            cardProfile.setMaximumSecondaryPasswordLength(command.getMaximumSecondaryPasswordLength());
        }
        if (command.getAllowedPinAttempts() != null) {
            cardProfile.setAllowedPinAttempts(command.getAllowedPinAttempts());
        }
        if (command.getAllowedPin2Attempts() != null) {
            cardProfile.setAllowedPin2Attempts(command.getAllowedPin2Attempts());
        }
        if (command.getValidityPeriod() != null) {
            cardProfile.setValidityPeriod(command.getValidityPeriod());
        }
        if (command.getValidityType() != null) {
            cardProfile.setValidityType(command.getValidityType());
        }
        if (command.getReloadable() != null) {
            cardProfile.setReloadable(Reloadable.fromCode(command.getReloadable()));
        }
        if (command.getSalesMethod() != null) {
            cardProfile.setSalesMethod(parseSalesMethod(command.getSalesMethod()));
        }
        if (command.getChangeCvv2OnExpirationDateRenewal() != null) {
            cardProfile.setChangeCvv2OnExpirationDateRenewal(command.getChangeCvv2OnExpirationDateRenewal());
        }
        if (command.getInitialCardStatus() != null) {
            cardProfile.setInitialCardStatus(command.getInitialCardStatus());
        }
        if (command.getIsActive() != null) {
            cardProfile.setIsActive(command.getIsActive());
        }
        if (command.getAdditionalInfoIsRequired() != null) {
            cardProfile.setAdditionalInfoIsRequired(command.getAdditionalInfoIsRequired());
        }
        if (command.getCvv2Length() != null) {
            cardProfile.setCvv2Length(command.getCvv2Length());
        }
        if (command.getPin2GenMethod() != null) {
            cardProfile.setPin2GenMethod(parsePin2GenMethod(command.getPin2GenMethod()));
        }
        if (command.getIssueMethod() != null) {
            cardProfile.setCardIssueMethod(CardIssueMethod.fromCode(command.getIssueMethod()));
        }
        if (command.getCardRenewalType() != null) {
            cardProfile.setCardRenewalType(CardRenewalType.fromCode(command.getCardRenewalType()));
        }
        return cardProfile;
    }

    public CardProfile activateCommandToProfile(ActivateCardProfileCommand command) {
        CardProfile cardProfile = new CardProfile();
        cardProfile.setId(new CardProfileId(command.getCardProfileId()));
        cardProfile.setIsActive(true);
        return cardProfile;
    }

    public CardProfile deactivateCommandToProfile(DeactivateCardProfileCommand command) {
        CardProfile cardProfile = new CardProfile();
        cardProfile.setId(new CardProfileId(command.getCardProfileId()));
        cardProfile.setIsActive(false);
        return cardProfile;
    }

    public UpdateCardProfileResponse cardProfileToUpdateCardProfileResponse(CardProfile profile, String message) {
        return new UpdateCardProfileResponse(
                profile.getId().getValue(),
                message,
                profile.getIsActive()
        );
    }

    private String resolveCardTypeDisplay(CardType cardType) {
        if (cardType == null) {
            return null;
        }
        if (cardType.getName() != null && !cardType.getName().isBlank()) {
            return cardType.getName();
        }
        return cardType.getDescription();
    }

    /**
     * Convert enum to EnumResponse (code and description)
     */

    public SalesMethod parseSalesMethod(String value) {
        if (value == null) return null;
        try {
            return SalesMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try to match by description
            for (SalesMethod type : SalesMethod.values()) {
                if (type.getDescription().equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    public SalesMethod parseSalesMethod(Integer code) {
        if (code == null) return null;
        return SalesMethod.fromCode(code);
    }

    public Pin2GenMethod parsePin2GenMethod(Integer code) {
        if (code == null) return null;
        return Pin2GenMethod.fromCode(code);
    }
}
