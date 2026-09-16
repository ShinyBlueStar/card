package com.sample.system.card.service.dataaccess.thirdparty;

import java.util.function.Predicate;

/**
 * Decides which errors count as a failure of the remote service for the circuit breaker.
 * Timeouts, connection problems and 5xx responses do; business answers (4xx such as
 * "party not found" or "wrong PIN") do not.
 */
public class ThirdPartyFailurePredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        if (!(throwable instanceof ThirdPartyException e)) {
            return false;
        }
        int http = e.getHttpStatusCode();
        return e.getResultCode() == WebCallUtils.TIME_OUT || http == 408 || http >= 500;
    }
}
