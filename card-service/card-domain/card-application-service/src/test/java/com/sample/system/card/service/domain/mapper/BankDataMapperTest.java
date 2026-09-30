package com.sample.system.card.service.domain.mapper;

import com.sample.system.card.service.domain.command.bank.CreateBankCommand;
import com.sample.system.card.service.domain.command.bank.UpdateBankCommand;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.response.bank.BankListResponse;
import com.sample.system.card.service.domain.response.bank.CreateBankResponse;
import com.sample.system.card.service.domain.response.bank.GetBankResponse;
import com.sample.system.card.service.domain.response.bank.UpdateBankResponse;
import com.sample.system.card.service.domain.valueObject.BankId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BankDataMapperTest {

    private final BankDataMapper mapper = new BankDataMapper();

    private static Bank bank(long id) {
        Bank bank = Bank.builder().binCode("603799").name("Melli").isActive(true).build();
        bank.setId(new BankId(id));
        return bank;
    }

    @Test
    void createCommandIsMappedToBank() {
        Bank bank = mapper.createCommandToBank(new CreateBankCommand("603799", "Melli", false));

        assertThat(bank.getBinCode()).isEqualTo("603799");
        assertThat(bank.getName()).isEqualTo("Melli");
        assertThat(bank.getIsActive()).isFalse();
    }

    @Test
    void bankIsMappedToCreateResponseWithMessage() {
        CreateBankResponse response = mapper.bankToCreateResponse(bank(3L), "done");

        assertThat(response.getBankId()).isEqualTo(3L);
        assertThat(response.getBinCode()).isEqualTo("603799");
        assertThat(response.getName()).isEqualTo("Melli");
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getMessage()).isEqualTo("done");
    }

    @Test
    void updateChangesEveryProvidedField() {
        Bank bank = bank(1L);

        mapper.updateBankFromCommand(bank, new UpdateBankCommand(1L, "627353", "Tejarat", false));

        assertThat(bank.getBinCode()).isEqualTo("627353");
        assertThat(bank.getName()).isEqualTo("Tejarat");
        assertThat(bank.getIsActive()).isFalse();
    }

    @Test
    void updateKeepsFieldsThatAreNull() {
        Bank bank = bank(1L);

        mapper.updateBankFromCommand(bank, new UpdateBankCommand(1L, null, null, null));

        assertThat(bank.getBinCode()).isEqualTo("603799");
        assertThat(bank.getName()).isEqualTo("Melli");
        assertThat(bank.getIsActive()).isTrue();
    }

    @Test
    void bankIsMappedToUpdateResponse() {
        UpdateBankResponse response = mapper.bankToUpdateResponse(bank(9L), "updated");

        assertThat(response.getBankId()).isEqualTo(9L);
        assertThat(response.getMessage()).isEqualTo("updated");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void bankIsMappedToGetResponse() {
        GetBankResponse response = mapper.bankToGetResponse(bank(4L));

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getBinCode()).isEqualTo("603799");
        assertThat(response.getName()).isEqualTo("Melli");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void banksAreMappedToResponseList() {
        List<GetBankResponse> responses = mapper.banksToGetResponses(List.of(bank(1L), bank(2L)));

        assertThat(responses).extracting(GetBankResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void emptyBankListGivesEmptyResponseList() {
        assertThat(mapper.banksToGetResponses(List.of())).isEmpty();
    }

    @Test
    void pageIsMappedToListResponseWithPagingInfo() {
        PageImpl<Bank> page = new PageImpl<>(List.of(bank(1L), bank(2L)), PageRequest.of(1, 2), 6);

        BankListResponse response = mapper.banksToGetListResponse(page);

        assertThat(response.getList()).hasSize(2);
        assertThat(response.getNumber()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(6);
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    void nullPageGivesNullListResponse() {
        assertThat(mapper.banksToGetListResponse(null)).isNull();
    }
}
