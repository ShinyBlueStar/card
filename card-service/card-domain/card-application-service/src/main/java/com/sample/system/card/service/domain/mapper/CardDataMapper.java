package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.entity.Card;
import com.sample.system.card.service.domain.response.card.CardListResponse;
import com.sample.system.card.service.domain.response.card.GetCardResponse;
import com.sample.system.card.service.domain.response.card.UpdateCardResponse;
import com.sample.system.card.service.domain.utility.date.DateUtils;
import com.sample.system.card.service.domain.valueObject.CardId;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Component
public class CardDataMapper {

    public List<GetCardResponse> cardsToGetResponses(List<Card> cards) {
        if (cards == null) return null;
        return cards.stream()
                .map(this::cardToGetCardResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convert Page of Cards to CardListResponse
     */
    public CardListResponse cardsToGetListResponse(org.springframework.data.domain.Page<Card> cards) {
        if (cards == null) return null;

        CardListResponse cardListResponse = new CardListResponse();
        cardListResponse.setList(cards.getContent().stream()
                .map(this::cardToGetCardResponse)
                .toList());
        cardListResponse.setNumber(cards.getNumber());
        cardListResponse.setSize(cards.getSize());
        cardListResponse.setTotalElements(cards.getTotalElements());
        cardListResponse.setTotalPages(cards.getTotalPages());
        return cardListResponse;
    }

    public GetCardResponse cardToGetCardResponse(Card card){
        if (card == null) {
            return null;
        }

        String issueDatePersian = Optional.ofNullable(card.getIssueDate())
                .map(Timestamp::valueOf)
                .map(this::convertToPersianDate)
                .orElseGet(() -> convertPlainDateToPersian(String.valueOf(card.getIssueDate())));

        String createdDatePersian = convertToPersianDate(card.getCreatedDate());
        String lastModifiedDatePersian = convertToPersianDate(card.getLastModifiedDate());

        return new GetCardResponse(
                card.getId() != null ? card.getId().getValue() : null,
                card.getPan(),
                issueDatePersian,
                card.getExpDate(),
                card.getCustomer().getFirstName(),
                card.getCustomer().getLastName(),
                card.getCustomer().getAddress(),
                card.getCardStatus() != null ? card.getCardStatus().getDescription() : null,
                card.getCardProfile().getCardType().getDescription(),
                card.getIssuingBank(),
                card.getUnitName(),
                card.getUnitCode(),
                card.getCustomer().getNationalId(),
                card.getCaseNumber(),
                card.getIsVirtualCard(),
                card.getCreatedBy(),
                createdDatePersian,
                card.getLastModifiedBy(),
                lastModifiedDatePersian
        );
    }

    /**
     * Convert Card to UpdateCardResponse
     */
    public UpdateCardResponse cardToUpdateCardResponse(Card card, String message) {
        if (card == null) return null;
        return UpdateCardResponse.builder()
                .cardId(card.getId() != null ? card.getId().getValue() : null)
                .message(message != null ? message : "Card updated successfully")
                .build();
    }

    public Card toCard(Long cardId) {
        Card card = Card.builder().build();
        if (cardId != null) {
            card.setId(new CardId(cardId));
        }
        return card;
    }

    private String convertToPersianDate(Timestamp date) {
        return Optional.ofNullable(date)
                .map(ts -> DateUtils.getLocaleDate(
                        DateUtils.FARSI_LOCALE,
                        ts,
                        DateUtils.PERSIAN_DATE_FORMAT,
                        false))
                .orElse(null);
    }

    private String convertPlainDateToPersian(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return convertToPersianDate(Timestamp.valueOf(date.atStartOfDay()));
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateText, DateTimeFormatter.ISO_DATE_TIME);
                return convertToPersianDate(Timestamp.valueOf(dateTime));
            } catch (DateTimeParseException ignored) {
                return dateText;
            }
        }
    }
}
