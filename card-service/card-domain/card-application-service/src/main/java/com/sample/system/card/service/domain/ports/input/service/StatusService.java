package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.entity.Status;
import com.sample.system.card.service.domain.exception.CardDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Status Application Service Interface
 * Defines operations for Status management
 * Following Hexagonal Architecture - Input Port
 */
public interface StatusService extends BaseService {

    int GENERAL_ERROR = 999;
    int DUPLICATE_REFERENCE_NUMBER = 1;
    int PARTIAL_ACTIVATION_ERROR = 2;
    int INVALID_CARD_NUMBER = 3;
    int USER_NOT_PERMISSION = 4;
    int TOKEN_NOT_VALID = 5;
    int INPUT_PARAMETER_NOT_VALID = 6;
    int INVALID_BANK = 7;
    int CARD_PROFILE_NOT_FOUND = 8;
    int CARD_PROFILE_HAS_CARDS = 9;
    int CARDTYPECATEGORY_NOT_FOUND = 10;
    int ID_NOT_FOUND = 11;
    int DUPLICATE_PROFILE_NAME_FOUND = 12;
    int ACTIVE_FEE_PROFILE_NOT_FOUND = 13;
    int ACTIVE_BANK_NOT_FOUND = 14;
    int ACTIVE_CARD_TYPE_NOT_FOUND = 15;
    int ACTIVE_CARD_CATEGORY_NOT_FOUND = 16;
    int FEE_PROFILE_NOT_FOUND = 17;
    int LAST_STATUS_NOT_ACCEPTABLE = 18;
    int REQUEST_NOT_FOUND = 19;
    int CARD_REQUEST_STATUS_IS_INVALID = 20;
    int CARD_NOT_FOUND = 21;
    int CARD_STATUS_IS_HOT = 22;
    int REASON_NOT_FOUND = 23;
    int CREATE_ISSUE_REQUEST_ERROR = 24;
    int CREATE_REPLACEMENT_REQUEST_ERROR = 25;
    int INVALID_CARD_PARAMETER = 26;
    int CARD_NUMBER_PATTERN_NOT_FOUND = 27;
    int CARD_NUMBER_PATTERN_EXHAUSTED = 28;
    int CARD_STATUS_IS_ACTIVE = 29;
    int INVALID_CVV2_LENGTH = 30;
    int CARD_IN_TERMINAL_STATE = 31;
    int CARD_INQUIRY_ERROR = 32;
    int ACTIVE_PATTERN_DOES_NOT_EXIST = 33;
    int CARD_REQUEST_ID_REQUIRED = 34;
    int CARD_REQUEST_TYPE_REQUIRED = 35;
    int CARD_DOES_NOT_EXIST = 36;
    int PARTY_NOT_FOUND = 37;
    int PARTY_SERVICE_ERROR = 38;
    int ASSIGN_PATTERN_DOES_NOT_EXIST = 39;
    int DUPLICATE_PATTERN = 40;
    int ERROR_IN_SAVING_CARD = 41;
    int NATIONAL_CODE_DOES_NOT_EXIST = 42;
    int INVALID_SALES_METHOD = 43;
    int INVALID_RELOADABLE_METHOD = 44;
    int INVALID_RELATION_BETWEEN_ISSUE_METHOD_AND_CARD_TYPE = 45;
    int MAXIMUM_ALLOWED_AMOUNT_SHOULD_NOT_BE_NULL = 46;
    int FILE_NOT_FOUND = 47;
    int FILE_EXPIRED = 48;
    int UNAUTHORIZED = 49;
    int GENERATE_ACCESS_FILE_ERROR = 50;
    int FILE_DELETE_FAILED = 51;
    int DUPLICATE_REQUEST_FOR_NATIONALID_CASENUMBER = 52;
    int SESSION_NOT_VALID = 53;
    int SSM_SERVICE_ERROR = 54;
    /** Connection/communication with SSM server failed (e.g. IOException, connection refused). */
    int SSM_CONNECTION_ERROR = 55;
    int CARD_SECRET_GENERATION_FAILED = 56;
    int REASON_IS_MANDATORY = 57;
    int SSM_GENERATE_NULL_VALUE = 58;
    int CARD_ID_NOT_FOUND = 59;

    Status findByCode(String code) throws CardDomainException;

    Status createStatus(@Valid Status status) throws CardDomainException;

    Status updateStatusPersianDescription(@Valid Status status) throws CardDomainException;

    Page<Status> listStatuses(Map<String, String> map, String caller, String ip) throws CardDomainException;

}
