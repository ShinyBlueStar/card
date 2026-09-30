package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.event.cardType.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardTypeCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardTypeQueryHandler;
import com.sample.system.card.service.domain.mapper.CardTypeDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardTypeService;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardTypeHandlersTest {

    @Mock
    private CardTypeService service;

    private CardTypeCommandHandler commandHandler;
    private CardTypeQueryHandler queryHandler;

    @BeforeEach
    void setUp() {
        CardTypeDataMapper mapper = new CardTypeDataMapper();
        commandHandler = new CardTypeCommandHandler(service, mapper);
        queryHandler = new CardTypeQueryHandler(service, mapper);
    }

    private static CardType type(long id) {
        return new CardType(new CardTypeId(id), 7, "Debit", "d", true);
    }

    @Test
    void createBuildsResponseFromEvent() throws CardDomainException {
        CreateCardTypeCommand command = new CreateCardTypeCommand("7", "Debit", "d", true);
        when(service.createCardType(command)).thenReturn(new CardTypeCreatedEvent(type(1L), ZonedDateTime.now()));

        assertThat(commandHandler.createCardType(command).getMessage()).isEqualTo("CardType created successfully");
    }

    @Test
    void updateBuildsResponseFromEvent() throws CardDomainException {
        UpdateCardTypeCommand command = new UpdateCardTypeCommand(2L, "7", "Debit", "d", true);
        when(service.updateCardType(command)).thenReturn(new CardTypeUpdatedEvent(type(2L), ZonedDateTime.now()));

        assertThat(commandHandler.updateCardType(command).getMessage()).isEqualTo("CardType updated successfully");
    }

    @Test
    void activateBuildsResponseFromEvent() throws CardDomainException {
        ActivateCardTypeCommand command = new ActivateCardTypeCommand(3L);
        when(service.activateCardType(command)).thenReturn(new CardTypeActivatedEvent(type(3L), ZonedDateTime.now()));

        assertThat(commandHandler.activateCardType(command).getMessage()).isEqualTo("CardType activated successfully");
    }

    @Test
    void deactivateBuildsResponseFromEvent() throws CardDomainException {
        DeactivateCardTypeCommand command = new DeactivateCardTypeCommand(4L);
        when(service.deactivateCardType(command)).thenReturn(new CardTypeDeactivatedEvent(type(4L), ZonedDateTime.now()));

        assertThat(commandHandler.deactivateCardType(command).getMessage()).isEqualTo("CardType deactivated successfully");
    }

    @Test
    void getMapsType() throws CardDomainException {
        GetCardTypeQuery query = new GetCardTypeQuery(5L);
        when(service.getCardType(query)).thenReturn(type(5L));

        assertThat(queryHandler.getCardType(query).id()).isEqualTo(5L);
    }

    @Test
    void getByCodeMapsType() throws CardDomainException {
        GetCardTypeByCodeQuery query = new GetCardTypeByCodeQuery("7");
        when(service.getCardTypeByCode(query)).thenReturn(type(6L));

        assertThat(queryHandler.getCardTypeByCode(query).code()).isEqualTo("7");
    }

    @Test
    void getAllMapsEveryType() throws CardDomainException {
        GetAllCardTypesQuery query = new GetAllCardTypesQuery();
        when(service.getAllCardTypes(query)).thenReturn(List.of(type(1L), type(2L)));

        assertThat(queryHandler.getAllCardTypes(query)).hasSize(2);
    }

    @Test
    void listMapsPage() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listCardTypes(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(type(1L))));

        assertThat(queryHandler.listCardTypes(params, "me", "ip").getList()).hasSize(1);
    }

    @Test
    void listWithoutPageGivesNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listCardTypes(params, "me", "ip")).thenReturn(null);

        assertThat(queryHandler.listCardTypes(params, "me", "ip")).isNull();
    }
}
