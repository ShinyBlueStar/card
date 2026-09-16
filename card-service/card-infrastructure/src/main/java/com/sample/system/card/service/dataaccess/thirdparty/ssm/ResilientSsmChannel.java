package com.sample.system.card.service.dataaccess.thirdparty.ssm;

import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyCircuitBreaker;
import com.sample.system.card.service.dataaccess.thirdparty.ThirdPartyException;
import com.sample.system.card.service.domain.entity.CardSecret;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.input.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * SsmChannel protected by the "ssm" circuit breaker.
 */
@Service
@Primary
@RequiredArgsConstructor
public class ResilientSsmChannel implements SsmChannel {

    static final String BREAKER = "ssm";

    private final SsmChannelRestImpl delegate;
    private final ThirdPartyCircuitBreaker breaker;

    @Override
    public UUID createSession(UUID cardId) throws ThirdPartyException, CardDomainException {
        return breaker.execute(BREAKER, StatusService.SSM_CONNECTION_ERROR, () -> delegate.createSession(cardId));
    }

    @Override
    public void invalidateSession(UUID sessionId) throws ThirdPartyException, CardDomainException {
        breaker.execute(BREAKER, StatusService.SSM_CONNECTION_ERROR, () -> {
            delegate.invalidateSession(sessionId);
            return null;
        });
    }

    @Override
    public String callGenerate(CardSecret cardSecret) throws ThirdPartyException, CardDomainException {
        return breaker.execute(BREAKER, StatusService.SSM_CONNECTION_ERROR, () -> delegate.callGenerate(cardSecret));
    }

    @Override
    public String callValidate(CardSecret cardSecret) throws ThirdPartyException, CardDomainException {
        return breaker.execute(BREAKER, StatusService.SSM_CONNECTION_ERROR, () -> delegate.callValidate(cardSecret));
    }
}
