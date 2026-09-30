package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.cardCategory.*;
import com.sample.system.card.service.domain.entity.CardCategory;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardCategoryDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardCategoryRepository;
import com.sample.system.card.service.domain.valueObject.CardCategoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardCategoryServiceImplTest {

    @Mock
    private CardCategoryRepository repository;

    private CardCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardCategoryServiceImpl(repository, new CardCategoryDataMapper());
    }

    private static CardCategory category(long id, boolean active) {
        return new CardCategory(new CardCategoryId(id), 10, "Gold", "d", active);
    }

    @Test
    void createSavesAndReturnsEvent() throws CardDomainException {
        when(repository.save(any(CardCategory.class))).thenAnswer(i -> {
            CardCategory c = i.getArgument(0);
            c.setId(new CardCategoryId(1L));
            return c;
        });

        var event = service.createCardCategory(new CreateCardCategoryCommand(1L, "10", "Gold", "d", true));

        assertThat(event.getCardCategory().getId().getValue()).isEqualTo(1L);
        assertThat(event.getCardCategory().getCode()).isEqualTo(10);
    }

    @Test
    void createFailsWhenRepositoryReturnsNull() {
        when(repository.save(any(CardCategory.class))).thenReturn(null);

        assertThatThrownBy(() -> service.createCardCategory(new CreateCardCategoryCommand(1L, "10", "Gold", "d", true)))
                .isInstanceOfSatisfying(CardDomainException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(StatusService.ACTIVE_CARD_CATEGORY_NOT_FOUND);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void updateChangesFieldsAndSaves() throws CardDomainException {
        CardCategory existing = category(1L, true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        var event = service.updateCardCategory(new UpdateCardCategoryCommand(1L, 1L, "10", "Platinum", null, null));

        assertThat(event.getCardCategory().getName()).isEqualTo("Platinum");
    }

    @Test
    void updateOfUnknownCategoryFails() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCardCategory(new UpdateCardCategoryCommand(1L, 1L, "10", "x", null, null)))
                .isInstanceOfSatisfying(CardDomainException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(StatusService.ACTIVE_CARD_CATEGORY_NOT_FOUND));
    }

    @Test
    void activateAndDeactivateToggleTheFlag() throws CardDomainException {
        CardCategory existing = category(2L, false);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.activateCardCategory(new ActivateCardCategoryCommand(2L)).getCardCategory().isActive()).isTrue();
        assertThat(service.deactivateCardCategory(new DeactivateCardCategoryCommand(2L)).getCardCategory().isActive()).isFalse();
    }

    @Test
    void getReturnsCategoryOrFails() throws CardDomainException {
        CardCategory existing = category(3L, true);
        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertThat(service.getCardCategory(new GetCardCategoryQuery(3L))).isSameAs(existing);
        assertThatThrownBy(() -> service.getCardCategory(new GetCardCategoryQuery(4L)))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void getByCodeParsesTheCodeAndFindsCategory() throws CardDomainException {
        CardCategory existing = category(5L, true);
        when(repository.findByCode(10)).thenReturn(Optional.of(existing));

        assertThat(service.getCardCategoryByCode(new GetCardCategoryByCodeQuery("10"))).isSameAs(existing);
    }

    @Test
    void getByUnknownCodeFails() {
        when(repository.findByCode(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCardCategoryByCode(new GetCardCategoryByCodeQuery("99")))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("99");
    }

    @Test
    void getByNonNumericCodeFails() {
        assertThatThrownBy(() -> service.getCardCategoryByCode(new GetCardCategoryByCodeQuery("abc")))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void getAllUsesActiveQueryOnlyWhenRequested() throws CardDomainException {
        when(repository.findActiveCardCategories()).thenReturn(List.of(category(1L, true)));
        when(repository.findAll()).thenReturn(List.of(category(1L, true), category(2L, false)));

        assertThat(service.getAllCardCategories(new GetAllCardCategoriesQuery(true))).hasSize(1);
        assertThat(service.getAllCardCategories(new GetAllCardCategoriesQuery(false))).hasSize(2);
        assertThat(service.getAllCardCategories(new GetAllCardCategoriesQuery(null))).hasSize(2);
    }

    @Test
    void listDelegatesToRepository() throws CardDomainException {
        Map<String, String> params = Map.of("a", "b");
        Page<CardCategory> page = new PageImpl<>(List.of(category(1L, true)));
        when(repository.findAllCategories(params)).thenReturn(page);

        assertThat(service.listCardCategories(params, "me", "ip")).isSameAs(page);
    }

    @Test
    void validateExistsPassesOrFails() throws CardDomainException {
        when(repository.findById(6L)).thenReturn(Optional.of(category(6L, true)));
        when(repository.findById(7L)).thenReturn(Optional.empty());

        service.validateCardCategoryExists(6L);
        assertThatThrownBy(() -> service.validateCardCategoryExists(7L)).isInstanceOf(CardDomainException.class);
    }
}
