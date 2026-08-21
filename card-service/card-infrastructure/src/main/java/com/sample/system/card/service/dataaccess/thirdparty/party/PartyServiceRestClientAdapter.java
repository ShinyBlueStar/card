package com.sample.system.card.service.dataaccess.thirdparty.party;

import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.domain.ports.output.PartyServiceRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter to bridge PartyServiceRestClient (output port) with PartyChannel (infrastructure)
 * Following Hexagonal Architecture - Adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartyServiceRestClientAdapter implements PartyServiceRestClient {

    private final PartyChannel partyChannel;

    @Override
    public String getIndividualInquiry(String nationalCode) throws Exception {
        try {
            log.info("Calling party channel for individual inquiry - nationalCode: {}", nationalCode);
            return partyChannel.getPartyIndividual(nationalCode);
        } catch (ThirdPartyException e) {
            log.error("Error calling party channel for individual inquiry - nationalCode: {}", nationalCode, e);
            throw new Exception("Failed to get individual party inquiry: " + e.getMessage(), e);
        }
    }

    @Override
    public String getBusinessInquiry(String nationalCode) throws Exception {
        try {
            log.debug("Calling party channel for business inquiry - nationalCode: {}", nationalCode);
            return partyChannel.getPartyBusiness(nationalCode);
        } catch (ThirdPartyException e) {
            log.error("Error calling party channel for business inquiry - nationalCode: {}", nationalCode, e);
            throw new Exception("Failed to get business party inquiry: " + e.getMessage(), e);
        }
    }
}