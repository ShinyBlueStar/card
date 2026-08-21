package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.Customer;

import java.util.Optional;

/**
 * Customer Repository Interface
 * Following Hexagonal Architecture - Output Port
 */
public interface CustomerRepository {

    /**
     * Save or update a customer
     * @param customer Customer entity to save
     * @return Saved customer entity
     */
    Customer save(Customer customer);

    /**
     * Find customer by national ID
     * @param nationalId National ID of the customer
     * @return Optional customer if found
     */
    Optional<Customer> findByNationalId(String nationalId);
}

