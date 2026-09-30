package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.CardStatus;
import com.sample.system.card.service.domain.response.card.CardListResponse;
import com.sample.system.card.service.domain.response.card.GetCardResponse;
import com.sample.system.card.service.domain.response.card.UpdateCardResponse;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardDataMapperTest {

    private final CardDataMapper mapper = new CardDataMapper();

    private static Card card(Long id) {
        CardProfile profile = CardProfile.builder()
                .cardType(new CardType(new CardTypeId(1L), 7, "Gold", "gold type", true)).build();
        Customer customer = Customer.builder().nationalId("0012345678").firstName("Ali").lastName("Rezaei").address("Tehran").build();
        Card card = Card.builder().pan("6037991200000011").issueDate(LocalDateTime.of(2024, 3, 20, 10, 0))
                .expDate("1405-03-01").customer(customer).cardProfile(profile).cardStatus(CardStatus.ACTIVE)
                .issuingBank("Melli").unitName("Unit").unitCode("U1").caseNumber("C-1").isVirtualCard(false)
                .createdBy("ali").createdDate(Timestamp.valueOf("2024-03-20 10:15:30"))
                .lastModifiedBy("reza").lastModifiedDate(Timestamp.valueOf("2024-04-21 11:00:00")).build();
        if (id != null) {
            card.setId(new CardId(id));
        }
        return card;
    }

    @Test
    void cardIsMappedToGetResponse() {
        GetCardResponse response = mapper.cardToGetCardResponse(card(5L));

        assertThat(response.cardId()).isEqualTo(5L);
        assertThat(response.pan()).isEqualTo("6037991200000011");
        assertThat(response.issueDate()).isNotBlank().isNotEqualTo("null");
        assertThat(response.expirationDate()).isEqualTo("1405-03-01");
        assertThat(response.cardHolderName()).isEqualTo("Ali");
        assertThat(response.cardHolderFamily()).isEqualTo("Rezaei");
        assertThat(response.address()).isEqualTo("Tehran");
        assertThat(response.cardStatus()).isEqualTo(CardStatus.ACTIVE.getDescription());
        assertThat(response.cardType()).isEqualTo("gold type");
        assertThat(response.issuingBank()).isEqualTo("Melli");
        assertThat(response.unitName()).isEqualTo("Unit");
        assertThat(response.unitCode()).isEqualTo("U1");
        assertThat(response.nationalId()).isEqualTo("0012345678");
        assertThat(response.caseNumber()).isEqualTo("C-1");
        assertThat(response.virtualCard()).isFalse();
        assertThat(response.createdBy()).isEqualTo("ali");
        assertThat(response.createdDate()).isNotBlank();
        assertThat(response.lastModifiedBy()).isEqualTo("reza");
        assertThat(response.lastModifiedDate()).isNotBlank();
    }

    @Test
    void cardWithoutIdStatusAndDatesIsMappedWithNulls() {
        Card card = card(null);
        card.setCardStatus(null);
        card.setCreatedDate(null);
        card.setLastModifiedDate(null);

        GetCardResponse response = mapper.cardToGetCardResponse(card);

        assertThat(response.cardId()).isNull();
        assertThat(response.cardStatus()).isNull();
        assertThat(response.createdDate()).isNull();
        assertThat(response.lastModifiedDate()).isNull();
    }

    @Test
    void cardWithoutIssueDateCurrentlyReportsTheTextNull() {
        Card card = card(1L);
        card.setIssueDate(null);

        // String.valueOf(null) is fed to the plain-date fallback, which cannot parse it and returns it unchanged
        assertThat(mapper.cardToGetCardResponse(card).issueDate()).isEqualTo("null");
    }

    @Test
    void nullCardIsMappedToNull() {
        assertThat(mapper.cardToGetCardResponse(null)).isNull();
        assertThat(mapper.cardToUpdateCardResponse(null, "m")).isNull();
        assertThat(mapper.cardsToGetResponses(null)).isNull();
        assertThat(mapper.cardsToGetListResponse(null)).isNull();
    }

    @Test
    void cardListsAndPagesAreMapped() {
        assertThat(mapper.cardsToGetResponses(List.of(card(1L), card(2L)))).hasSize(2);

        CardListResponse page = mapper.cardsToGetListResponse(new PageImpl<>(List.of(card(1L)), PageRequest.of(1, 1), 3));
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void updateResponseCarriesIdAndMessageOrTheDefaultMessage() {
        UpdateCardResponse custom = mapper.cardToUpdateCardResponse(card(4L), "blocked");
        UpdateCardResponse defaulted = mapper.cardToUpdateCardResponse(card(null), null);

        assertThat(custom.getCardId()).isEqualTo(4L);
        assertThat(custom.getMessage()).isEqualTo("blocked");
        assertThat(defaulted.getCardId()).isNull();
        assertThat(defaulted.getMessage()).isEqualTo("Card updated successfully");
    }

    @Test
    void cardIdIsWrappedIntoACard() {
        assertThat(mapper.toCard(9L).getId().getValue()).isEqualTo(9L);
        assertThat(mapper.toCard(null).getId()).isNull();
    }

    @Test
    void plainDatesAreConvertedByTheFallbackOnlyWhenTheyCanBeParsed() throws Exception {
        var method = CardDataMapper.class.getDeclaredMethod("convertPlainDateToPersian", String.class);
        method.setAccessible(true);

        assertThat(method.invoke(mapper, (Object) null)).isNull();
        assertThat(method.invoke(mapper, " ")).isNull();
        assertThat((String) method.invoke(mapper, "2024-03-20")).isNotBlank().isNotEqualTo("2024-03-20");
        assertThat((String) method.invoke(mapper, "2024-03-20T10:15:30")).isNotBlank().isNotEqualTo("2024-03-20T10:15:30");
        assertThat(method.invoke(mapper, "garbage")).isEqualTo("garbage");
    }
}
