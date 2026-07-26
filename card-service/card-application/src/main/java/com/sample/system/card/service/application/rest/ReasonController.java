package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.reason.CreateReasonCommand;
import com.sample.system.card.service.domain.command.reason.GetReasonQuery;
import com.sample.system.card.service.domain.enums.ReasonGroup;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.ReasonCommandHandler;
import com.sample.system.card.service.domain.handler.query.ReasonQueryHandler;
import com.sample.system.card.service.domain.response.reason.CreateReasonResponse;
import com.sample.system.card.service.domain.response.reason.GetReasonResponse;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import com.sample.system.card.service.domain.response.reason.ReasonListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/reason", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Reason Management", description = "API for managing reasons")
public class ReasonController extends BaseController {

    private final ReasonCommandHandler reasonCommandHandler;
    private final ReasonQueryHandler reasonQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

        summary = "ایجاد دلیل جدید",
        description = "Create a new reason"
    )
    public ResponseEntity<BaseResponse<CreateReasonResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد دلیل",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": 1,
                                              "title": "گم شدن کارت",
                                              "reason": "کارت توسط مشتری گم شده است",
                                              "groupId": "BLOCK_CARD"
                                            }
                                            """
                            )
                    )
            )
            @Parameter(description = "Reason creation details", required = true)
            @RequestBody @Valid CreateReasonCommand createReasonCommand) throws CardDomainException {

        log.info("Creating reason: {}", createReasonCommand.getTitle());
        CreateReasonResponse response = reasonCommandHandler.createReason(createReasonCommand);
        return ok(response);
    }

    @GetMapping(path = "/{reasonId}")
    @Operation(

        summary = "دریافت اطلاعات دلیل",
        description = "Get reason details by ID"
    )
    public ResponseEntity<BaseResponse<GetReasonResponse>> findById(
            @Parameter(description = "Reason ID", required = true, example = "1")
            @PathVariable Long reasonId) throws CardDomainException {

        log.info("Getting reason with ID: {}", reasonId);
        GetReasonQuery query = GetReasonQuery.builder().reasonId(reasonId).build();
        GetReasonResponse response = reasonQueryHandler.getReason(query);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation(

        summary = "",
        description = "")
    public ResponseEntity<BaseResponse<ReasonListResponse>> list(
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
                                                  "size": "10",
                                                  "groupId": "1"
                                                }
                                              }
                                              """
                                                  )
                                                  )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("Listing reasons with params: {}", map);
        ReasonListResponse listResponse = reasonQueryHandler.getAllReasons(map, "", "");
        return ok(listResponse);
    }

    @GetMapping(path = "/by-group/{groupId}")
    @Operation(

            summary = "دریافت دلایل بر اساس گروه",
            description = "Get reasons by group (BLOCK_CARD, UNBLOCK_CARD, ACTIVE_CARD, INACTIVE_CARD)"
    )
    public ResponseEntity<BaseResponse<ReasonListResponse>> findByGroup(
            @Parameter(description = "Reason group", required = true, example = "BLOCK_CARD")
            @PathVariable ReasonGroup groupId) throws CardDomainException {

        log.info("Getting reasons by group: {}", groupId);
        ReasonListResponse response = reasonQueryHandler.findByGroup(groupId);
        return ok(response);
    }
}

