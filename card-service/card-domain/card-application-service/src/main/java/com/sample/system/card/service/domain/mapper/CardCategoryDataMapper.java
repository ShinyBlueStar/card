package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.response.cardCategory.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardCategoryDataMapper {

    public CardCategory createCommandToCardCategory(CreateCardCategoryCommand createCardCategoryCommand) {
        return new CardCategory(null,
                parseCode(createCardCategoryCommand.getCode()),
                createCardCategoryCommand.getName(),
                createCardCategoryCommand.getDescription(),
                createCardCategoryCommand.getIsActive());
    }

    private Integer parseCode(String code) {
        if (code == null) return null;
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid code format: " + code);
        }
    }

    public CreateCardCategoryResponse cardCategoryToCreateResponse(CardCategory cardCategory, String message) {
        return CreateCardCategoryResponse.builder()
                .cardCategoryId(cardCategory.getId().getValue())
                .cardTypeId(null) // Set if needed
                .code(cardCategory.getCode().toString())
                .name(cardCategory.getName())
                .description(cardCategory.getDescription())
                .isActive(cardCategory.getIsActive())
                .message(message)
                .build();
    }

    public void updateCardCategoryFromCommand(CardCategory cardCategory, UpdateCardCategoryCommand command) {
        if (command.getName() != null) {
            cardCategory.setName(command.getName());
        }
        if (command.getDescription() != null) {
            cardCategory.setDescription(command.getDescription());
        }
        if (command.getIsActive() != null) {
            cardCategory.setIsActive(command.getIsActive());
        }
    }

    public UpdateCardCategoryResponse cardCategoryToUpdateResponse(CardCategory cardCategory, String message) {
        return UpdateCardCategoryResponse.builder()
                .cardCategoryId(cardCategory.getId().getValue())
                .message(message)
                .isActive(cardCategory.getIsActive())
                .build();
    }

    public GetCardCategoryResponse cardCategoryToGetResponse(CardCategory cardCategory) {
        return new GetCardCategoryResponse(
                cardCategory.getId().getValue(),
                cardCategory.getCode() != null ? cardCategory.getCode().toString() : null,
                cardCategory.getName(),
                cardCategory.getDescription(),
                cardCategory.getIsActive());
    }

    public List<GetCardCategoryResponse> cardCategoriesToGetResponses(List<CardCategory> cardCategories) {
        return cardCategories.stream()
                .map(this::cardCategoryToGetResponse)
                .toList();
    }

    public CardCategoryListResponse cardCategoriesToGetListResponse(Page<CardCategory> cardCategories) {
        if (cardCategories == null) return null;

        CardCategoryListResponse cardCategoryListResponse = new CardCategoryListResponse();
        cardCategoryListResponse.setList(cardCategories.getContent().stream()
                .map(this::cardCategoryToGetResponse).toList());
        cardCategoryListResponse.setNumber(cardCategories.getNumber());
        cardCategoryListResponse.setSize(cardCategories.getSize());
        cardCategoryListResponse.setTotalElements(cardCategories.getTotalElements());
        cardCategoryListResponse.setTotalPages(cardCategories.getTotalPages());
        return cardCategoryListResponse;
    }
}
