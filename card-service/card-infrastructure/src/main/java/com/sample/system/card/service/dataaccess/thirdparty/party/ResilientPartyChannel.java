package com.sample.system.card.service.dataaccess.thirdparty.party;

import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyCircuitBreaker;
import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * PartyChannel protected by the "party" circuit breaker.
 */
@Service
@Primary
@RequiredArgsConstructor
public class ResilientPartyChannel implements PartyChannel {

    static final String BREAKER = "party";

    private final PartyChannelRestImpl delegate;
    private final ThirdPartyCircuitBreaker breaker;

    @Override
    public String getPartyIndividual(String nationalCode) throws ThirdPartyException {
        return breaker.execute(BREAKER, StatusService.PARTY_SERVICE_ERROR, () -> delegate.getPartyIndividual(nationalCode));
    }

    @Override
    public String getPartyBusiness(String nationalCode) throws ThirdPartyException {
        return breaker.execute(BREAKER, StatusService.PARTY_SERVICE_ERROR, () -> delegate.getPartyBusiness(nationalCode));
    }

    @Override
    public String getPartyInfo(String nationalCode, String type) throws ThirdPartyException {
        return "business".equalsIgnoreCase(type) ? getPartyBusiness(nationalCode) : getPartyIndividual(nationalCode);
    }
}
