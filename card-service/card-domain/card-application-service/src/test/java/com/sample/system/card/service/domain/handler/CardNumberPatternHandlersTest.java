package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.numberPattern.*;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.event.cardNumberPattern.CardNumberPatternCreatedEvent;
import com.sample.system.card.service.domain.event.cardNumberPattern.CardNumberPatternUpdatedEvent;
import com.sample.system.card.service.domain.event.cardNumberPattern.CardNumberPatternsFetchedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardNumberPatternCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardNumberPatternQueryHandler;
import com.sample.system.card.service.domain.mapper.CardNumberPatternDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardNumberPatternService;
import com.sample.system.card.service.domain.valueObject.CardNumberPatternId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardNumberPatternHandlersTest {

    @Mock
    private CardNumberPatternService service;

    private CardNumberPatternCommandHandler commands;
    private CardNumberPatternQueryHandler queries;

    @BeforeEach
    void setUp() {
        CardNumberPatternDataMapper mapper = new CardNumberPatternDataMapper();
        commands = new CardNumberPatternCommandHandler(service, mapper);
        queries = new CardNumberPatternQueryHandler(mapper, service);
    }

    private static CardNumberPattern pattern(long id) {
        CardNumberPattern pattern = CardNumberPattern.builder().cardNumberFrom("1000000").cardNumberTo("1999999").build();
        pattern.setId(new CardNumberPatternId(id));
        return pattern;
    }

    @Test
    void createMapsCommandAndReturnsSuccess() throws CardDomainException {
        when(service.createCardNumberPattern(any(CardNumberPattern.class))).thenReturn(new CardNumberPatternCreatedEvent(pattern(1L)));

        var response = commands.createCardNumberPattern(
                CreateCardNumberPatternCommand.builder().cardNumberFrom("1000000").cardNumberTo("1999999").productCode("P").build());

        assertThat(response.getCardNumberPatternId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("success");
    }

    @Test
    void updateAndDeactivateMapCommands() throws CardDomainException {
        when(service.updateCardNumberPattern(any(CardNumberPattern.class))).thenReturn(new CardNumberPatternUpdatedEvent(pattern(2L)));
        when(service.deactivateCardNumberPattern(any(CardNumberPattern.class))).thenReturn(new CardNumberPatternUpdatedEvent(pattern(3L)));

        assertThat(commands.updateCardNumberPattern(UpdateCardNumberPatternCommand.builder().patternId(2L).build()).getMessage())
                .isEqualTo("Card Number Pattern updated successfully!");
        assertThat(commands.deactivateCardNumberPattern(DeactivateCardNumberPatternCommand.builder().patternId(3L).build())
                .getCardNumberPatternId()).isEqualTo(3L);
    }

    @Test
    void bulkCreateReturnsOneResponsePerSavedPattern() throws CardDomainException {
        when(service.createCardNumberPatterns(any())).thenReturn(new CardNumberPatternsFetchedEvent(List.of(pattern(1L), pattern(2L))));

        var responses = commands.createCardNumberPatterns(List.of(
                CreateCardNumberPatternCommand.builder().productCode("A").build(),
                CreateCardNumberPatternCommand.builder().productCode("B").build()));

        assertThat(responses).extracting(r -> r.getCardNumberPatternId()).containsExactly(1L, 2L);
        assertThat(responses.get(0).getMessage()).isEqualTo("Card Number Pattern created successfully!");
    }

    @Test
    void queriesMapServiceResults() throws CardDomainException {
        GetCardNumberPatternQuery get = new GetCardNumberPatternQuery(4L);
        GetAllCardNumberPatternsQuery all = new GetAllCardNumberPatternsQuery("ACTIVE", 1L, "x", "y", "z");
        Map<String, String> params = Map.of();
        when(service.getCardNumberPattern(get)).thenReturn(pattern(4L));
        when(service.getAllCardNumberPatterns(all)).thenReturn(List.of(pattern(1L), pattern(2L)));
        when(service.listCardNumberPatterns(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(pattern(1L))));
        when(service.getPatternsContainingCardNumber("1500000")).thenReturn(List.of(pattern(1L)));

        assertThat(queries.getCardNumberPattern(get).id()).isEqualTo(4L);
        assertThat(queries.getAllCardNumberPatterns(all)).hasSize(2);
        assertThat(queries.listCardNumberPatterns(params, "me", "ip").getList()).hasSize(1);
        assertThat(queries.getPatternsContainingCardNumber("1500000")).hasSize(1);
    }

    @Test
    void listWithoutPageGivesNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listCardNumberPatterns(params, "me", "ip")).thenReturn(null);

        assertThat(queries.listCardNumberPatterns(params, "me", "ip")).isNull();
    }
}
