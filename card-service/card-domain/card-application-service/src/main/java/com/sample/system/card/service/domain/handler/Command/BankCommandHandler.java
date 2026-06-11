package com.sample.system.card.service.domain.handler.Command;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.event.bank.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.BankDataMapper;
import com.sample.system.card.service.domain.ports.input.service.BankService;
import com.sample.system.card.service.domain.response.bank.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankCommandHandler {

    private final BankService bankService;
    private final BankDataMapper bankDataMapper;

    @Transactional
    public CreateBankResponse createBank(CreateBankCommand command) throws CardDomainException {
        log.info("Starting Bank creation handler - Name: {}, BIN: {}", command.getName(), command.getBinCode());
        BankCreatedEvent event = bankService.createBank(command);
        log.debug("Mapping Bank to CreateBankResponse");
        return bankDataMapper.bankToCreateResponse(event.getBank(), "Bank created successfully");
    }

    @Transactional
    public UpdateBankResponse updateBank(UpdateBankCommand command) throws CardDomainException {
        log.info("Starting Bank update handler - BankId: {}", command.getBankId());
        BankUpdatedEvent event = bankService.updateBank(command);
        log.debug("Mapping Bank to UpdateBankResponse");
        return bankDataMapper.bankToUpdateResponse(event.getBank(), "Bank updated successfully");
    }

    @Transactional
    public UpdateBankResponse activateBank(ActivateBankCommand command) throws CardDomainException {
        log.info("Starting Bank activation handler - BankId: {}", command.getBankId());
        BankActivatedEvent event = bankService.activateBank(command);
        log.debug("Mapping Bank to UpdateBankResponse");
        return bankDataMapper.bankToUpdateResponse(event.getBank(), "Bank activated successfully");
    }

    @Transactional
    public UpdateBankResponse deactivateBank(DeactivateBankCommand command) throws CardDomainException {
        log.info("Starting Bank deactivation handler - BankId: {}", command.getBankId());
        BankDeactivatedEvent event = bankService.deactivateBank(command);
        log.debug("Mapping Bank to UpdateBankResponse");
        return bankDataMapper.bankToUpdateResponse(event.getBank(), "Bank deactivated successfully");
    }

    @Transactional
    public UpdateBankResponse changeBankStatus(ChangeBankStatusCommand command) throws CardDomainException {
        log.info("Changing bank status - BankId: {}, Active: {}", command.getBankId(), command.getActive());
        UpdateBankResponse response;
        if (Boolean.TRUE.equals(command.getActive())) {
            BankActivatedEvent event = bankService.activateBank(
                    ActivateBankCommand.builder().bankId(command.getBankId()).build());
            response = bankDataMapper.bankToUpdateResponse(event.getBank(), "Bank activated successfully");
        } else {
            BankDeactivatedEvent event = bankService.deactivateBank(
                    DeactivateBankCommand.builder().bankId(command.getBankId()).build());
            response = bankDataMapper.bankToUpdateResponse(event.getBank(), "Bank deactivated successfully");
        }
        log.info("Bank status changed successfully - BankId: {}, Active: {}", command.getBankId(), command.getActive());
        return response;
    }
}
