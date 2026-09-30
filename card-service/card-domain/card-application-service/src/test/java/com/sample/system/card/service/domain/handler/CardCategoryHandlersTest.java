package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.event.cardCategory.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardCategoryCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardCategoryQueryHandler;
import com.sample.system.card.service.domain.mapper.CardCategoryDataMapper;
import com.sample.system.card.service.domain.ports.input.service.CardCategoryService;
import com.sample.system.card.service.domain.response.cardCategory.*;
import com.sample.system.card.service.domain.valueObject.CardCategoryId;
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
class CardCategoryHandlersTest {

    @Mock
    private CardCategoryService service;

    private CardCategoryCommandHandler commandHandler;
    private CardCategoryQueryHandler queryHandler;

    @BeforeEach
    void setUp() {
        CardCategoryDataMapper mapper = new CardCategoryDataMapper();
        commandHandler = new CardCategoryCommandHandler(service, mapper);
        queryHandler = new CardCategoryQueryHandler(service, mapper);
    }

    private static CardCategory category(long id) {
        return new CardCategory(new CardCategoryId(id), 10, "Gold", "d", true);
    }

    @Test
    void createBuildsResponseFromEvent() throws CardDomainException {
        CreateCardCategoryCommand command = new CreateCardCategoryCommand(1L, "10", "Gold", "d", true);
        when(service.createCardCategory(command))
                .thenReturn(new CardCategoryCreatedEvent(category(1L), ZonedDateTime.now()));

        CreateCardCategoryResponse response = commandHandler.createCardCategory(command);

        assertThat(response.getCardCategoryId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("CardCategory created successfully");
    }

    @Test
    void updateBuildsResponseFromEvent() throws CardDomainException {
        UpdateCardCategoryCommand command = new UpdateCardCategoryCommand(2L, 1L, "10", "Gold", "d", true);
        when(service.updateCardCategory(command))
                .thenReturn(new CardCategoryUpdatedEvent(category(2L), ZonedDateTime.now()));

        assertThat(commandHandler.updateCardCategory(command).getMessage())
                .isEqualTo("CardCategory updated successfully");
    }

    @Test
    void activateBuildsResponseFromEvent() throws CardDomainException {
        ActivateCardCategoryCommand command = new ActivateCardCategoryCommand(3L);
        when(service.activateCardCategory(command))
                .thenReturn(new CardCategoryActivatedEvent(category(3L), ZonedDateTime.now()));

        assertThat(commandHandler.activateCardCategory(command).getMessage())
                .isEqualTo("CardCategory activated successfully");
    }

    @Test
    void deactivateBuildsResponseFromEvent() throws CardDomainException {
        DeactivateCardCategoryCommand command = new DeactivateCardCategoryCommand(4L);
        when(service.deactivateCardCategory(command))
                .thenReturn(new CardCategoryDeactivatedEvent(category(4L), ZonedDateTime.now()));

        assertThat(commandHandler.deactivateCardCategory(command).getMessage())
                .isEqualTo("CardCategory deactivated successfully");
    }

    @Test
    void getMapsCategory() throws CardDomainException {
        GetCardCategoryQuery query = new GetCardCategoryQuery(5L);
        when(service.getCardCategory(query)).thenReturn(category(5L));

        assertThat(queryHandler.getCardCategory(query).id()).isEqualTo(5L);
    }

    @Test
    void getByCodeMapsCategory() throws CardDomainException {
        GetCardCategoryByCodeQuery query = new GetCardCategoryByCodeQuery("10");
        when(service.getCardCategoryByCode(query)).thenReturn(category(6L));

        assertThat(queryHandler.getCardCategoryByCode(query).code()).isEqualTo("10");
    }

    @Test
    void getAllMapsEveryCategory() throws CardDomainException {
        GetAllCardCategoriesQuery query = new GetAllCardCategoriesQuery(false);
        when(service.getAllCardCategories(query)).thenReturn(List.of(category(1L), category(2L)));

        assertThat(queryHandler.getAllCardCategories(query)).hasSize(2);
    }

    @Test
    void listMapsPage() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listCardCategories(params, "me", "ip")).thenReturn(new PageImpl<>(List.of(category(1L))));

        assertThat(queryHandler.listCardCategories(params, "me", "ip").getList()).hasSize(1);
    }

    @Test
    void listWithoutPageGivesNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listCardCategories(params, "me", "ip")).thenReturn(null);

        assertThat(queryHandler.listCardCategories(params, "me", "ip")).isNull();
    }
}
