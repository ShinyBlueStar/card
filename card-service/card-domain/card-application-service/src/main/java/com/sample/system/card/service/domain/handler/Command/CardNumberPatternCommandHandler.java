package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.numberPattern.CreateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.DeactivateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.UpdateCardNumberPatternCommand;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.event.cardNumberPattern.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardNumberPatternDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardNumberPatternService;
import com.sample.system.card.service.domain.response.number.CreateCardNumberPatternResponse;
import com.sample.system.card.service.domain.response.number.UpdateCardNumberPatternResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class CardNumberPatternCommandHandler {

    private final CardNumberPatternService cardNumberPatternService;
    private final CardNumberPatternDataMapper cardNumberPatternDataMapper;

    @Transactional
    public CreateCardNumberPatternResponse createCardNumberPattern(CreateCardNumberPatternCommand command) throws CardDomainException {
        log.info("Starting CardNumberPattern creation service - From: {}, To: {}, ProductCode: {}",
                command.getCardNumberFrom(), command.getCardNumberTo(), command.getProductCode());

        log.debug("Mapping CreateCardNumberPatternCommand to CardNumberPattern domain entity");
        CardNumberPattern cardNumberPattern = cardNumberPatternDataMapper.createCommandToPattern(command);
        log.debug("Command mapping completed successfully");

        CardNumberPatternCreatedEvent event = cardNumberPatternService.createCardNumberPattern(cardNumberPattern);
        log.debug("Mapping CardNumberPattern to CreateCardNumberPatternResponse");
        return cardNumberPatternDataMapper.patternToCreateResponse(event.getCardNumberPattern(),
                "success");
    }

    @Transactional
    public UpdateCardNumberPatternResponse updateCardNumberPattern(UpdateCardNumberPatternCommand command) throws CardDomainException {
        log.info("Starting CardNumberPattern update service - PatternId: {}", command.getPatternId());

        log.debug("Mapping UpdateCardNumberPatternCommand to CardNumberPattern domain entity");
        CardNumberPattern cardNumberPattern = cardNumberPatternDataMapper.updateCommandToPattern(command);
        log.debug("Command mapping completed successfully");

        CardNumberPatternUpdatedEvent event = cardNumberPatternService.updateCardNumberPattern(cardNumberPattern);
        log.debug("CardNumberPattern update completed by CommandHandler - PatternId: {}",
                event.getCardNumberPattern().getId().getValue());

        log.debug("Mapping CardNumberPattern to UpdateCardNumberPatternResponse");
        UpdateCardNumberPatternResponse response = cardNumberPatternDataMapper.patternToUpdateResponse(
                event.getCardNumberPattern(), "Card Number Pattern updated successfully!");

        log.info("CardNumberPattern update service completed successfully - PatternId: {}, From: {}, To: {}",
                event.getCardNumberPattern().getId().getValue(),
                event.getCardNumberPattern().getCardNumberFrom(),
                event.getCardNumberPattern().getCardNumberTo());
        return response;
    }

    @Transactional
    public UpdateCardNumberPatternResponse deactivateCardNumberPattern(DeactivateCardNumberPatternCommand command) throws CardDomainException {
        log.info("Starting CardNumberPattern deactivate service - PatternId: {}", command.getPatternId());

        log.debug("Mapping DeactivateCardNumberPatternCommand to CardNumberPattern domain entity");
        CardNumberPattern cardNumberPattern = cardNumberPatternDataMapper.deactivateCommandToPattern(command);
        log.debug("Command mapping completed successfully");

        CardNumberPatternUpdatedEvent event = cardNumberPatternService.deactivateCardNumberPattern(cardNumberPattern);
        log.debug("CardNumberPattern deactivate completed by CommandHandler - PatternId: {}",
                event.getCardNumberPattern().getId().getValue());

        log.debug("Mapping CardNumberPattern to UpdateCardNumberPatternResponse");
        UpdateCardNumberPatternResponse response = cardNumberPatternDataMapper.patternToUpdateResponse(
                event.getCardNumberPattern(), "الگوی شماره کارت با موفقیت غیرفعال شد");

        log.info("CardNumberPattern deactivate service completed successfully - PatternId: {}",
                event.getCardNumberPattern().getId().getValue());
        return response;
    }

    @Transactional
    public List<CreateCardNumberPatternResponse> createCardNumberPatterns(List<CreateCardNumberPatternCommand> commands) throws CardDomainException {
        log.info("Bulk creating {} CardNumberPatterns", commands.size());

        log.debug("Mapping CreateCardNumberPatternCommand list to CardNumberPattern list");
        List<CardNumberPattern> cardNumberPatterns = commands.stream()
                .map(cardNumberPatternDataMapper::createCommandToPattern)
                .toList();
        log.debug("Command mapping completed successfully");

        CardNumberPatternsFetchedEvent event = cardNumberPatternService.createCardNumberPatterns(cardNumberPatterns);
        List<CreateCardNumberPatternResponse> responses = event.getCardNumberPatterns().stream()
                .map(pattern -> cardNumberPatternDataMapper.patternToCreateResponse(pattern, "Card Number Pattern created successfully!"))
                .toList();

        log.info("Bulk created {} CardNumberPatterns", responses.size());
        return responses;
    }
}