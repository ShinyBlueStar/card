package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.BankDataMapper;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.BankRepository;
import com.sample.system.card.service.domain.valueObject.BankId;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceImplScenariosTest {

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
    void updateOfUnknownBankFails() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBank(new UpdateBankCommand(1L, "603799", "Melli", true)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("Bank not found: 1");
        verify(repository, never()).save(any());
    }

    @Test
    void activateSetsBankActiveAndSaves() throws CardDomainException {
        Bank bank = bank(2L, false);
        when(repository.findById(2L)).thenReturn(Optional.of(bank));
        when(repository.save(bank)).thenReturn(bank);

        assertThat(service.activateBank(new ActivateBankCommand(2L)).getBank().isActive()).isTrue();
    }

    @Test
    void activateOfUnknownBankFails() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateBank(new ActivateBankCommand(2L)))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void deactivateSetsBankInactiveAndSaves() throws CardDomainException {
        Bank bank = bank(3L, true);
        when(repository.findById(3L)).thenReturn(Optional.of(bank));
        when(repository.save(bank)).thenReturn(bank);

        assertThat(service.deactivateBank(new DeactivateBankCommand(3L)).getBank().isActive()).isFalse();
    }

    @Test
    void deactivateOfUnknownBankFails() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivateBank(new DeactivateBankCommand(3L)))
                .isInstanceOf(CardDomainException.class);
    }

    @Test
    void getBankReturnsBank() throws CardDomainException {
        Bank bank = bank(4L, true);
        when(repository.findById(4L)).thenReturn(Optional.of(bank));

        assertThat(service.getBank(new GetBankQuery(4L))).isSameAs(bank);
    }

    @Test
    void getBankOfUnknownIdFails() {
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBank(new GetBankQuery(4L)))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("Bank not found with id: 4");
    }

    @Test
    void getBankByBinCodeReturnsBank() throws CardDomainException {
        Bank bank = bank(5L, true);
        when(repository.findByBinCode("603799")).thenReturn(Optional.of(bank));

        assertThat(service.getBankByBinCode(new GetBankByBinCodeQuery("603799"))).isSameAs(bank);
    }

    @Test
    void getBankByUnknownBinCodeFails() {
        when(repository.findByBinCode("111111")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBankByBinCode(new GetBankByBinCodeQuery("111111")))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("111111");
    }

    @Test
    void findByIdReturnsBank() throws CardDomainException {
        Bank bank = bank(6L, true);
        when(repository.findById(6L)).thenReturn(Optional.of(bank));

        assertThat(service.findById(6L)).isSameAs(bank);
    }

    @Test
    void findByIdOfUnknownBankReportsActiveBankNotFound() {
        when(repository.findById(6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(6L))
                .isInstanceOfSatisfying(CardDomainException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(StatusService.ACTIVE_BANK_NOT_FOUND);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    @Test
    void listBanksDelegatesToRepository() throws CardDomainException {
        Map<String, String> params = Map.of("name", "Melli");
        Page<Bank> page = new PageImpl<>(List.of(bank(1L, true)));
        when(repository.findAllBanks(params)).thenReturn(page);

        assertThat(service.listBanks(params, "me", "ip")).isSameAs(page);
    }

    @Test
    void getAllBanksWithActiveOnlyUsesActiveQuery() throws CardDomainException {
        when(repository.findActiveBanks()).thenReturn(List.of(bank(1L, true)));

        assertThat(service.getAllBanks(new GetAllBanksQuery(true))).hasSize(1);
        verify(repository, never()).findAll();
    }

    @Test
    void getAllBanksWithoutFilterReturnsEveryBank() throws CardDomainException {
        when(repository.findAll()).thenReturn(List.of(bank(1L, true), bank(2L, false)));

        assertThat(service.getAllBanks(new GetAllBanksQuery(false))).hasSize(2);
        assertThat(service.getAllBanks(new GetAllBanksQuery(null))).hasSize(2);
        assertThat(service.getAllBanks(new GetAllBanksQuery())).hasSize(2);
        verify(repository, never()).findActiveBanks();
    }

    @Test
    void validateBankExistsPassesForExistingBank() throws CardDomainException {
        when(repository.findById(7L)).thenReturn(Optional.of(bank(7L, true)));

        service.validateBankExists(7L);

        verify(repository).findById(7L);
    }

    @Test
    void validateBankExistsFailsForUnknownBank() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateBankExists(7L)).isInstanceOf(CardDomainException.class);
    }
}
