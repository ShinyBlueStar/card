package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.bank.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.BankCommandHandler;
import com.sample.system.card.service.domain.handler.query.BankQueryHandler;
import com.sample.system.card.service.domain.response.bank.*;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Bank Management
 * Provides endpoints for Bank CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bank Management", description = "API for managing banks")
public class BankController extends BaseController {

    private final BankCommandHandler bankCommandHandler;
    private final BankQueryHandler bankQueryHandler;

    @PostMapping("/create")
    @Operation(summary = "Create Bank", description = "Create a new bank")
    public ResponseEntity<BaseResponse<CreateBankResponse>> createBank(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد بانک",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "binCode": "603799",
                                              "name": "بانک ملی ایران",
                                              "isActive": true
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Bank creation details", required = true)
            @Valid @RequestBody CreateBankCommand command) throws CardDomainException {
        log.info("Creating bank with BIN code: {} and name: {}", command.getBinCode(), command.getName());
        CreateBankResponse response = bankCommandHandler.createBank(command);
            log.info("Bank created successfully with ID: {}", response.getBankId());
            return ok(response);
    }

    @PutMapping("/update/{bankId}")
    @Operation(summary = "Update Bank", description = "Update an existing bank")
    public ResponseEntity<BaseResponse<UpdateBankResponse>> updateBank(
            @Parameter(description = "Bank ID", required = true, example = "1")
            @PathVariable Long bankId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای به‌روزرسانی بانک",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "binCode": "603799",
                                              "name": "بانک ملی ایران - به‌روزرسانی شده",
                                              "isActive": true
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Bank update details", required = true)
            @Valid @RequestBody UpdateBankCommand command) throws CardDomainException {
        log.info("Updating bank with ID: {}", bankId);
        UpdateBankCommand updateCommand = UpdateBankCommand.builder()
                .bankId(bankId)
                .binCode(command.getBinCode())
                .name(command.getName())
                .isActive(command.getIsActive())
                .build();
        UpdateBankResponse response = bankCommandHandler.updateBank(updateCommand);
        log.info("Bank updated successfully with ID: {}", bankId);
        return ok(response);
    }

    @PostMapping("/change-status")
    @Operation(summary = "Change Bank Status", description = "Activate or deactivate a bank based on requested status")
    public ResponseEntity<BaseResponse<UpdateBankResponse>> changeStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای تغییر وضعیت بانک",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "bankId": 1,
                                              "active": true
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ChangeBankStatusCommand command) throws CardDomainException {
        log.info("Changing bank status - BankId: {}, Active: {}", command.getBankId(), command.getActive());
        UpdateBankResponse response = bankCommandHandler.changeBankStatus(command);
        return ok(response);
    }

    @GetMapping("/{bankId}")
    @Operation(summary = "Get Bank by ID", description = "Retrieve a bank by its ID")
    public ResponseEntity<BaseResponse<GetBankResponse>> findById(
            @Parameter(description = "Bank ID", required = true, example = "1")
            @PathVariable Long bankId) throws CardDomainException {
        log.info("Finding bank by ID: {}", bankId);
        GetBankQuery query = GetBankQuery.builder()
                .bankId(bankId)
                .build();
        GetBankResponse response = bankQueryHandler.getBank(query);
        log.info("Bank found successfully with ID: {}", bankId);
        return ok(response);
    }

    @GetMapping("/bin/{binCode}")
    @Operation(summary = "Get Bank by BIN Code", description = "Retrieve a bank by its BIN code")
    public ResponseEntity<BaseResponse<GetBankResponse>> findByBinCode(
            @Parameter(description = "Bank BIN Code", required = true, example = "603799")
            @PathVariable String binCode) throws CardDomainException {
        log.info("Finding bank by BIN code: {}", binCode);
        GetBankByBinCodeQuery query = GetBankByBinCodeQuery.builder()
                .binCode(binCode)
                .build();
        GetBankResponse response = bankQueryHandler.getBankByBinCode(query);
        log.info("Bank found successfully with BIN code: {}", binCode);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی بانک",
            description = "")
    public ResponseEntity<BaseResponse<BankListResponse>> listBanks(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای جستجو",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                              {
                                                "parameterMap": {
                                                  "page": "0",
                                                  "size":"10",
                                                  "id": "1",
                                                  "name": "بانک ملی",
                                                  "binCode": "603799",
                                                  "isActive": "true"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list banks with params {}", map);
        BankListResponse listResponse = bankQueryHandler.listBanks(map, "", "");
        return ok(listResponse);
    }
}
