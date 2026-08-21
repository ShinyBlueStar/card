package com.sample.system.card.service.domain.ports.input.service.impl;

import com.sample.system.card.service.domain.entity.CardRequest;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.ports.input.service.CustomerService;
import com.sample.system.card.service.domain.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.Optional;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    @Override
    public Customer checkOrSave(Customer customer) {
        log.info("Checking if customer with id {} exists", customer.getId());
        Optional<Customer> optionalCustomer =
                repository.findByNationalId(customer.getNationalId());
        return optionalCustomer.orElseGet(() -> repository.save(customer));
    }

    @Override
    public Customer createOrGetCustomerFromRequest(CardRequest request) {
        log.info("Creating customer from CardRequest - NationalId: {}", request.getNationalId());
        Customer customer = Customer.builder()
                .nationalId(request.getNationalId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        return checkOrSave(customer);
    }
}
