package com.sample.system.card.service.domain.handler.query;

import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.mapper.BankDataMapper;
import com.sample.system.card.service.domain.ports.input.service.BankService;
import com.sample.system.card.service.domain.response.bank.BankListResponse;
import com.sample.system.card.service.domain.response.bank.GetBankResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankQueryHandler {

    private final BankService bankService;
    private final BankDataMapper bankDataMapper;

    @Transactional(readOnly = true)
    public GetBankResponse getBank(GetBankQuery query) throws CardDomainException {
        log.info("Getting bank with id: {}", query.getBankId());
        Bank bank = bankService.getBank(query);
        log.info("Bank retrieval completed by QueryHandler - BankId: {}", query.getBankId());
        return bankDataMapper.bankToGetResponse(bank);
    }

    @Transactional(readOnly = true)
    public GetBankResponse getBankByBinCode(GetBankByBinCodeQuery query) throws CardDomainException {
        log.info("Getting bank with BIN code: {}", query.getBinCode());
        Bank bank = bankService.getBankByBinCode(query);
        log.info("Bank retrieval by BIN code completed by QueryHandler - BinCode: {}", query.getBinCode());
        return bankDataMapper.bankToGetResponse(bank);
    }

    @Transactional(readOnly = true)
    public List<GetBankResponse> getAllBanks(GetAllBanksQuery query) throws CardDomainException {
        log.info("Getting all banks with activeOnly filter: {}", query.getActiveOnly());
        List<Bank> banks = bankService.getAllBanks(query);
        log.info("Banks retrieval completed by QueryHandler - Count: {}", banks.size());
        return bankDataMapper.banksToGetResponses(banks);
    }

    public BankListResponse listBanks(Map<String, String> map, String caller, String ip) throws CardDomainException {
        org.springframework.data.domain.Page<Bank> responses = bankService.listBanks(map, caller, ip);
        log.debug("Banks retrieval completed by QueryHandler - Count: {}",
                responses != null ? responses.getContent().size() : 0);
        return bankDataMapper.banksToGetListResponse(responses);
    }
}
