package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.cardType.CreateCardTypeCommand;
import com.sample.system.card.service.domain.command.cardType.GetCardTypeQuery;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardTypeCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardTypeQueryHandler;
import com.sample.system.card.service.domain.response.cardType.CardTypeListResponse;
import com.sample.system.card.service.domain.response.cardType.CreateCardTypeResponse;
import com.sample.system.card.service.domain.response.cardType.GetCardTypeResponse;
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
 * REST Controller for CardType Management
 * Provides endpoints for CardType CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/card-type", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Card Type Management", description = "API for managing card types")
public class CardTypeController extends BaseController {

    private final CardTypeCommandHandler cardTypeCommandHandler;
    private final CardTypeQueryHandler cardTypeQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

        summary = "ایجاد نوع کارت جدید",
        description = "Create a new card type"
    )
    public ResponseEntity<BaseResponse<CreateCardTypeResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد نوع کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "DEBIT",
                                              "name": "دبیت",
                                              "description": "نوع کارت دبیت",
                                              "isActive": true
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Card type creation details", required = true)
            @RequestBody @Valid CreateCardTypeCommand createCardTypeCommand) throws CardDomainException {

        log.info("Creating card type: {}", createCardTypeCommand.getName());
        CreateCardTypeResponse response = cardTypeCommandHandler.createCardType(createCardTypeCommand);
        return ok(response);
    }

    @GetMapping(path = "/{cardTypeId}")
    @Operation(

        summary = "دریافت اطلاعات نوع کارت",
        description = "Get card type details by ID"
    )
    public ResponseEntity<BaseResponse<GetCardTypeResponse>> findById(
            @Parameter(description = "Card type ID", required = true, example = "1")
            @PathVariable Long cardTypeId) throws CardDomainException {

        log.info("Getting card type with ID: {}", cardTypeId);
        GetCardTypeQuery query = GetCardTypeQuery.builder().cardTypeId(cardTypeId).build();
        GetCardTypeResponse response = cardTypeQueryHandler.getCardType(query);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی نوع کارت",
            description = "")
    public ResponseEntity<BaseResponse<CardTypeListResponse>> list(
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
                                                  "name": "Debit",
                                                  "code": "1",
                                                  "isActive": "true"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list card types with params {}", map);
        CardTypeListResponse listResponse = cardTypeQueryHandler.listCardTypes(map, "", "");
        return ok(listResponse);
    }
}

