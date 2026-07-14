package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.feeProfile.CreateFeeProfileCommand;
import com.sample.system.card.service.domain.command.feeProfile.GetFeeProfileQuery;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.FeeProfileCommandHandler;
import com.sample.system.card.service.domain.handler.query.FeeProfileQueryHandler;
import com.sample.system.card.service.domain.response.feeProfile.FeeProfileListResponse;
import com.sample.system.card.service.domain.response.feeProfile.CreateFeeProfileResponse;
import com.sample.system.card.service.domain.response.feeProfile.GetFeeProfileResponse;
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
 * REST Controller for FeeProfile Management
 * Provides endpoints for FeeProfile CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/fee-profile", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Fee Profile Management", description = "API for managing fee profiles")
public class FeeProfileController extends BaseController {

    private final FeeProfileCommandHandler feeProfileCommandHandler;
    private final FeeProfileQueryHandler feeProfileQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

        summary = "ایجاد پروفایل کارمزد جدید",
        description = "Create a new fee profile"
    )
    public ResponseEntity<BaseResponse<CreateFeeProfileResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد پروفایل کارمزد",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "name": "پروفایل کارمزد استاندارد",
                                              "description": "پروفایل کارمزد برای کارت‌های معمولی",
                                              "isActive": true
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Fee profile creation details", required = true)
            @RequestBody @Valid CreateFeeProfileCommand createFeeProfileCommand) throws CardDomainException {

        log.info("Creating fee profile: {}", createFeeProfileCommand.getName());
        CreateFeeProfileResponse response = feeProfileCommandHandler.createFeeProfile(createFeeProfileCommand);
        return ok(response);
    }

    @GetMapping(path = "/{feeProfileId}")
    @Operation(

        summary = "دریافت اطلاعات پروفایل کارمزد",
        description = "Get fee profile details by ID"
    )
    public ResponseEntity<BaseResponse<GetFeeProfileResponse>> findById(
            @Parameter(description = "Fee profile ID", required = true, example = "1")
            @PathVariable Long feeProfileId) throws CardDomainException {

        log.info("Getting fee profile with ID: {}", feeProfileId);
        GetFeeProfileQuery query = GetFeeProfileQuery.builder().feeProfileId(feeProfileId).build();
        GetFeeProfileResponse response = feeProfileQueryHandler.getFeeProfile(query);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی پروفایل کارمزد",
            description = "")
    public ResponseEntity<BaseResponse<FeeProfileListResponse>> list(
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
                                                  "name": "Standard",
                                                  "isActive": "true"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list fee profiles with params {}", map);
        FeeProfileListResponse listResponse = feeProfileQueryHandler.listFeeProfiles(map, "", "");
        return ok(listResponse);
    }
}

