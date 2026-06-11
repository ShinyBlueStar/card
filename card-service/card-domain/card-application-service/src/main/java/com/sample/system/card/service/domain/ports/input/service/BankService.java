package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.event.bank.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface BankService extends BaseService {

    BankCreatedEvent createBank(@Valid CreateBankCommand command) throws CardDomainException;
    BankUpdatedEvent updateBank(@Valid UpdateBankCommand command) throws CardDomainException;
    BankActivatedEvent activateBank(@Valid ActivateBankCommand command) throws CardDomainException;
    BankDeactivatedEvent deactivateBank(@Valid DeactivateBankCommand command) throws CardDomainException;
    Bank getBank(@Valid GetBankQuery query) throws CardDomainException;
    Bank getBankByBinCode(@Valid GetBankByBinCodeQuery query) throws CardDomainException;
    List<Bank> getAllBanks(@Valid GetAllBanksQuery query) throws CardDomainException;
    Page<Bank> listBanks(Map<String, String> params, String caller, String ip) throws CardDomainException;
    void validateBankExists(Long bankId) throws CardDomainException;
    Bank findById(Long bankId) throws CardDomainException;
}
