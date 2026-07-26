package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.CreateStatusCommand;
import com.sample.system.card.service.domain.command.UpdateStatusCommand;
import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.StatusCommandHandler;
import com.sample.system.card.service.domain.handler.query.StatusQueryHandler;
import com.sample.system.card.service.domain.response.CreateStatusResponse;
import com.sample.system.card.service.domain.response.StatusListResponse;
import com.sample.system.card.service.domain.response.UpdateStatusResponse;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/status", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Status Management", description = "API for managing status messages")
public class StatusController extends BaseController {

    private final StatusCommandHandler statusCommandHandler;
    private final StatusQueryHandler statusQueryHandler;

    @PostMapping(path = "/create")
    @Operation(

            summary = "Create status",
            description = "Create a new status with persian description"
    )
    public ResponseEntity<BaseResponse<CreateStatusResponse>> createStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON payload for creating a new status",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "1000",
                                              "description": "Sample description",
                                              "persianDescription": "توضیح وضعیت"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid CreateStatusCommand command) throws CardDomainException {

        log.info("Creating status with code: {}", command.getCode());
        CreateStatusResponse response = statusCommandHandler.createStatus(command);
        return ok(response);
    }

    @PutMapping(path = "/update")
    @Operation(

            summary = "Update status persianDescription",
            description = "Update persianDescription for an existing status using its code"
    )
    public ResponseEntity<BaseResponse<UpdateStatusResponse>> updateStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON payload for updating status persianDescription",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "1000",
                                              "persianDescription": "توضیح وضعیت به‌روزرسانی شده"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid UpdateStatusCommand command) throws CardDomainException {

        log.info("Updating status persianDescription for code: {}", command.getCode());
        UpdateStatusResponse response = statusCommandHandler.updateStatusPersianDescription(command);
        return ok(response);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی وضعیت",
            description = "Search and list statuses with pagination and filters")
    public ResponseEntity<BaseResponse<StatusListResponse>> listStatuses(
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
                                                  "code": "1000",
                                                  "description": "Success",
                                                  "persianDescription": "موفق",
                                                  "page": "0",
                                                  "size": "10"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list statuses with params {}", map);
        StatusListResponse listResponse = statusQueryHandler.listStatuses(map, "", "");
        return ok(listResponse);
    }
}
