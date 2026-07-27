package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.card.*;
import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.enums.CardStatusAction;
import com.sample.system.card.service.domain.event.card.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.response.card.UpdateCardResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class CardCommandHandler {

    private final CardService cardService;
    private final CardDataMapper cardDataMapper;

    @Transactional
    public UpdateCardResponse blockCard(BlockCardCommand command) throws CardDomainException {
        log.info("Starting Card blocking handler - CardId: {}", command.getCardId());
        Card card = cardDataMapper.toCard(command.getCardId());
        CardBlockedEvent event = cardService.blockCard(card, command.getReasonId(), command.getBlockedBy());
        log.debug("Mapping Card to UpdateCardResponse");
        UpdateCardResponse response = cardDataMapper.cardToUpdateCardResponse(
                event.getCard(), "Card blocked successfully!");
        log.info("Card blocking handler completed successfully - CardId: {}",
                event.getCard().getId() != null ? event.getCard().getId().getValue() : null);
        return response;
    }

    @Transactional
    public UpdateCardResponse activateCard(ActivateCardCommand command) throws CardDomainException {
        log.info("Starting Card activation handler - CardId: {}", command.getCardId());
        CardActivatedEvent event = cardService.activateCard(command.getCardId(), command.getReasonId());
        log.debug("Mapping Card to UpdateCardResponse");
        UpdateCardResponse response = cardDataMapper.cardToUpdateCardResponse(
                event.getCard(), "Card activated successfully!");
        log.info("Card activation handler completed successfully - CardId: {}",
                event.getCard().getId() != null ? event.getCard().getId().getValue() : null);
        return response;
    }

    @Transactional
    public UpdateCardResponse deactivateCard(DeactivateCardCommand command) throws CardDomainException {
        log.info("Starting Card deactivation handler - CardId: {}", command.getCardId());
        CardDeactivatedEvent event = cardService.deactivateCard(command.getCardId(), command.getReasonId(), command.getDeactivatedBy());
        log.debug("Mapping Card to UpdateCardResponse");
        UpdateCardResponse response = cardDataMapper.cardToUpdateCardResponse(
                event.getCard(), "Card deactivated successfully!");
        log.info("Card deactivation handler completed successfully - CardId: {}",
                event.getCard().getId() != null ? event.getCard().getId().getValue() : null);
        return response;
    }

    @Transactional
    public UpdateCardResponse changeStatus(ChangeCardStatusCommand command) throws CardDomainException {
        log.info("Changing card status - CardId: {}, Action: {}", command.getCardId(), command.getAction());
        UpdateCardResponse response;
        CardStatusAction action = command.getAction();
        switch (action) {
            case ACTIVATE -> {
                CardActivatedEvent event = cardService.activateCard(command.getCardId(), command.getReasonId());
                response = cardDataMapper.cardToUpdateCardResponse(event.getCard(), "Card activated successfully!");
            }
            case DEACTIVATE -> {
                if (command.getReasonId() == null) {
                    throw new CardDomainException("reasonId is required for deactivation",
                            StatusService.REASON_IS_MANDATORY, HttpStatus.BAD_REQUEST);
                }
                String actor = resolveCurrentUsername();
                CardDeactivatedEvent event = cardService.deactivateCard(command.getCardId(), command.getReasonId(), actor);
                response = cardDataMapper.cardToUpdateCardResponse(event.getCard(), "Card deactivated successfully!");
            }
            case BLOCK -> {
                if (command.getReasonId() == null) {
                    throw new CardDomainException("reasonId is required for blocking",
                            StatusService.REASON_IS_MANDATORY, HttpStatus.BAD_REQUEST);
                }
                String actor = resolveCurrentUsername();
                Card card = cardDataMapper.toCard(command.getCardId());
                CardBlockedEvent event = cardService.blockCard(card, command.getReasonId(), actor);
                response = cardDataMapper.cardToUpdateCardResponse(event.getCard(), "Card blocked successfully!");
            }
            case UNBLOCK -> {
                if (command.getReasonId() == null) {
                    throw new CardDomainException("reasonId is required for nonBlocking",
                            StatusService.REASON_IS_MANDATORY, HttpStatus.BAD_REQUEST);
                }
                String actor = resolveCurrentUsername();
                Card card = cardDataMapper.toCard(command.getCardId());
                CardUnblockedEvent event = cardService.unblockCard(card, command.getReasonId(), actor);
                response = cardDataMapper.cardToUpdateCardResponse(event.getCard(), "Card unBlocked successfully!");
            }

            default -> throw new CardDomainException("Unsupported card status action: " + command.getAction(),
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        log.info("Card status changed successfully - CardId: {}, Action: {}", command.getCardId(), command.getAction());
        return response;
    }

    private String resolveCurrentUsername() {
        // TODO: Integrate with security context
        return "system_user";
    }
}
