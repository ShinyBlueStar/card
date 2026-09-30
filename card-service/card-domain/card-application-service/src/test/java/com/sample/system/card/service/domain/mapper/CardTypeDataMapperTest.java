package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.cardType.CreateCardTypeCommand;
import com.sample.system.card.service.domain.command.cardType.UpdateCardTypeCommand;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.response.cardType.*;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTypeDataMapperTest {

    private final CardTypeDataMapper mapper = new CardTypeDataMapper();

    private static CardType type(long id, Integer code) {
        return new CardType(new CardTypeId(id), code, "Debit", "debit cards", true);
    }

    @Test
    void createCommandIsMappedWithNumericCode() {
        CardType type = mapper.createCommandToCardType(new CreateCardTypeCommand("7", "Debit", "d", true));

        assertThat(type.getId()).isNull();
        assertThat(type.getCode()).isEqualTo(7);
        assertThat(type.getName()).isEqualTo("Debit");
        assertThat(type.getDescription()).isEqualTo("d");
        assertThat(type.getIsActive()).isTrue();
    }

    @Test
    void createCommandWithoutCodeGivesNullCode() {
        assertThat(mapper.createCommandToCardType(new CreateCardTypeCommand(null, "Debit", null, null)).getCode()).isNull();
    }

    @Test
    void createCommandWithNonNumericCodeIsRejected() {
        assertThatThrownBy(() -> mapper.createCommandToCardType(new CreateCardTypeCommand("x1", "Debit", null, true)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("x1");
    }

    @Test
    void typeIsMappedToCreateResponse() {
        CreateCardTypeResponse response = mapper.cardTypeToCreateResponse(type(3L, 7), "created");

        assertThat(response.getCardTypeId()).isEqualTo(3L);
        assertThat(response.getCode()).isEqualTo("7");
        assertThat(response.getName()).isEqualTo("Debit");
        assertThat(response.getDescription()).isEqualTo("debit cards");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getMessage()).isEqualTo("created");
    }

    @Test
    void updateChangesProvidedFieldsOnly() {
        CardType type = type(1L, 7);

        mapper.updateCardTypeFromCommand(type, new UpdateCardTypeCommand(1L, "7", "Credit", "new", false));

        assertThat(type.getName()).isEqualTo("Credit");
        assertThat(type.getDescription()).isEqualTo("new");
        assertThat(type.getIsActive()).isFalse();
    }

    @Test
    void updateIgnoresNullFields() {
        CardType type = type(1L, 7);

        mapper.updateCardTypeFromCommand(type, new UpdateCardTypeCommand(1L, "7", null, null, null));

        assertThat(type.getName()).isEqualTo("Debit");
        assertThat(type.getDescription()).isEqualTo("debit cards");
        assertThat(type.getIsActive()).isTrue();
    }

    @Test
    void typeIsMappedToUpdateResponse() {
        UpdateCardTypeResponse response = mapper.cardTypeToUpdateResponse(type(8L, 1), "m");

        assertThat(response.getCardTypeId()).isEqualTo(8L);
        assertThat(response.getMessage()).isEqualTo("m");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void typeIsMappedToGetResponseWithAndWithoutCode() {
        assertThat(mapper.cardTypeToGetResponse(type(1L, 42)).code()).isEqualTo("42");
        assertThat(mapper.cardTypeToGetResponse(type(2L, null)).code()).isNull();
    }

    @Test
    void listOfTypesIsMapped() {
        assertThat(mapper.cardTypesToGetResponses(List.of(type(1L, 1), type(2L, 2))))
                .extracting(GetCardTypeResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void pageIsMappedWithPagingInfo() {
        PageImpl<CardType> page = new PageImpl<>(List.of(type(1L, 1)), PageRequest.of(0, 1), 3);

        CardTypeListResponse response = mapper.cardTypesToGetListResponse(page);

        assertThat(response.getList()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(1);
    }

    @Test
    void nullPageGivesNull() {
        assertThat(mapper.cardTypesToGetListResponse(null)).isNull();
    }
}
