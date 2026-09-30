package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.card.*;
import com.sample.system.card.service.domain.entity.*;
import com.sample.system.card.service.domain.enums.CardStatusAction;
import com.sample.system.card.service.domain.event.card.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardQueryHandler;
import com.sample.system.card.service.domain.mapper.CardDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.response.accessfile.AccessFileResponse;
import com.sample.system.card.service.domain.valueObject.CardId;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardHandlersTest {

    @Mock
    private CardService service;

    private CardCommandHandler commands;
    private CardQueryHandler queries;

    @BeforeEach
    void setUp() {
        CardDataMapper mapper = new CardDataMapper();
        commands = new CardCommandHandler(service, mapper);
        queries = new CardQueryHandler(service, mapper);
    }

    private static Card card(Long id) {
        Card card = Card.builder().pan("6037991200000011")
                .customer(Customer.builder().nationalId("0012345678").firstName("Ali").lastName("Rezaei").build())
                .cardProfile(CardProfile.builder().cardType(new CardType(new CardTypeId(1L), 1, "Gold", "gold", true)).build())
                .issueDate(java.time.LocalDateTime.now()).build();
        if (id != null) {
            card.setId(new CardId(id));
        }
        return card;
    }

    private static ChangeCardStatusCommand change(CardStatusAction action, Long reasonId) {
        return ChangeCardStatusCommand.builder().cardId(1L).action(action).reasonId(reasonId).build();
    }

    @Test
    void blockUsesTheCardIdAndTheBlockingUser() throws CardDomainException {
        when(service.blockCard(any(Card.class), any(), any()))
                .thenReturn(new CardBlockedEvent(card(1L), ZonedDateTime.now()));

        var response = commands.blockCard(BlockCardCommand.builder().cardId(1L).reasonId(5L).blockedBy("op").build());

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(service).blockCard(captor.capture(), any(), any());
        assertThat(captor.getValue().getId().getValue()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("Card blocked successfully!");
        assertThat(response.getCardId()).isEqualTo(1L);
    }

    @Test
    void activateAndDeactivateReturnMessages() throws CardDomainException {
        when(service.activateCard(1L, 5L)).thenReturn(new CardActivatedEvent(card(1L), ZonedDateTime.now(), "op"));
        when(service.deactivateCard(2L, 6L, "op")).thenReturn(new CardDeactivatedEvent(card(2L), ZonedDateTime.now(), "op", "r"));

        assertThat(commands.activateCard(ActivateCardCommand.builder().cardId(1L).ReasonId(5L).build()).getMessage())
                .isEqualTo("Card activated successfully!");
        assertThat(commands.deactivateCard(DeactivateCardCommand.builder().cardId(2L).reasonId(6L).deactivatedBy("op").build())
                .getMessage()).isEqualTo("Card deactivated successfully!");
    }

    @Test
    void changeStatusDispatchesToTheMatchingOperation() throws CardDomainException {
        when(service.activateCard(1L, null)).thenReturn(new CardActivatedEvent(card(1L), ZonedDateTime.now(), null));
        when(service.deactivateCard(1L, 5L, "system_user")).thenReturn(new CardDeactivatedEvent(card(1L), ZonedDateTime.now(), "system_user", "r"));
        when(service.blockCard(any(Card.class), any(), any())).thenReturn(new CardBlockedEvent(card(1L), ZonedDateTime.now()));
        when(service.unblockCard(any(Card.class), any(), any())).thenReturn(new CardUnblockedEvent(card(1L)));

        assertThat(commands.changeStatus(change(CardStatusAction.ACTIVATE, null)).getMessage()).isEqualTo("Card activated successfully!");
        assertThat(commands.changeStatus(change(CardStatusAction.DEACTIVATE, 5L)).getMessage()).isEqualTo("Card deactivated successfully!");
        assertThat(commands.changeStatus(change(CardStatusAction.BLOCK, 5L)).getMessage()).isEqualTo("Card blocked successfully!");
        assertThat(commands.changeStatus(change(CardStatusAction.UNBLOCK, 5L)).getMessage()).isEqualTo("Card unBlocked successfully!");
    }

    @Test
    void changeStatusRequiresAReasonForDeactivateBlockAndUnblock() {
        for (CardStatusAction action : List.of(CardStatusAction.DEACTIVATE, CardStatusAction.BLOCK, CardStatusAction.UNBLOCK)) {
            assertThatThrownBy(() -> commands.changeStatus(change(action, null)))
                    .isInstanceOfSatisfying(CardDomainException.class, e -> {
                        assertThat(e.getStatus()).isEqualTo(StatusService.REASON_IS_MANDATORY);
                        assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }

    @Test
    void queriesMapServiceResults() throws CardDomainException {
        GetAllCardsQuery all = GetAllCardsQuery.builder().activeOnly(true).customerNumber("1").build();
        Map<String, String> params = Map.of();
        when(service.getCard(1L)).thenReturn(card(1L));
        when(service.getAllCards(all)).thenReturn(List.of(card(1L), card(2L)));
        when(service.listCards(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(card(1L)))).thenReturn(null);
        when(service.generateAccessFile()).thenReturn(AccessFileResponse.builder().fileName("a.csv").build());

        assertThat(queries.getCard(1L).cardId()).isEqualTo(1L);
        assertThat(queries.getAllCards(all)).hasSize(2);
        assertThat(queries.listCards(params, "me", "ip").getList()).hasSize(1);
        assertThat(queries.listCards(params, "me", "ip")).isNull();
        assertThat(queries.generateAccessFile().getFileName()).isEqualTo("a.csv");
    }
}
