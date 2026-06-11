package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.event.bank.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.BankDataMapper;
import com.sample.system.card.service.domain.ports.input.service.BankService;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.ports.output.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankRepository repository;
    private final BankDataMapper bankDataMapper;

    @Override
    @Transactional
    public BankCreatedEvent createBank(CreateBankCommand command) throws CardDomainException {
        log.info("Starting Bank creation service - Name: {}, BIN: {}", command.getName(), command.getBinCode());
        Bank bank = bankDataMapper.createCommandToBank(command);
        Bank savedBank = repository.save(bank);
        if (savedBank == null) {
            log.error("Could not save Bank with name: {} and BIN code: {}", command.getName(), command.getBinCode());
            throw new CardDomainException("Could not save Bank with name " + command.getName());
        }
        BankCreatedEvent event = new BankCreatedEvent(savedBank, ZonedDateTime.now());
        log.info("Bank creation process completed successfully - BankId: {}, Name: {}",
                savedBank.getId().getValue(), savedBank.getName());
        return event;
    }

    @Override
    @Transactional
    public BankUpdatedEvent updateBank(UpdateBankCommand command) throws CardDomainException {
        log.info("Starting Bank update service - BankId: {}", command.getBankId());
        Bank bank = repository.findById(command.getBankId())
                .orElseThrow(() -> {
                    log.error("Bank not found with ID: {}", command.getBankId());
                    return new CardDomainException("Bank not found: " + command.getBankId());
                });
        bankDataMapper.updateBankFromCommand(bank, command);
        Bank updated = repository.save(bank);
        BankUpdatedEvent event = new BankUpdatedEvent(updated, ZonedDateTime.now());
        log.info("Bank update process completed successfully - BankId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    @Transactional
    public BankActivatedEvent activateBank(ActivateBankCommand command) throws CardDomainException {
        log.info("Starting Bank activation service - BankId: {}", command.getBankId());
        Bank bank = repository.findById(command.getBankId())
                .orElseThrow(() -> {
                    log.error("Bank not found with ID: {}", command.getBankId());
                    return new CardDomainException("Bank not found: " + command.getBankId());
                });
        bank.activate();
        Bank updated = repository.save(bank);
        BankActivatedEvent event = new BankActivatedEvent(updated, ZonedDateTime.now());
        log.info("Bank activation process completed successfully - BankId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    @Transactional
    public BankDeactivatedEvent deactivateBank(DeactivateBankCommand command) throws CardDomainException {
        log.info("Starting Bank deactivation service - BankId: {}", command.getBankId());
        Bank bank = repository.findById(command.getBankId())
                .orElseThrow(() -> {
                    log.error("Bank not found with ID: {}", command.getBankId());
                    return new CardDomainException("Bank not found: " + command.getBankId());
                });
        bank.deactivate();
        Bank updated = repository.save(bank);
        BankDeactivatedEvent event = new BankDeactivatedEvent(updated, ZonedDateTime.now());
        log.info("Bank deactivation process completed successfully - BankId: {}", updated.getId().getValue());
        return event;
    }

    @Override
    @Transactional(readOnly = true)
    public Bank getBank(GetBankQuery query) throws CardDomainException {
        log.info("Starting Bank retrieval service - BankId: {}", query.getBankId());
        Bank bank = repository.findById(query.getBankId())
                .orElseThrow(() -> {
                    log.error("Bank not found with id: {}", query.getBankId());
                    return new CardDomainException("Bank not found with id: " + query.getBankId());
                });
        log.info("Bank retrieval completed successfully - BankId: {}", query.getBankId());
        return bank;
    }

    @Override
    @Transactional(readOnly = true)
    public Bank getBankByBinCode(GetBankByBinCodeQuery query) throws CardDomainException {
        log.info("Starting Bank retrieval by BIN code service - BinCode: {}", query.getBinCode());
        Bank bank = repository.findByBinCode(query.getBinCode())
                .orElseThrow(() -> {
                    log.error("Bank not found with BIN code: {}", query.getBinCode());
                    return new CardDomainException("Bank not found with BIN code: " + query.getBinCode());
                });
        log.info("Bank retrieval by BIN code completed successfully - BinCode: {}", query.getBinCode());
        return bank;
    }

    @Override
    public Bank findById(Long id) throws CardDomainException {
        log.info("Starting Bank retrieval service from ID - BankId: {}", id);
        Bank bank = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Bank not found with id: {}", id);
                    return new CardDomainException("Bank not found with id: " + id,
                            StatusService.ACTIVE_BANK_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });
        log.info("Bank retrieval completed successfully - BankId: {}", id);
        return bank;
    }

    @Override
    public Page<Bank> listBanks(Map<String, String> params, String caller, String ip) throws CardDomainException {
        log.info("Starting Banks list service");
        Page<Bank> allBanks = repository.findAllBanks(params);
        return allBanks;
    }

    @Override
    public List<Bank> getAllBanks(GetAllBanksQuery query) throws CardDomainException {
        log.info("Starting Banks retrieval service - ActiveOnly: {}", query.getActiveOnly());
        List<Bank> banks = query.getActiveOnly() != null && query.getActiveOnly()
                ? repository.findActiveBanks()
                : repository.findAll();
        log.info("Banks retrieval completed successfully - Count: {}", banks.size());
        return banks;
    }

    @Override
    public void validateBankExists(Long bankId) throws CardDomainException {
        log.info("Validating bank existence for ID: {}", bankId);
        GetBankQuery query = GetBankQuery.builder().bankId(bankId).build();
        getBank(query);
        log.info("Bank validation successful for ID: {}", bankId);
    }
}
