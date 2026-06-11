package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.response.bank.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BankDataMapper {

    public Bank createCommandToBank(CreateBankCommand createBankCommand) {
        log.debug("Converting CreateBankCommand to Bank - Name: {}, BIN: {}",
                createBankCommand.getName(), createBankCommand.getBinCode());

        Bank bank = Bank.builder()
                .binCode(createBankCommand.getBinCode())
                .name(createBankCommand.getName())
                .isActive(createBankCommand.getIsActive())
                .build();

        log.debug("Successfully converted CreateBankCommand to Bank");
        return bank;
    }

    public CreateBankResponse bankToCreateResponse(Bank bank, String message) {
        log.debug("Converting Bank to CreateBankResponse - ID: {}", bank.getId().getValue());

        CreateBankResponse response = CreateBankResponse.builder()
                .bankId(bank.getId().getValue())
                .binCode(bank.getBinCode())
                .name(bank.getName())
                .isActive(bank.getIsActive())
                .message(message)
                .build();

        log.debug("Successfully converted Bank to CreateBankResponse");
        return response;
    }

    public void updateBankFromCommand(Bank bank, UpdateBankCommand command) {
        log.debug("Updating Bank from UpdateBankCommand - Bank ID: {}, Command ID: {}",
                bank.getId().getValue(), command.getBankId());

        if (command.getBinCode() != null) {
            log.debug("Updating BIN code from {} to {}", bank.getBinCode(), command.getBinCode());
            bank.setBinCode(command.getBinCode());
        }
        if (command.getName() != null) {
            log.debug("Updating name from {} to {}", bank.getName(), command.getName());
            bank.setName(command.getName());
        }
        if (command.getIsActive() != null) {
            log.debug("Updating isActive from {} to {}", bank.getIsActive(), command.getIsActive());
            bank.setIsActive(command.getIsActive());
        }

        log.debug("Successfully updated Bank from UpdateBankCommand");
    }

    public UpdateBankResponse bankToUpdateResponse(Bank bank, String message) {
        log.debug("Converting Bank to UpdateBankResponse - ID: {}", bank.getId().getValue());

        UpdateBankResponse response = UpdateBankResponse.builder()
                .bankId(bank.getId().getValue())
                .message(message)
                .isActive(bank.getIsActive())
                .build();

        log.debug("Successfully converted Bank to UpdateBankResponse");
        return response;
    }

    public GetBankResponse bankToGetResponse(Bank bank) {
        log.debug("Converting Bank to GetBankResponse - ID: {}", bank.getId().getValue());

        GetBankResponse response = GetBankResponse.builder()
                .id(bank.getId().getValue())
                .binCode(bank.getBinCode())
                .name(bank.getName())
                .isActive(bank.getIsActive())
                .build();

        log.debug("Successfully converted Bank to GetBankResponse");
        return response;
    }

    public List<GetBankResponse> banksToGetResponses(List<Bank> banks) {
        log.debug("Converting {} banks to GetBankResponse list", banks.size());

        List<GetBankResponse> responses = banks.stream()
                .map(this::bankToGetResponse)
                .toList();

        log.debug("Successfully converted {} banks to GetBankResponse list", responses.size());
        return responses;
    }

    public BankListResponse banksToGetListResponse(Page<Bank> banks) {
        if (banks == null) return null;

        BankListResponse bankListResponse = new BankListResponse();
        bankListResponse.setList(banks.getContent().stream()
                .map(this::bankToGetResponse).toList());
        bankListResponse.setNumber(banks.getNumber());
        bankListResponse.setSize(banks.getSize());
        bankListResponse.setTotalElements(banks.getTotalElements());
        bankListResponse.setTotalPages(banks.getTotalPages());
        return bankListResponse;
    }
}
