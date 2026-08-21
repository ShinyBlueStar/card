package com.sample.system.card.service.domain.ports.input.service;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.entity.Customer;

public interface CustomerService extends BaseService {

    Customer checkOrSave(Customer customer);
    Customer createOrGetCustomerFromRequest(CardRequest request);

}
