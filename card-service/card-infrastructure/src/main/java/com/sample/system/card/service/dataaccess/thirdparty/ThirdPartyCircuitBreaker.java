package com.sample.system.card.service.dataaccess.thirdparty;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Runs calls to an external service through a named circuit breaker
 * (configured under {@code resilience4j.circuitbreaker.instances.<name>}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdPartyCircuitBreaker {

    private static final int SERVICE_UNAVAILABLE = 503;

    private final CircuitBreakerRegistry registry;

    @FunctionalInterface
    public interface Call<T, E1 extends Exception, E2 extends Exception> {
        T run() throws E1, E2;
    }

    /**
     * @param name           circuit breaker instance name
     * @param openResultCode result code of the {@link ThirdPartyException} thrown while the circuit is open
     */
    public <T, E1 extends Exception, E2 extends Exception> T execute(String name, int openResultCode, Call<T, E1, E2> call)
            throws ThirdPartyException, E1, E2 {
        CircuitBreaker breaker = registry.circuitBreaker(name);
        try {
            breaker.acquirePermission();
        } catch (CallNotPermittedException e) {
            log.warn("Circuit breaker '{}' is {}: call rejected", name, breaker.getState());
            throw new ThirdPartyException("Circuit breaker '" + name + "' is open", openResultCode,
                    "service temporarily unavailable", SERVICE_UNAVAILABLE, "circuit breaker open: " + name);
        }
        long start = System.nanoTime();
        try {
            T result = call.run();
            breaker.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            return result;
        } catch (Exception e) {
            breaker.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, e);
            throw e;
        }
    }
}
