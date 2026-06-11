package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.application.handler.CardGlobalExceptionHandler;
import com.sample.system.card.service.domain.command.bank.CreateBankCommand;
import com.sample.system.card.service.domain.command.bank.GetBankQuery;
import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.BankCommandHandler;
import com.sample.system.card.service.domain.handler.query.BankQueryHandler;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import com.sample.system.card.service.domain.response.bank.CreateBankResponse;
import com.sample.system.card.service.domain.response.bank.GetBankResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CardGlobalExceptionHandler.class)
class BankControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BankCommandHandler commandHandler;
    @MockitoBean
    private BankQueryHandler queryHandler;
    @MockitoBean
    private StatusService statusService;

    @Test
    void createBankReturnsWrappedResponse() throws Exception {
        when(commandHandler.createBank(any(CreateBankCommand.class))).thenReturn(CreateBankResponse.builder()
                .bankId(1L).binCode("603799").name("Melli").isActive(true).message("Bank created successfully").build());

        mvc.perform(post("/api/v1/banks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"binCode": "603799", "name": "Melli", "isActive": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bankId").value(1))
                .andExpect(jsonPath("$.data.binCode").value("603799"))
                .andExpect(jsonPath("$.doTimeStamp").isNumber());
    }

    @Test
    void invalidBankIsRejectedBeforeReachingTheHandler() throws Exception {
        mvc.perform(post("/api/v1/banks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"binCode": "12", "name": "Melli"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorDetail.code").value(String.valueOf(StatusService.INPUT_PARAMETER_NOT_VALID)))
                .andExpect(jsonPath("$.errorDetail.message").value("BIN code must be exactly 6 digits"));

        verify(commandHandler, never()).createBank(any());
    }

    @Test
    void getBankReturnsBank() throws Exception {
        when(queryHandler.getBank(any(GetBankQuery.class)))
                .thenReturn(GetBankResponse.builder().id(7L).binCode("603799").name("Melli").isActive(true).build());

        mvc.perform(get("/api/v1/banks/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("Melli"));
    }

    @Test
    void domainErrorIsTranslatedWithStatusDescription() throws Exception {
        when(queryHandler.getBank(any(GetBankQuery.class))).thenThrow(new CardDomainException(
                "Bank not found", StatusService.ID_NOT_FOUND, HttpStatus.BAD_REQUEST));
        when(statusService.findByCode(String.valueOf(StatusService.ID_NOT_FOUND)))
                .thenReturn(new Status("شناسه یافت نشد", "11", "ID not found"));

        mvc.perform(get("/api/v1/banks/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorDetail.code").value("11"))
                .andExpect(jsonPath("$.errorDetail.message").value("شناسه یافت نشد"));
    }

    @Test
    void unexpectedErrorBecomesGeneralError() throws Exception {
        when(queryHandler.getBankByBinCode(any())).thenThrow(new IllegalStateException("boom"));
        when(statusService.findByCode(anyString())).thenReturn(new Status("خطای عمومی", "999", "General error"));

        mvc.perform(get("/api/v1/banks/bin/603799"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorDetail.code").value(String.valueOf(StatusService.GENERAL_ERROR)));
    }

    @Test
    void listPassesSearchParameters() throws Exception {
        mvc.perform(post("/api/v1/banks/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parameterMap": {"page": "0", "size": "5", "name": "ملی"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(queryHandler).listBanks(eq(Map.of("page", "0", "size", "5", "name", "ملی")), eq(""), eq(""));
    }
}
