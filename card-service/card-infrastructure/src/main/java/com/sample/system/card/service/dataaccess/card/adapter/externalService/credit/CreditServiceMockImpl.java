package com.sample.system.card.service.dataaccess.card.adapter.externalService.credit;

import com.sample.system.card.service.domain.ports.output.externalService.CreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of CreditService for testing/development
 */
@Slf4j
@Service
@Profile("!production")
public class CreditServiceMockImpl implements CreditService {

    @Override
    public String getMacnaCode(String nationalCode) {
        log.debug("Mock: Getting Macna code for nationalCode: {}", nationalCode);
        return "MOCK001";
    }
}

