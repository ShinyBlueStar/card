package com.sample.system.card.service.dataaccess.card.adapter.externalService.credit;

import com.sample.system.card.service.domain.ports.output.externalService.CreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Real implementation of CreditService for production
 * Makes actual GRPC calls to Credit module
 * TODO: Implement actual GRPC client calls when Credit module is available
 */
@Slf4j
@Service
@Profile("production")
public class CreditServiceRealImpl implements CreditService {

    @Override
    public String getMacnaCode(String nationalCode) {
        log.info("Calling Credit module GRPC to get Macna code for nationalCode: {}", nationalCode);
        // TODO: Implement actual GRPC call to Credit module
        // Example: creditGrpcClient.getCreditInfo(nationalCode).getMacnaCode();
        throw new UnsupportedOperationException("CreditServiceRealImpl GRPC implementation not yet available");
    }
}

