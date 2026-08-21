package com.sample.system.card.service.dataaccess.thirdparty.party;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.card.service.dataaccess.thirdparty.party.dto.PartyApiResponseDto;
import com.sample.system.card.service.dataaccess.thirdparty.party.dto.PartyBusinessDto;
import com.sample.system.card.service.dataaccess.thirdparty.party.dto.PartyDataDto;
import com.sample.system.card.service.dataaccess.thirdparty.party.dto.PartyIndividualDto;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyResponseParser {

    private final ObjectMapper objectMapper;

    public boolean exists(String rawJson, String nationalId) throws CardDomainException {
        PartyDataDto data = extractData(rawJson, nationalId);
        return data.getPartyIndividual() != null || data.getPartyBusiness() != null;
    }

    public Customer parseCustomer(String rawJson, String nationalId) throws CardDomainException {
        PartyDataDto data = extractData(rawJson, nationalId);

        if (data.getPartyIndividual() != null) {
            return mapIndividual(data.getPartyIndividual(), nationalId);
        }

        if (data.getPartyBusiness() != null) {
            return mapBusiness(data.getPartyBusiness(), nationalId);
        }

        throw new CardDomainException(
                "Unsupported party type",
                StatusService.PARTY_NOT_FOUND,
                HttpStatus.NOT_FOUND
        );
    }

    /* ---------------- mapping ---------------- */

    private Customer mapIndividual(PartyIndividualDto dto, String nationalId) {
        String address = extractAddress(dto);

        return Customer.builder()
                .nationalId(nationalId)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .address(address)
                .build();
    }

    private Customer mapBusiness(PartyBusinessDto dto, String nationalId) {
        return Customer.builder()
                .nationalId(nationalId)
                .firstName(dto.getFirstName())
                .build();
    }

    /* ---------------- helpers ---------------- */

    private PartyDataDto extractData(String rawJson, String nationalId) throws CardDomainException {
        PartyApiResponseDto response = deserialize(rawJson, nationalId);

        if (Boolean.FALSE.equals(response.getSuccess())) {
            throw new CardDomainException(
                    "Party service returned success=false",
                    StatusService.PARTY_NOT_FOUND,
                    HttpStatus.NOT_FOUND
            );
        }

        if (response.getData() == null) {
            throw new CardDomainException(
                    "Party data is null",
                    StatusService.PARTY_NOT_FOUND,
                    HttpStatus.NOT_FOUND
            );
        }

        return response.getData();
    }

    private PartyApiResponseDto deserialize(String rawJson, String nationalId) throws CardDomainException {
        try {
            return objectMapper.readValue(rawJson, PartyApiResponseDto.class);
        } catch (Exception e) {
            log.error("Failed to deserialize party response for {}", nationalId, e);
            throw new CardDomainException(
                    "Invalid party response structure",
                    StatusService.PARTY_SERVICE_ERROR,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String extractAddress(PartyIndividualDto dto) {
        if (dto.getStreetAddresses() == null || dto.getStreetAddresses().isEmpty()) {
            return null;
        }
        return null;
    }
}
