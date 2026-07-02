package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.response.cardType.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardTypeDataMapper {

    public CardType createCommandToCardType(CreateCardTypeCommand createCardTypeCommand) {
        return new CardType(null,
                parseCode(createCardTypeCommand.getCode()),
                createCardTypeCommand.getName(),
                createCardTypeCommand.getDescription(),
                createCardTypeCommand.getIsActive());
    }

    private Integer parseCode(String code) {
        if (code == null) return null;
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid code format: " + code);
        }
    }

    public CreateCardTypeResponse cardTypeToCreateResponse(CardType cardType, String message) {
        return CreateCardTypeResponse.builder()
                .cardTypeId(cardType.getId().getValue())
                .code(cardType.getCode().toString())
                .name(cardType.getName())
                .description(cardType.getDescription())
                .isActive(cardType.getIsActive())
                .message(message)
                .build();
    }

    public void updateCardTypeFromCommand(CardType cardType, UpdateCardTypeCommand command) {
        if (command.getName() != null) {
            cardType.setName(command.getName());
        }
        if (command.getDescription() != null) {
            cardType.setDescription(command.getDescription());
        }
        if (command.getIsActive() != null) {
            cardType.setIsActive(command.getIsActive());
        }
    }

    public UpdateCardTypeResponse cardTypeToUpdateResponse(CardType cardType, String message) {
        return UpdateCardTypeResponse.builder()
                .cardTypeId(cardType.getId().getValue())
                .message(message)
                .isActive(cardType.getIsActive())
                .build();
    }

    public GetCardTypeResponse cardTypeToGetResponse(CardType cardType) {
        return new GetCardTypeResponse(
                cardType.getId().getValue(),
                cardType.getCode() != null ? cardType.getCode().toString() : null,
                cardType.getName(),
                cardType.getDescription(),
                cardType.getIsActive());
    }

    public List<GetCardTypeResponse> cardTypesToGetResponses(List<CardType> cardTypes) {
        return cardTypes.stream()
                .map(this::cardTypeToGetResponse)
                .toList();
    }

    public CardTypeListResponse cardTypesToGetListResponse(Page<CardType> cardTypes) {
        if (cardTypes == null) return null;

        CardTypeListResponse cardTypeListResponse = new CardTypeListResponse();
        cardTypeListResponse.setList(cardTypes.getContent().stream()
                .map(this::cardTypeToGetResponse).toList());
        cardTypeListResponse.setNumber(cardTypes.getNumber());
        cardTypeListResponse.setSize(cardTypes.getSize());
        cardTypeListResponse.setTotalElements(cardTypes.getTotalElements());
        cardTypeListResponse.setTotalPages(cardTypes.getTotalPages());
        return cardTypeListResponse;
    }
}
