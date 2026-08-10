package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.command.GenerateAccessFileForRequestCommand;
import com.sample.system.card.service.domain.command.base.BaseSearchQuery;
import com.sample.system.card.service.domain.command.cardRequest.*;
import com.sample.system.card.service.domain.command.cardRequest.CreateRenewRequestCommand;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.handler.Command.CardRequestCommandHandler;
import com.sample.system.card.service.domain.handler.query.CardRequestQueryHandler;
import com.sample.system.card.service.domain.handler.query.FileDownloadQueryHandler;
import com.sample.system.card.service.domain.response.base.BaseResponse;
import com.sample.system.card.service.domain.response.request.CardRequestListResponse;
import com.sample.system.card.service.domain.response.request.CreateCardRequestResponse;
import com.sample.system.card.service.domain.response.request.GetCardRequestResponse;
import com.sample.system.card.service.domain.response.request.UpdateCardRequestResponse;
import com.sample.system.card.service.domain.response.request.GenerateAccessFileForRequestResponse;
import com.sample.system.card.service.domain.response.request.BatchProcessCardRequestsResponse;
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

/**
 * REST Controller for Card Request Management
 * Provides endpoints for Card Request CRUD operations
 * Following REST API best practices with Swagger documentation
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/card-request", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Card Request Management", description = "API for managing card requests and processing")
public class CardRequestController extends BaseController {

    private final CardRequestCommandHandler cardRequestCommandHandler;
    private final CardRequestQueryHandler cardRequestQueryHandler;
    private final FileDownloadQueryHandler fileDownloadQueryHandler;

    @PostMapping(path = "issue/create")
    @Operation(

        summary = "ایجاد درخواست جدید صدور کارت ",
        description = "Create a new card request with customer information and card profile"
    )
    public ResponseEntity<BaseResponse<CreateCardRequestResponse>> initiateCardRequest(
            @Parameter(description = "Card request creation details", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد درخواست کارت",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nationalId": "0012345678",
                                              "caseNumber": "CASE-123456",
                                              "cardProfileId": 10,
                                              "unitCode": "98",
                                              "unitName": "mehrvatan",
                                              "firstName": "Fatemeh",
                                              "lastName": "Ahmadi",
                                              "issuerPersonId": "323"
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid CreateIssueRequestCommand createCommand) throws CardDomainException {

        log.info("Creating ISSUE card request for nationalId: {}", createCommand.getNationalId());
        CreateCardRequestResponse response = cardRequestCommandHandler.createCardRequest(createCommand);
        return ok(response);
    }

    @GetMapping(path = "/{requestId}")//ToDO: its better to be a QueryParam, instead Of pathParam
    @Operation(// check validations

        summary = "دریافت اطلاعات درخواست ",
        description = "Get card request details by ID"
    )
    public ResponseEntity<BaseResponse<GetCardRequestResponse>> getCardRequest(
            @Parameter(description = "Card request ID", required = true, example = "1")
            @PathVariable Long requestId) throws CardDomainException {

        log.info("Getting card request with ID: {}", requestId);
        GetCardRequestQuery query = GetCardRequestQuery.builder().cardRequestId(requestId).build();
        GetCardRequestResponse response = cardRequestQueryHandler.getCardRequest(query);
        return ok(response);
    }

    @PostMapping(path = "/{requestId}/generate-access-file")
    @Operation(

        summary = "تولید فایل اکسس برای درخواست",
        description = "Generate Access file for a card request if status is PRINT_EXPECT"
    )
    public ResponseEntity<BaseResponse<GenerateAccessFileForRequestResponse>> generateAccessFileForRequest(
            @Parameter(description = "Card request ID", required = true, example = "1")
            @PathVariable Long requestId) throws CardDomainException {

        log.info("Generating Access file for request ID: {}", requestId);
        GenerateAccessFileForRequestCommand command = GenerateAccessFileForRequestCommand.builder()
                .requestId(requestId)
                .build();
        GenerateAccessFileForRequestResponse response = cardRequestCommandHandler.generateAccessFileForRequest(command);
        return ok(response);
    }

    @PostMapping(path = "/batch-process")
    @Operation(

        summary = "پردازش دسته‌ای درخواست‌های صدور کارت",
        description = "Batch process card requests: validate ISSUED requests with cards, export to Excel, and update status to PRINT_EXPECT"
    )
    public ResponseEntity<BaseResponse<BatchProcessCardRequestsResponse>> batchProcessCardRequests(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of request IDs to process",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"requestIds\": [1, 2, 3, 4, 5]}"
                            )
                    )
            )
            @RequestBody @Valid BatchProcessCardRequestsCommand command) throws CardDomainException {

        log.info("Batch processing card requests - Count: {}", command.getRequestIds().size());
        BatchProcessCardRequestsResponse response = cardRequestCommandHandler.batchProcessCardRequests(command);
        return ok(response);
    }

    @PostMapping(path = "/download/{hashId}")
    @Operation(

        summary = "دانلود فایل اکسس",
        description = "Download Access file using secure hashId with token and access level validation"
    )
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @Parameter(description = "Secure hash ID for file download", required = true, example = "abc123...")
            @PathVariable String hashId,
            @Parameter(description = "Access token for authorization", required = false)
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "Access level required", required = false)
            @RequestParam(value = "accessLevel", required = false, defaultValue = "READ") String accessLevel) throws CardDomainException {

        log.info("Downloading file with hashId: {}", hashId);

        // Extract token from "Bearer <token>" format if present
        String actualToken = token;
        if (token != null && token.startsWith("Bearer ")) {
            actualToken = token.substring(7);
        }

        org.springframework.core.io.Resource resource = fileDownloadQueryHandler.downloadFile(hashId, actualToken, accessLevel);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                        "application/vnd.ms-access")
                .body(resource);
    }

    @PostMapping(path = "/list")
    @Operation( summary = "جستجوی درخواست های کارت",
            description = "")
    public ResponseEntity<BaseResponse<CardRequestListResponse>> listCardRequests(
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
                                                  "profileId": "12",
                                                  "unitName": "شعبه عباس آباد",
                                                  "unitCode": "12345",
                                                  "createdDateFrom": "1404/09/01",
                                                  "createdDateTo": "1404/12/29",
                                                  "profileId": "12",
                                                  "nationalId": "2219911111",
                                                  "caseNumber": "CASE-998877",
                                                  "cardNumber": "6037996712345678",
                                                  "lastName": "ahmadi",
                                                  "firstName": "fatemeh",
                                                  "page": "0",
                                                  "size": "10"
                                                }
                                              }
                                            """
                            )
                    )
            ) BaseSearchQuery baseSearchQuery) throws CardDomainException {

        var map = searchParams(baseSearchQuery);
        log.info("list card profiles with params {}", map);
        CardRequestListResponse listResponse = cardRequestQueryHandler.listCardRequests(map, "", "");
        return ok(listResponse);
    }

    @PostMapping(path = "/process")
    @Operation(

        summary = "پردازش درخواست",
        description = "Process a card request based on request type (صدور کارت, تمدید کارت, المثنی, صدور مجدد)"
    )
    public ResponseEntity<BaseResponse<UpdateCardRequestResponse>> processCardRequest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای پردازش درخواست",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "cardRequestId": 7001,
                                              "requestType": 1
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid ProcessCardRequestCommand command) throws CardDomainException {

        log.info("Processing card request with ID: {}, Type: {}", command.getCardRequestId(), command.getRequestType());
        UpdateCardRequestResponse response = cardRequestCommandHandler.processCardRequest(command);
        return ok(response);
    }

    @PutMapping(path = "/cancel/{requestId}")
    @Operation(

        summary = "لغو درخواست",
        description = "Cancel a card request if it's in a cancelable status"
    )
    public ResponseEntity<BaseResponse<UpdateCardRequestResponse>> cancelCardRequest(
            @Parameter(description = "Card request ID to cancel", required = true, example = "1")
            @PathVariable Long requestId) throws CardDomainException {

        log.info("Canceling card request with ID: {}", requestId);
        CancelCardRequestCommand command = CancelCardRequestCommand.builder()
                .cardRequestId(requestId)
                .build();
        UpdateCardRequestResponse response = cardRequestCommandHandler.cancelCardRequest(command);
        return ok(response);
    }

    @PostMapping(path = "/replace/create")
    @Operation(

        summary = "ایجاد درخواست جدید صدور کارت المثنی",
        description = "Create a new Card Replace request with a new one (new PAN/CVV2/Tracks)"
    )
    public ResponseEntity<BaseResponse<CreateCardRequestResponse>> replaceCard(
            @Parameter(description = "Card replacement command", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای ایجاد درخواست صدور کارت المثنی",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "cardId": 12345,
                                              "nationalId": "0012345678",
                                              "issuerPersonId": "123456",
                                              "unitId": "001",
                                              "reasonId": 9001
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid CreateReplacementRequestCommand command) throws CardDomainException {

        log.info("Creating Replace Card. CardId: {}", command.getCardId());
        CreateCardRequestResponse response = cardRequestCommandHandler.createReplaceCardRequest(command);
        return ok(response);
    }

   @PostMapping(path = "/renew/create")
   @Operation(

       summary = "تمدید کارت",
       description = "Renew a card by issuing new expiration date and CVV2"
   )
   public ResponseEntity<BaseResponse<CreateCardRequestResponse>> renewCard(
           @Parameter(description = "Card renewal command", required = true)
           @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "نمونه JSON برای تمدید کارت",
                   required = true,
                   content = @Content(
                           mediaType = "application/json",
                           examples = @ExampleObject(
                                   value = """
                                           {
                                             "cardId": 12345,
                                             "nationalId": "0012345678",
                                             "issuerPersonId": "123456",
                                             "unitId": "001"
                                           }
                                           """
                           )
                   )
           )
           @RequestBody @Valid CreateRenewRequestCommand command) throws CardDomainException {

       log.info("Creating renew card request. CardId: {}", command.getCardId());
       CreateCardRequestResponse response = cardRequestCommandHandler.createRenewCardRequest(command);
       return ok(response);
   }

    @PutMapping(path = "/update-status")
    @Operation(

        summary = "به‌روزرسانی وضعیت درخواست",
        description = "Update card request status"
    )
    public ResponseEntity<BaseResponse<UpdateCardRequestResponse>> updateCardRequestStatus(
            @Parameter(description = "Card request status update details", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "نمونه JSON برای به‌روزرسانی وضعیت درخواست کارت",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "cardRequestId": 7001,
                                              "newStatus": 2
                                            }
                                            """
                            )
                    )
            )
            @RequestBody @Valid UpdateCardRequestStatusCommand command) throws CardDomainException {

        log.info("Updating card request status for ID: {}", command.getCardRequestId());
        UpdateCardRequestResponse response = cardRequestCommandHandler.updateCardRequestStatus(command);
        return ok(response);
    }

    @GetMapping(path = "/health")
    @Operation(
        summary = "Health check for Card Request service",
        description = "Check if Card Request service is working properly"
    )
    public ResponseEntity<String> healthCheck() {
        log.info("Card Request service health check");
        return ResponseEntity.ok("Card Request service is running!");
    }
}
