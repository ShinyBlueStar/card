package com.sample.system.card.service.domain.ports.output.externalService;

import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.exception.CardDomainException;

public interface PartyGateway {

    String findByNationalId(String nationalId) throws CardDomainException;

    boolean existsByNationalId(String nationalId) throws CardDomainException;

    Customer loadCustomerByNationalId(String nationalId) throws CardDomainException;
}

