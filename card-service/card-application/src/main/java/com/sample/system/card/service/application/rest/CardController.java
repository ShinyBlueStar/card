package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.application.openapi.CardSecretOpenApiExamples;
import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.card.*;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardQueryHandler;
import com.sample.system.card.service.domain.response.accessfile.AccessFileResponse;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import com.sample.system.card.service.domain.response.card.*;
import com.sample.system.card.service.domain.utility.PanMaskingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/card", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Card Management", description = "API for managing cards")
public class CardController extends BaseController {

    private final CardCommandHandler cardCommandHandler;
    private final CardQueryHandler cardQueryHandler;
    private final com.sample.system.card.service.domain.handler.Command.CardSecretCommandHandler cardSecretCommandHandler;

    @GetMapping(path = "/{cardId}")
    @Operation(

        summary = "دریافت اطلاعات کارت",
        description = "Get card details by ID"
    )
    public ResponseEntity<BaseResponse<GetCardResponse>> getCard(
            @Parameter(description = "Card ID", required = true, example = "1")
            @PathVariable Long cardId) throws CardDomainException {
        log.info("Getting card with ID: {}", cardId);
        GetCardResponse response = cardQueryHandler.getCard(cardId);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی کارت",
            description = "Search cards with pagination and filtering")
    public ResponseEntity<BaseResponse<CardListResponse>> listCards(
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
                                                  "id": "1",
                                                  "pan": "6037996449000002",
                                                  "nationalId": "0012345678",
                                                  "issuingBank": "1",
                                                  "cardStatus": "1",
                                                  "cardTypeId": "2",
                                                  "issuanceType": "physical",
                                                  "cardProfileId": "1",
                                                  "isVirtualCard": "false",
                                                  "issueDateFrom": "1404/10/01",
                                                  "issueDateTo": "1404/10/10",
                                                  "createdDateFrom": "1404/10/01",
                                                  "createdDateTo": "1404/10/10",
                                                  "page": "0",
                                                  "size":"10"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {
        var map = searchParams(baseSearchQuery);
        log.info("list cards with params {}", map);
        CardListResponse listResponse = cardQueryHandler.listCards(map, "", "");
        return ok(listResponse);
    }

    @PostMapping(path = "/change-status")
    @Operation(

        summary = "تغییر وضعیت کارت",
        description = "Activate, deactivate, block or unblock a card using a single endpoint"
    )
    public ResponseEntity<BaseResponse<UpdateCardResponse>> changeStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای تغییر وضعیت کارت - action می‌تواند ACTIVATE، DEACTIVATE، BLOCK یا UNBLOCK باشد",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Activate card",
                                            value = """
                                                    {
                                                      "cardId": 1,
                                                      "action": "ACTIVATE"
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Deactivate card",
                                            value = """
                                                    {
                                                      "cardId": 1,
                                                      "action": "DEACTIVATE",
                                                      "reasonId": 1
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Block card",
                                            value = """
                                                    {
                                                      "cardId": 1,
                                                      "action": "BLOCK",
                                                      "reasonId": 1
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Unblock card (رفع مسدودی)",
                                            value = """
                                                    {
                                                      "cardId": 1,
                                                      "action": "UNBLOCK",
                                                      "reasonId": 1
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody @Valid ChangeCardStatusCommand command) throws CardDomainException {

        log.info("Changing card status - CardId: {}, action: {}", command.getCardId(), command.getAction());
        UpdateCardResponse response = cardCommandHandler.changeStatus(command);
        return ok(response);
    }

    @GetMapping(path = "/card/print/access")
    @Operation(

            summary = "تولید فایل Access کارت‌ها",
            description = "تولید فایل اکسس از داده‌های کارت، ترک و مشتری و بازگرداندن آدرس دانلود"
    )
    public ResponseEntity<BaseResponse<AccessFileResponse>> exportAccessFile() throws CardDomainException {
        log.info("Generating Access export file for cards");
        AccessFileResponse response = cardQueryHandler.generateAccessFile();
        return ok(response);
    }

    @GetMapping(path = "/health")
    @Operation(
        summary = "Health check for Card service",
        description = "Check if Card service is working properly"
    )
    public ResponseEntity<String> healthCheck() {
        log.info("Card service health check");
        return ResponseEntity.ok("Card service is running!");
    }

    // ===================== SECRET OPERATIONS (PIN/OTP/CVV2) =====================

    @PostMapping(path = "/secrets/generate")
    @Operation(

            summary = "تولید PIN/OTP/CVV2",
            description = CardSecretOpenApiExamples.GENERATE_OPERATION_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = CardSecretOpenApiExamples.GENERATE_REQUEST_BODY_DESCRIPTION,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = CardSecretOpenApiExamples.Generate.NAME_PIN,
                                            summary = CardSecretOpenApiExamples.Generate.SUMMARY_PIN,
                                            value = CardSecretOpenApiExamples.Generate.BODY_PIN
                                    ),
                                    @ExampleObject(
                                            name = CardSecretOpenApiExamples.Generate.NAME_OTP,
                                            summary = CardSecretOpenApiExamples.Generate.SUMMARY_OTP,
                                            value = CardSecretOpenApiExamples.Generate.BODY_OTP
                                    ),
                                    @ExampleObject(
                                            name = CardSecretOpenApiExamples.Generate.NAME_CVV,
                                            summary = CardSecretOpenApiExamples.Generate.SUMMARY_CVV,
                                            value = CardSecretOpenApiExamples.Generate.BODY_CVV
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<BaseResponse<com.sample.system.card.service.domain.response.card.GenerateSecretResponse>> generateSecret(
            @RequestBody @Valid GenerateSecretCommand command) throws CardDomainException {
        log.info("Generating secret - PAN={}, type={}",
                PanMaskingUtil.maskPan(command.getPan()),
                command.getType());

        GenerateSecretResponse response = cardSecretCommandHandler.generate(command);

        return ok(response);
    }

    @PostMapping(path = "/secrets/validate")
    @Operation(

            summary = "اعتبارسنجی PIN/OTP/CVV2",
            description = CardSecretOpenApiExamples.VALIDATE_OPERATION_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = CardSecretOpenApiExamples.VALIDATE_REQUEST_BODY_DESCRIPTION,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = CardSecretOpenApiExamples.Validate.NAME_PIN,
                                            summary = CardSecretOpenApiExamples.Validate.SUMMARY_PIN,
                                            value = CardSecretOpenApiExamples.Validate.BODY_PIN
                                    ),
                                    @ExampleObject(
                                            name = CardSecretOpenApiExamples.Validate.NAME_OTP,
                                            summary = CardSecretOpenApiExamples.Validate.SUMMARY_OTP,
                                            value = CardSecretOpenApiExamples.Validate.BODY_OTP
                                    ),
                                    @ExampleObject(
                                            name = CardSecretOpenApiExamples.Validate.NAME_CVV,
                                            summary = CardSecretOpenApiExamples.Validate.SUMMARY_CVV,
                                            value = CardSecretOpenApiExamples.Validate.BODY_CVV
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<BaseResponse<ValidateSecretResponse>> validateSecret(
            @RequestBody @Valid ValidateSecretCommand command) throws CardDomainException {
        log.info("Validating secret - PAN={}, type={}", PanMaskingUtil.maskPan(command.getPan()), command.getType());
        ValidateSecretResponse response = cardSecretCommandHandler.validate(command);
        return ok(response);
    }
}
