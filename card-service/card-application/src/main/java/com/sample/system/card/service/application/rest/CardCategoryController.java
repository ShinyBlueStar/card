package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.cardCategory.CreateCardCategoryCommand;
import com.sample.system.card.service.domain.command.cardCategory.GetCardCategoryQuery;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardCategoryCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardCategoryQueryHandler;
import com.sample.system.card.service.domain.response.cardCategory.CardCategoryListResponse;
import com.sample.system.card.service.domain.response.cardCategory.CreateCardCategoryResponse;
import com.sample.system.card.service.domain.response.cardCategory.GetCardCategoryResponse;
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
 * REST Controller for CardCategory Management
 * Provides endpoints for CardCategory CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/card-category", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Card Category Management", description = "API for managing card categories")
public class CardCategoryController extends BaseController {

    private final CardCategoryCommandHandler cardCategoryCommandHandler;
    private final CardCategoryQueryHandler cardCategoryQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

        summary = "ایجاد دسته‌بندی کارت جدید",
        description = "Create a new card category"
    )
    public ResponseEntity<BaseResponse<CreateCardCategoryResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد دسته‌بندی کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "cardTypeId": 1,
                                              "code": "GOLD",
                                              "name": "طلایی",
                                              "description": "دسته‌بندی کارت طلایی",
                                              "isActive": true
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Card category creation details", required = true)
            @RequestBody @Valid CreateCardCategoryCommand createCardCategoryCommand) throws CardDomainException {

        log.info("Creating card category: {}", createCardCategoryCommand.getName());
        CreateCardCategoryResponse response = cardCategoryCommandHandler.createCardCategory(createCardCategoryCommand);
        return ok(response);
    }

    @GetMapping(path = "/{cardCategoryId}")
    @Operation(

        summary = "دریافت اطلاعات دسته‌بندی کارت",
        description = "Get card category details by ID"
    )
    public ResponseEntity<BaseResponse<GetCardCategoryResponse>> findById(
            @Parameter(description = "Card category ID", required = true, example = "1")
            @PathVariable Long cardCategoryId) throws CardDomainException {

        log.info("Getting card category with ID: {}", cardCategoryId);
        GetCardCategoryQuery query = GetCardCategoryQuery.builder().cardCategoryId(cardCategoryId).build();
        GetCardCategoryResponse response = cardCategoryQueryHandler.getCardCategory(query);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی دسته‌بندی کارت",
            description = "")
    public ResponseEntity<BaseResponse<CardCategoryListResponse>> list(
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
                                                  "name": "Gold",
                                                  "code": "1",
                                                  "isActive": "true"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list card categories with params {}", map);
        CardCategoryListResponse listResponse = cardCategoryQueryHandler.listCardCategories(map, "", "");
        return ok(listResponse);
    }
}

