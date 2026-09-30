package com.sample.system.card.service.domain.handler;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.event.bank.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.BankCommandHandler;
import com.sample.system.card.service.domain.handler.query.BankQueryHandler;
import com.sample.system.card.service.domain.mapper.BankDataMapper;
import com.sample.system.card.service.domain.ports.input.service.BankService;
import com.sample.system.card.service.domain.response.bank.BankListResponse;
import com.sample.system.card.service.domain.response.bank.CreateBankResponse;
import com.sample.system.card.service.domain.response.bank.GetBankResponse;
import com.sample.system.card.service.domain.response.bank.UpdateBankResponse;
import com.sample.system.card.service.domain.valueObject.BankId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankHandlersTest {

    @Mock
    private BankService service;

    private BankCommandHandler commandHandler;
    private BankQueryHandler queryHandler;

    @BeforeEach
    void setUp() {
        BankDataMapper mapper = new BankDataMapper();
        commandHandler = new BankCommandHandler(service, mapper);
        queryHandler = new BankQueryHandler(service, mapper);
    }

    private static Bank bank(long id, boolean active) {
        Bank bank = Bank.builder().binCode("603799").name("Melli").isActive(active).build();
        bank.setId(new BankId(id));
        return bank;
    }

    @Test
    void createReturnsResponseBuiltFromEvent() throws CardDomainException {
        CreateBankCommand command = new CreateBankCommand("603799", "Melli", true);
        when(service.createBank(command)).thenReturn(new BankCreatedEvent(bank(1L, true), ZonedDateTime.now()));

        CreateBankResponse response = commandHandler.createBank(command);

        assertThat(response.getBankId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("Bank created successfully");
    }

    @Test
    void updateReturnsResponseBuiltFromEvent() throws CardDomainException {
        UpdateBankCommand command = new UpdateBankCommand(2L, "603799", "Melli", true);
        when(service.updateBank(command)).thenReturn(new BankUpdatedEvent(bank(2L, true), ZonedDateTime.now()));

        UpdateBankResponse response = commandHandler.updateBank(command);

        assertThat(response.getBankId()).isEqualTo(2L);
        assertThat(response.getMessage()).isEqualTo("Bank updated successfully");
    }

    @Test
    void activateReturnsActivatedMessage() throws CardDomainException {
        ActivateBankCommand command = new ActivateBankCommand(3L);
        when(service.activateBank(command)).thenReturn(new BankActivatedEvent(bank(3L, true), ZonedDateTime.now()));

        UpdateBankResponse response = commandHandler.activateBank(command);

        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Bank activated successfully");
    }

    @Test
    void deactivateReturnsDeactivatedMessage() throws CardDomainException {
        DeactivateBankCommand command = new DeactivateBankCommand(4L);
        when(service.deactivateBank(command)).thenReturn(new BankDeactivatedEvent(bank(4L, false), ZonedDateTime.now()));

        UpdateBankResponse response = commandHandler.deactivateBank(command);

        assertThat(response.getIsActive()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Bank deactivated successfully");
    }

    @Test
    void changeStatusToActiveActivatesTheBank() throws CardDomainException {
        when(service.activateBank(any(ActivateBankCommand.class)))
                .thenReturn(new BankActivatedEvent(bank(5L, true), ZonedDateTime.now()));

        UpdateBankResponse response = commandHandler.changeBankStatus(new ChangeBankStatusCommand(5L, true));

        ArgumentCaptor<ActivateBankCommand> captor = ArgumentCaptor.forClass(ActivateBankCommand.class);
        verify(service).activateBank(captor.capture());
        assertThat(captor.getValue().getBankId()).isEqualTo(5L);
        assertThat(response.getMessage()).isEqualTo("Bank activated successfully");
    }

    @Test
    void changeStatusToInactiveDeactivatesTheBank() throws CardDomainException {
        when(service.deactivateBank(any(DeactivateBankCommand.class)))
                .thenReturn(new BankDeactivatedEvent(bank(6L, false), ZonedDateTime.now()));

        UpdateBankResponse response = commandHandler.changeBankStatus(new ChangeBankStatusCommand(6L, false));

        ArgumentCaptor<DeactivateBankCommand> captor = ArgumentCaptor.forClass(DeactivateBankCommand.class);
        verify(service).deactivateBank(captor.capture());
        assertThat(captor.getValue().getBankId()).isEqualTo(6L);
        assertThat(response.getMessage()).isEqualTo("Bank deactivated successfully");
    }

    @Test
    void changeStatusWithoutFlagIsTreatedAsDeactivation() throws CardDomainException {
        when(service.deactivateBank(any(DeactivateBankCommand.class)))
                .thenReturn(new BankDeactivatedEvent(bank(7L, false), ZonedDateTime.now()));

        UpdateBankResponse response = commandHandler.changeBankStatus(new ChangeBankStatusCommand(7L, null));

        assertThat(response.getMessage()).isEqualTo("Bank deactivated successfully");
    }

    @Test
    void commandHandlerPropagatesServiceErrors() throws CardDomainException {
        CreateBankCommand command = new CreateBankCommand("603799", "Melli", true);
        when(service.createBank(command)).thenThrow(new CardDomainException("boom"));

        assertThatThrownBy(() -> commandHandler.createBank(command))
                .isInstanceOf(CardDomainException.class).hasMessageContaining("boom");
    }

    @Test
    void getBankMapsServiceResult() throws CardDomainException {
        GetBankQuery query = new GetBankQuery(8L);
        when(service.getBank(query)).thenReturn(bank(8L, true));

        GetBankResponse response = queryHandler.getBank(query);

        assertThat(response.getId()).isEqualTo(8L);
        assertThat(response.getBinCode()).isEqualTo("603799");
    }

    @Test
    void getBankByBinCodeMapsServiceResult() throws CardDomainException {
        GetBankByBinCodeQuery query = new GetBankByBinCodeQuery("603799");
        when(service.getBankByBinCode(query)).thenReturn(bank(9L, true));

        assertThat(queryHandler.getBankByBinCode(query).getId()).isEqualTo(9L);
    }

    @Test
    void getAllBanksMapsEveryBank() throws CardDomainException {
        GetAllBanksQuery query = new GetAllBanksQuery(true);
        when(service.getAllBanks(query)).thenReturn(List.of(bank(1L, true), bank(2L, true)));

        assertThat(queryHandler.getAllBanks(query)).extracting(GetBankResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void listBanksMapsThePage() throws CardDomainException {
        Map<String, String> params = Map.of("page", "0");
        when(service.listBanks(params, "me", "1.1.1.1")).thenReturn(new PageImpl<>(List.of(bank(1L, true))));

        BankListResponse response = queryHandler.listBanks(params, "me", "1.1.1.1");

        assertThat(response.getList()).hasSize(1);
    }

    @Test
    void listBanksWithoutPageGivesNull() throws CardDomainException {
        Map<String, String> params = Map.of();
        when(service.listBanks(params, "me", "ip")).thenReturn(null);

        assertThat(queryHandler.listBanks(params, "me", "ip")).isNull();
    }
}
