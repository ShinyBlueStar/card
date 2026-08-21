package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.CustomerCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CustomerDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CustomerCommandJpaRepository;
import com.sample.system.card.service.domain.entity.Customer;
import com.sample.system.card.service.domain.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Customer Repository Implementation
 * Following DDD infrastructure patterns with adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerCommandJpaRepository customerJpaRepository;
    private final CustomerDataAccessMapper customerDataAccessMapper;

    @Override
    public Customer save(Customer customer) {
        log.debug("Saving customer with nationalId: {}", customer.getNationalId());
        CustomerCommandEntity entity = customerDataAccessMapper.toEntity(customer);
        CustomerCommandEntity savedEntity = customerJpaRepository.save(entity);
        log.debug("Customer saved successfully - CustomerId: {}, NationalId: {}",
                savedEntity.getId(), savedEntity.getNationalId());
        return customerDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Customer> findByNationalId(String nationalId) {
        log.debug("Finding customer by nationalId: {}", nationalId);
        return customerJpaRepository.findByNationalId(nationalId)
                .map(customerDataAccessMapper::toDomain);
    }
}

