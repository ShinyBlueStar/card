package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.cardType.*;
import com.sample.system.card.service.domain.entity.CardType;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.CardTypeDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.CardTypeRepository;
import com.sample.system.card.service.domain.valueObject.CardTypeId;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardTypeServiceImplTest {

    @Mock
    private CardTypeRepository repository;

    private CardTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardTypeServiceImpl(repository, new CardTypeDataMapper());
    }

    private static CardType type(long id, boolean active) {
        return new CardType(new CardTypeId(id), 7, "Debit", "d", active);
    }

    @Test
    void createSavesAndReturnsEvent() throws CardDomainException {
        when(repository.save(any(CardType.class))).thenAnswer(i -> {
            CardType t = i.getArgument(0);
            t.setId(new CardTypeId(1L));
            return t;
        });

        var event = service.createCardType(new CreateCardTypeCommand("7", "Debit", "d", true));

        assertThat(event.getCardType().getId().getValue()).isEqualTo(1L);
    }

    @Test
    void createFailsWhenRepositoryReturnsNull() {
        when(repository.save(any(CardType.class))).thenReturn(null);

        assertThatThrownBy(() -> service.createCardType(new CreateCardTypeCommand("7", "Debit", "d", true)))
                .isInstanceOfSatisfying(CardDomainException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(StatusService.ERROR_IN_SAVING_CARD);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.OK);
                });
    }

    @Test
    void updateChangesFieldsAndSaves() throws CardDomainException {
        CardType existing = type(1L, true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        var event = service.updateCardType(new UpdateCardTypeCommand(1L, "7", "Credit", null, null));

        assertThat(event.getCardType().getName()).isEqualTo("Credit");
    }

    @Test
    void updateOfUnknownTypeReportsActiveCardTypeNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCardType(new UpdateCardTypeCommand(1L, "7", "x", null, null)))
                .isInstanceOfSatisfying(CardDomainException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(StatusService.ACTIVE_CARD_TYPE_NOT_FOUND);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void activateAndDeactivateToggleTheFlag() throws CardDomainException {
        CardType existing = type(2L, false);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.activateCardType(new ActivateCardTypeCommand(2L)).getCardType().isActive()).isTrue();
        assertThat(service.deactivateCardType(new DeactivateCardTypeCommand(2L)).getCardType().isActive()).isFalse();
    }

    @Test
    void activateAndDeactivateOfUnknownTypeFail() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateCardType(new ActivateCardTypeCommand(9L)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("9");
        assertThatThrownBy(() -> service.deactivateCardType(new DeactivateCardTypeCommand(9L)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("9");
    }

    @Test
    void getReturnsTypeOrFails() throws CardDomainException {
        CardType existing = type(3L, true);
        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertThat(service.getCardType(new GetCardTypeQuery(3L))).isSameAs(existing);
        assertThatThrownBy(() -> service.getCardType(new GetCardTypeQuery(4L))).isInstanceOf(CardDomainException.class);
    }

    @Test
    void getByCodeFindsTypeOrFails() throws CardDomainException {
        CardType existing = type(5L, true);
        when(repository.findByCode(7)).thenReturn(Optional.of(existing));
        when(repository.findByCode(8)).thenReturn(Optional.empty());

        assertThat(service.getCardTypeByCode(new GetCardTypeByCodeQuery("7"))).isSameAs(existing);
        assertThatThrownBy(() -> service.getCardTypeByCode(new GetCardTypeByCodeQuery("8")))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("8");
        assertThatThrownBy(() -> service.getCardTypeByCode(new GetCardTypeByCodeQuery("zz")))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void getAllUsesActiveQueryOnlyWhenRequested() throws CardDomainException {
        when(repository.findActiveCardTypes()).thenReturn(List.of(type(1L, true)));
        when(repository.findAll()).thenReturn(List.of(type(1L, true), type(2L, false)));

        assertThat(service.getAllCardTypes(new GetAllCardTypesQuery(true))).hasSize(1);
        assertThat(service.getAllCardTypes(new GetAllCardTypesQuery(false))).hasSize(2);
        assertThat(service.getAllCardTypes(new GetAllCardTypesQuery(null))).hasSize(2);
    }

    @Test
    void listDelegatesToRepository() throws CardDomainException {
        Map<String, String> params = Map.of("a", "b");
        Page<CardType> page = new PageImpl<>(List.of(type(1L, true)));
        when(repository.findAllTypes(params)).thenReturn(page);

        assertThat(service.listCardTypes(params, "me", "ip")).isSameAs(page);
    }

    @Test
    void validateExistsPassesOrFails() throws CardDomainException {
        when(repository.findById(6L)).thenReturn(Optional.of(type(6L, true)));
        when(repository.findById(7L)).thenReturn(Optional.empty());

        service.validateCardTypeExists(6L);
        assertThatThrownBy(() -> service.validateCardTypeExists(7L)).isInstanceOf(CardDomainException.class);
    }

    @Test
    void cardTypeActiveFlagFollowsActivateAndDeactivate() {
        CardType type = new CardType();
        assertThat(type.isActive()).isFalse();
        type.activate();
        assertThat(type.isActive()).isTrue();
        type.deactivate();
        assertThat(type.isActive()).isFalse();
        type.setCode(9);
        assertThat(type.getCode()).isEqualTo(9);
    }
}
