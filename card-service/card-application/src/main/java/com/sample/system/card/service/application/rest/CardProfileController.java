package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.cardProfile.ChangeCardProfileStatusCommand;
import com.sample.system.card.service.domain.command.cardProfile.CreateCardProfileCommand;
import com.sample.system.card.service.domain.command.cardProfile.GetCardProfileQuery;
import com.sample.system.card.service.domain.command.cardProfile.UpdateCardProfileCommand;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardProfileCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardProfileQueryHandler;
import com.sample.system.card.service.domain.response.profile.CreateCardProfileResponse;
import com.sample.system.card.service.domain.response.profile.GetCardProfileResponse;
import com.sample.system.card.service.domain.response.profile.UpdateCardProfileResponse;
import com.sample.system.card.service.domain.response.profile.CardProfileListResponse;
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
 * REST Controller for CardProfile Management
 * Provides endpoints for CardProfile CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/card-profile", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Card Profile Management", description = "API for managing card profiles and master data")
public class CardProfileController extends BaseController {

    private final CardProfileCommandHandler cardProfileCommandHandler;
    private final CardProfileQueryHandler cardProfileQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

        summary = "ایجاد پروفایل کارت جدید",
        description = "Create a new card profile with configuration and limits"
    )
        public ResponseEntity<BaseResponse<CreateCardProfileResponse>> createCardType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد پروفایل کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "profileName": "پروفایل کارت طلایی",
                                              "issuingBankId": 1,
                                              "feeProfileId": 1,
                                              "cardCategoryId": 1,
                                              "cardTypeId": 1,
                                              "minimumPasswordLength": 4,
                                              "maximumPasswordLength": 6,
                                              "minimumSecondaryPasswordLength": 4,
                                              "maximumSecondaryPasswordLength": 6,
                                              "allowedPinAttempts": 3,
                                              "allowedPin2Attempts": 3,
                                              "cvv2Length": 3,
                                              "validityPeriod": 24,
                                              "validityType": "ماه",
                                              "cardIssueMethod": 0,
                                              "cardNumberPatternId": 1,
                                              "initialCardStatus": true,
                                              "changeCvv2OnExpirationDateRenewal": true,
                                              "isActive": true,
                                              "additionalInfoIsRequired": false,
                                              "pin2GenMethod": 1,
                                              "reloadable": null,
                                              "cardRenewalType": 1,
                                              "salesMethod": null,
                                              "maxNumCreditCard": 5,
                                              "maximumAllowedAmount": 10000000
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Card profile creation details", required = true)
            @RequestBody @Valid CreateCardProfileCommand createCardProfileCommand) throws CardDomainException {

        log.info("Creating card profile: {}", createCardProfileCommand.getProfileName());
        CreateCardProfileResponse response = cardProfileCommandHandler.createCardProfile(createCardProfileCommand);
        return ok(response);
    }

    @PutMapping(path = "/update")
    @Operation(

        summary = "به‌روزرسانی پروفایل کارت",
        description = "Update an existing card profile"
    )
    public ResponseEntity<BaseResponse<UpdateCardProfileResponse>> updateCardType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای به‌روزرسانی پروفایل کارت - فقط فیلدهایی که می‌خواهید به‌روزرسانی شوند را ارسال کنید",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "profileName": "پروفایل کارت طلایی - به‌روزرسانی شده",
                                              "issuingBankId": 1,
                                              "feeProfileId": 1,
                                              "cardCategoryId": 1,
                                              "cardTypeId": 1,
                                              "minimumPasswordLength": 4,
                                              "maximumPasswordLength": 6,
                                              "minimumSecondaryPasswordLength": 4,
                                              "maximumSecondaryPasswordLength": 6,
                                              "allowedPinAttempts": 3,
                                              "allowedPin2Attempts": 3,
                                              "cvv2Length": 3,
                                              "validityPeriod": 24,
                                              "validityType": "ماه",
                                              "cardIssueMethod": 0,
                                              "cardNumberPatternId": 1,
                                              "initialCardStatus": true,
                                              "changeCvv2OnExpirationDateRenewal": true,
                                              "additionalInfoIsRequired": false,
                                              "pin2GenMethod": 1,
                                              "reloadable": null,
                                              "cardRenewalType": 1,
                                              "salesMethod": null,
                                              "maxNumCreditCard": 5,
                                              "maximumAllowedAmount": 10000000
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Card profile update details", required = true)
            @RequestBody @Valid UpdateCardProfileCommand updateCardTypeCommand) throws CardDomainException {

        log.info("Updating card profile with ID: {}", updateCardTypeCommand.getCardProfileId());
        UpdateCardProfileResponse response = cardProfileCommandHandler.updateCardProfile(updateCardTypeCommand);
        return ok(response);
    }

    @GetMapping(path = "/{cardProfileId}")
    @Operation(

        summary = "دریافت اطلاعات پروفایل کارت",
        description = "Get card profile details by ID"
    )
    public ResponseEntity<BaseResponse<GetCardProfileResponse>> getCardType(
            @Parameter(description = "Card profile ID", required = true, example = "1")
            @PathVariable Long cardProfileId) throws CardDomainException {

        log.info("Getting card profile with ID: {}", cardProfileId);
        GetCardProfileQuery query = GetCardProfileQuery.builder().cardProfileId(cardProfileId)
                .build();
        GetCardProfileResponse response = cardProfileQueryHandler.getCardProfile(query);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی پروفایل کارت",
            description = "")
    public ResponseEntity<BaseResponse<CardProfileListResponse>> listCardProfiles(
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
                                                  "profileName": "Gold Debit",
                                                  "cardTypeId": "1",
                                                  "cardCategoryId": "2",
                                                  "issuingBankId": "1",
                                                  "feeProfileId": "1",
                                                  "cardNumGenerationMethod": "INCREMENTAL",
                                                  "isActive": "true",
                                                  "initialCardStatus": "true",
                                                  "issueMethod": "1",
                                                  "validityType": "MONTH",
                                                  "validityPeriodFrom": "12",
                                                  "validityPeriodTo": "24",
                                                  "maximumAllowedAmountFrom": "1000000",
                                                  "maximumAllowedAmountTo": "5000000",
                                                  "cardRenewalType": "1",
                                                  "allowedPin2Attempts": "3",
                                                  "createDateFrom": "1404/01/01",
                                                  "createDateTo": "1404/12/20",
                                                  "page": "0",
                                                  "size":"10"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list card profiles with params {}", map);
        CardProfileListResponse listResponse = cardProfileQueryHandler.listCardProfiles(map, "", "");
        return ok(listResponse);
    }

    @PostMapping(path = "/change-status")
    @Operation(

        summary = "تغییر وضعیت پروفایل کارت",
        description = "Activate or deactivate a card profile based on requested status"
    )
    public ResponseEntity<BaseResponse<UpdateCardProfileResponse>> changeStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای تغییر وضعیت پروفایل کارت",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "cardProfileId": 1,
                                              "active": true
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid ChangeCardProfileStatusCommand command) throws CardDomainException {

        log.info("Changing card profile status - ProfileId: {}, Active: {}", command.getCardProfileId(), command.getActive());
        UpdateCardProfileResponse response = cardProfileCommandHandler.changeStatus(command);
        return ok(response);
    }

    @GetMapping(path = "/health")
    @Operation(
        summary = "Health check for CardProfile service",
        description = "Check if CardProfile service is working properly"
    )
    public ResponseEntity<String> healthCheck() {
        log.info("CardProfile service health check");
        return ResponseEntity.ok("CardProfile service is running!");
    }
}
