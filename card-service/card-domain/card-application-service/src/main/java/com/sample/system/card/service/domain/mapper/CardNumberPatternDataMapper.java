package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.numberPattern.CreateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.DeactivateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.UpdateCardNumberPatternCommand;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.enums.GeneralStatusEnum;
import com.sample.system.card.service.domain.response.number.CardNumberPatternListResponse;
import com.sample.system.card.service.domain.response.number.CreateCardNumberPatternResponse;
import com.sample.system.card.service.domain.response.number.GetCardNumberPatternResponse;
import com.sample.system.card.service.domain.response.number.UpdateCardNumberPatternResponse;
import com.sample.system.card.service.domain.utility.date.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class CardNumberPatternDataMapper {

    public CardNumberPattern createCommandToPattern(CreateCardNumberPatternCommand createCommand) {
        CardNumberPatternStatus patternStatus = null;
        if (createCommand.getStatus() != null) {
            GeneralStatusEnum status = GeneralStatusEnum.fromCode(createCommand.getStatus());
            patternStatus = status == GeneralStatusEnum.ACTIVE
                    ? CardNumberPatternStatus.ACTIVE
                    : CardNumberPatternStatus.INACTIVE;
        }

        return CardNumberPattern.builder()
                .cardNumberFrom(createCommand.getCardNumberFrom())
                .cardNumberTo(createCommand.getCardNumberTo())
                .productCode(createCommand.getProductCode())
                .name(createCommand.getName())
                .patternStatus(patternStatus)
                .cardNumGenerationMethod(createCommand.getCardNumGenerationMethod() != null
                        ? CardNumGenerationMethod.fromCode(createCommand.getCardNumGenerationMethod())
                        : null)
                .build();
    }

    public CardNumberPattern updateCommandToPattern(UpdateCardNumberPatternCommand updateCommand) {
        CardNumberPatternStatus patternStatus = null;
        if (updateCommand.getStatus() != null) {
            GeneralStatusEnum status = GeneralStatusEnum.fromCode(updateCommand.getStatus());
            patternStatus = status == GeneralStatusEnum.ACTIVE
                    ? CardNumberPatternStatus.ACTIVE
                    : CardNumberPatternStatus.INACTIVE;
        }

        CardNumberPattern pattern = CardNumberPattern.builder()
                .cardNumberFrom(updateCommand.getCardNumberFrom())
                .cardNumberTo(updateCommand.getCardNumberTo())
                .name(updateCommand.getName())
                .patternStatus(patternStatus)
                .build();

        // Set the ID after building since BaseEntity manages it
        if (updateCommand.getPatternId() != null) {
            pattern.setId(new com.sample.system.card.service.domain.valueObject.CardNumberPatternId(updateCommand.getPatternId()));
        }
        return pattern;
    }

    public CardNumberPattern deactivateCommandToPattern(DeactivateCardNumberPatternCommand command) {
        CardNumberPattern pattern = CardNumberPattern.builder().build();

        // Set the ID
        if (command.getPatternId() != null) {
            pattern.setId(new com.sample.system.card.service.domain.valueObject.CardNumberPatternId(command.getPatternId()));
        }
        return pattern;
    }

    /**
     * Convert CardNumberPattern domain entity to CreateCardNumberPatternResponse
     */
    public CreateCardNumberPatternResponse patternToCreateResponse(CardNumberPattern pattern, String message) {
        return CreateCardNumberPatternResponse.builder()
                .cardNumberPatternId(pattern.getId() != null ? pattern.getId().getValue() : null)
                .message(message)
                .build();
    }

    /**
     * Convert CardNumberPattern domain entity to UpdateCardNumberPatternResponse
     */
    public UpdateCardNumberPatternResponse patternToUpdateResponse(CardNumberPattern pattern, String message) {
        return UpdateCardNumberPatternResponse.builder()
                .cardNumberPatternId(pattern.getId() != null ? pattern.getId().getValue() : null)
                .message(message)
                .build();
    }

    /**
     * Convert CardNumberPattern domain entity to GetCardNumberPatternResponse
     */
    public GetCardNumberPatternResponse patternToGetResponse(CardNumberPattern pattern) {
        return new GetCardNumberPatternResponse(
                pattern.getId() != null ? pattern.getId().getValue() : null,
                pattern.getCardNumberFrom(),
                pattern.getCardNumberTo(),
                pattern.getCardProfile() != null && pattern.getCardProfile().getId() != null
                        ? pattern.getCardProfile().getId().getValue()
                        : (pattern.getCardProfileId() != null ? pattern.getCardProfileId().getValue() : null),
                pattern.getProductCode(),
                pattern.getName(),
                pattern.getCardNumGenerationMethod() != null ? pattern.getCardNumGenerationMethod()
                        .getDescription() : null,
                pattern.getPatternStatus() != null ? pattern.getPatternStatus()
                        .getDescription() : null,
                pattern.getFirstCardNumber(),
                pattern.getLastCardNumber(),
                pattern.getNextCardNumber(),
                pattern.getCreatedBy(),
                convertToPersianDate(pattern.getCreatedDate()),
                pattern.getLastModifiedBy(),
                convertToPersianDate(pattern.getLastModifiedDate()),
                "الگوی شماره کارت با موفقیت دریافت شد");
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

    /**
     * Convert list of CardNumberPattern domain entities to list of GetCardNumberPatternResponse
     */
    public List<GetCardNumberPatternResponse> patternsToGetResponses(List<CardNumberPattern> patterns) {
        return patterns.stream()
                .map(this::patternToGetResponse)
                .toList();
    }

    /**
     * Helper method to check if a card number falls within the pattern range
     */
    public boolean isCardNumberInRange(CardNumberPattern pattern, String cardNumber) {
        return pattern != null && pattern.contains(cardNumber);
    }

    /**
     * Helper method to check if pattern is available
     */
    public boolean isPatternAvailable(CardNumberPattern pattern) {
        // Note: isAvailable() method is commented in CardNumberPattern entity
        // because status field doesn't exist in Entity
        return pattern != null;
    }

    /**
     * Convert Page of CardNumberPattern domain entities to CardNumberPatternListResponse
     */
    public CardNumberPatternListResponse patternsToGetListResponse(Page<CardNumberPattern> patterns) {
        if (patterns == null) return null;

        CardNumberPatternListResponse response = new CardNumberPatternListResponse();
        response.setList(patterns.getContent().stream()
                .map(this::patternToGetResponse).toList());
        response.setNumber(patterns.getNumber());
        response.setSize(patterns.getSize());
        response.setTotalElements(patterns.getTotalElements());
        response.setTotalPages(patterns.getTotalPages());
        return response;
    }
}

