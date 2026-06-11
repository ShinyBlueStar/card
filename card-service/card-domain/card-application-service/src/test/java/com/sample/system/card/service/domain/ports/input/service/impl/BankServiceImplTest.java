package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.bank.ActivateBankCommand;
import com.sample.system.card.service.domain.command.bank.CreateBankCommand;
import com.sample.system.card.service.domain.command.bank.GetBankByBinCodeQuery;
import com.sample.system.card.service.domain.command.bank.UpdateBankCommand;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.event.bank.BankCreatedEvent;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.BankDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.BankRepository;
import com.sample.system.card.service.domain.valueObject.BankId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {

    @Mock
    private BankRepository repository;

    private BankServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BankServiceImpl(repository, new BankDataMapper());
    }

    private static Bank bank(long id, boolean active) {
        Bank bank = Bank.builder().binCode("603799").name("Melli").isActive(active).build();
        bank.setId(new BankId(id));
        return bank;
    }

    @Test
    void createBankSavesMappedBankAndReturnsEvent() throws CardDomainException {
        when(repository.save(any(Bank.class))).thenAnswer(invocation -> {
            Bank toSave = invocation.getArgument(0);
            toSave.setId(new BankId(5L));
            return toSave;
        });

        BankCreatedEvent event = service.createBank(new CreateBankCommand("603799", "Melli", true));

        ArgumentCaptor<Bank> saved = ArgumentCaptor.forClass(Bank.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getBinCode()).isEqualTo("603799");
        assertThat(saved.getValue().getName()).isEqualTo("Melli");
        assertThat(event.getBank().getId().getValue()).isEqualTo(5L);
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    void createBankFailsWhenRepositoryReturnsNull() {
        when(repository.save(any(Bank.class))).thenReturn(null);

        assertThatThrownBy(() -> service.createBank(new CreateBankCommand("603799", "Melli", true)))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void updateBankChangesOnlyProvidedFields() throws CardDomainException {
        Bank existing = bank(1L, true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.updateBank(new UpdateBankCommand(1L, "610433", "Mellat", null));

        assertThat(existing.getBinCode()).isEqualTo("610433");
        assertThat(existing.getName()).isEqualTo("Mellat");
        assertThat(existing.getIsActive()).isTrue();
    }

    @Test
    void updateBankFailsForUnknownId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBank(new UpdateBankCommand(99L, "610433", "Mellat", true)))
                .isInstanceOf(CardDomainException.class)
                .hasMessageContaining("99");
        verify(repository, never()).save(any());
    }

    @Test
    void activateBankSetsActiveFlag() throws CardDomainException {
        Bank existing = bank(1L, false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.activateBank(new ActivateBankCommand(1L));

        assertThat(existing.getIsActive()).isTrue();
    }

    @Test
    void findByIdReturnsBadRequestWithStatusCodeWhenMissing() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(7L))
                .isInstanceOfSatisfying(CardDomainException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(StatusService.ACTIVE_BANK_NOT_FOUND);
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void getBankByBinCodeDelegatesToRepository() throws CardDomainException {
        Bank existing = bank(1L, true);
        when(repository.findByBinCode("603799")).thenReturn(Optional.of(existing));

        assertThat(service.getBankByBinCode(new GetBankByBinCodeQuery("603799"))).isSameAs(existing);
    }
}
