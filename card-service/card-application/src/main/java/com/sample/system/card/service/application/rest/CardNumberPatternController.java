package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.*;
import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.numberPattern.CreateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.DeactivateCardNumberPatternCommand;
import com.sample.system.card.service.domain.command.numberPattern.GetCardNumberPatternQuery;
import com.sample.system.card.service.domain.command.numberPattern.UpdateCardNumberPatternCommand;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardNumberPatternCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardNumberPatternQueryHandler;
import com.sample.system.card.service.domain.response.number.CreateCardNumberPatternResponse;
import com.sample.system.card.service.domain.response.number.GetCardNumberPatternResponse;
import com.sample.system.card.service.domain.response.number.CardNumberPatternListResponse;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import com.sample.system.card.service.domain.response.number.UpdateCardNumberPatternResponse;
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
 * REST Controller for Card Number Pattern Management
 * Provides endpoints for Card Number Pattern CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/card-number-pattern", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Card Number Pattern Management", description = "API for managing card number patterns and ranges")
public class CardNumberPatternController extends BaseController {

    private final CardNumberPatternCommandHandler cardNumberPatternCommandHandler;
    private final CardNumberPatternQueryHandler cardNumberPatternQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

        summary = "ایجاد الگوی شماره کارت جدید",
        description = "Create a new card number pattern with range and configuration"
    )
    public ResponseEntity<BaseResponse<CreateCardNumberPatternResponse>> createCardNumberPattern(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد الگوی شماره کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "productCode": "12",
                                              "cardNumGenerationMethod": 2,
                                              "name": "الگوی شماره کارت طلایی",
                                              "status": 1,
                                              "cardNumberFrom": "0000000",
                                              "cardNumberTo": "9999999"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid CreateCardNumberPatternCommand createCommand) throws CardDomainException {

        log.info("Creating card number pattern: {} to {}", createCommand.getCardNumberFrom(), createCommand.getCardNumberTo());
        CreateCardNumberPatternResponse response = cardNumberPatternCommandHandler.createCardNumberPattern(createCommand);
        return ok(response);
    }

    @PutMapping(path = "/update")
    @Operation(

        summary = "به‌روزرسانی الگوی شماره کارت",
        description = "Update an existing card number pattern"
    )
    public ResponseEntity<BaseResponse<UpdateCardNumberPatternResponse>> updateCardNumberPattern(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای به‌روزرسانی الگوی شماره کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "patternId": 1,
                                              "name": "الگوی شماره کارت طلایی - به‌روزرسانی شده",
                                              "status": 1,
                                              "cardNumberFrom": "0000000",
                                              "cardNumberTo": "9999999"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid UpdateCardNumberPatternCommand updateCommand) throws CardDomainException {

        log.info("Updating card number pattern with ID: {}", updateCommand.getPatternId());
        UpdateCardNumberPatternResponse response = cardNumberPatternCommandHandler.updateCardNumberPattern(updateCommand);
        return ok(response);
    }

    @PutMapping(path = "/deactivate")
    @Operation(

        summary = "غیرفعال کردن الگوی شماره کارت",
        description = "Deactivate a card number pattern by setting its status to INACTIVE"
    )
    public ResponseEntity<BaseResponse<UpdateCardNumberPatternResponse>> deactivateCardNumberPattern(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای غیرفعال کردن الگوی شماره کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "patternId": 1
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid DeactivateCardNumberPatternCommand deactivateCommand) throws CardDomainException {

        log.info("Deactivating card number pattern with ID: {}", deactivateCommand.getPatternId());
        UpdateCardNumberPatternResponse response = cardNumberPatternCommandHandler.deactivateCardNumberPattern(deactivateCommand);
        return ok(response);
    }

    @GetMapping(path = "/{patternId}")
    @Operation(

        summary = "دریافت اطلاعات الگوی شماره کارت",
        description = "Get card number pattern details by ID"
    )
    public ResponseEntity<BaseResponse<GetCardNumberPatternResponse>> getCardNumberPattern(
            @Parameter(description = "Card number pattern ID", required = true)
            @PathVariable Long patternId) throws CardDomainException {

        log.info("Getting card number pattern with ID: {}", patternId);
        GetCardNumberPatternQuery query = GetCardNumberPatternQuery.builder().patternId(patternId).build();
        GetCardNumberPatternResponse response = cardNumberPatternQueryHandler.getCardNumberPattern(query);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation(
     summary = "جستجوی الگوهای شماره کارت",
        description = "Search card number patterns with pagination and filtering options"
    )
    public ResponseEntity<BaseResponse<CardNumberPatternListResponse>> listCardNumberPatterns(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای جستجو - فیلدهای قابل استفاده: status (ACTIVE, INACTIVE, ASSIGN, EXHAUSTED), " +
                            "cardProfileId, page, size, createdBy, createDateFrom, createDateTo, lastModifiedBy," +
                            " lastModifiedDateFrom, lastModifiedDateTo",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                              {
                                                "parameterMap": {
                                                  "status": "1",
                                                  "page": "0",
                                                  "size": "10",
                                                  "cardProfileId": "1",
                                                  "productCode": "33",
                                                  "name": "الگو",
                                                  "createdBy": "admin",
                                                  "createDateFrom": "1404/01/01",
                                                  "createDateTo": "1404/12/20",
                                                  "lastModifiedBy": "admin",
                                                  "lastModifiedDateFrom": "1404/01/01",
                                                  "lastModifiedDateTo": "1404/12/20"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list card number patterns with params {}", map);
        CardNumberPatternListResponse listResponse = cardNumberPatternQueryHandler.listCardNumberPatterns(map, "", "");
        return ok(listResponse);
    }

    @GetMapping(path = "/health")
    @Operation(
        summary = "Health check for Card Number Pattern service",
        description = "Check if Card Number Pattern service is working properly"
    )
    public ResponseEntity<String> healthCheck() {
        log.info("Card Number Pattern service health check");
        return ResponseEntity.ok("Card Number Pattern service is running!");
    }
}
