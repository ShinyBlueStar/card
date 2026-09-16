package com.sample.system.card.service.dataaccess.thirdparty;

import com.sample.system.card.service.dataaccess.thirdparty.party.PartyChannelRestImpl;
import com.sample.system.card.service.dataaccess.thirdparty.party.ResilientPartyChannel;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThirdPartyCircuitBreakerTest {

    private static final int OPEN_CODE = 38;

    private CircuitBreakerRegistry registry;
    private ThirdPartyCircuitBreaker breaker;

    @BeforeEach
    void setUp() {
        registry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .recordException(new ThirdPartyFailurePredicate())
                .build());
        breaker = new ThirdPartyCircuitBreaker(registry);
    }

    private static ThirdPartyException failure(int resultCode, int http) {
        return new ThirdPartyException("fail", resultCode, "msg", http, "body");
    }

    private void failTimes(int n, ThirdPartyException error) {
        for (int i = 0; i < n; i++) {
            assertThrows(ThirdPartyException.class, () -> breaker.execute("party", OPEN_CODE, () -> {
                throw error;
            }));
        }
    }

    @Test
    void opensAfterRepeatedServerFailuresAndRejectsWithoutCallingService() {
        failTimes(4, failure(WebCallUtils.GENERAL_ERROR, 500));

        assertEquals(CircuitBreaker.State.OPEN, registry.circuitBreaker("party").getState());

        boolean[] called = {false};
        ThirdPartyException e = assertThrows(ThirdPartyException.class,
                () -> breaker.execute("party", OPEN_CODE, () -> {
                    called[0] = true;
                    return "x";
                }));
        assertFalse(called[0]);
        assertEquals(OPEN_CODE, e.getResultCode());
        assertEquals(503, e.getHttpStatusCode());
    }

    @Test
    void timeoutsCountAsFailures() {
        failTimes(4, failure(WebCallUtils.TIME_OUT, -1));
        assertEquals(CircuitBreaker.State.OPEN, registry.circuitBreaker("party").getState());
    }

    @Test
    void businessErrorsDoNotOpenTheCircuit() {
        failTimes(10, failure(WebCallUtils.GENERAL_ERROR, 404));
        assertEquals(CircuitBreaker.State.CLOSED, registry.circuitBreaker("party").getState());
    }

    @Test
    void successfulCallReturnsResultAndKeepsCircuitClosed() throws Exception {
        assertEquals("ok", breaker.execute("party", OPEN_CODE, () -> "ok"));
        assertEquals(CircuitBreaker.State.CLOSED, registry.circuitBreaker("party").getState());
    }

    @Test
    void circuitsAreIndependentPerService() {
        failTimes(4, failure(WebCallUtils.GENERAL_ERROR, 503));
        assertEquals(CircuitBreaker.State.CLOSED, registry.circuitBreaker("ssm").getState());
    }

    @Test
    void partyChannelDelegatesThroughTheBreaker() throws Exception {
        PartyChannelRestImpl delegate = mock(PartyChannelRestImpl.class);
        when(delegate.getPartyIndividual("123")).thenReturn("{}");
        when(delegate.getPartyBusiness("456")).thenReturn("{b}");
        ResilientPartyChannel channel = new ResilientPartyChannel(delegate, breaker);

        assertEquals("{}", channel.getPartyInfo("123", "individual"));
        assertEquals("{b}", channel.getPartyInfo("456", "business"));

        when(delegate.getPartyIndividual("9")).thenThrow(failure(WebCallUtils.GENERAL_ERROR, 500));
        for (int i = 0; i < 4; i++) {
            assertThrows(ThirdPartyException.class, () -> channel.getPartyIndividual("9"));
        }
        ThirdPartyException open = assertThrows(ThirdPartyException.class, () -> channel.getPartyIndividual("9"));
        assertEquals("service temporarily unavailable", open.getChannelMessage());
        verify(delegate, times(4)).getPartyIndividual("9");
    }
}
