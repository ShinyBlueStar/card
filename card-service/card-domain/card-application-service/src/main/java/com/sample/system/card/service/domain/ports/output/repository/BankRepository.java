package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.Bank;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for Bank operations
 * Following DDD repository pattern
 */
public interface BankRepository {

    /**
     * Save a bank
     */
    Bank save(Bank bank);

    /**
     * Find bank by ID
     */
    Optional<Bank> findById(Long id);

    /**
     * Find bank by BIN code
     */
    Optional<Bank> findByBinCode(String binCode);

    /**
     * Find all active banks
     */
    List<Bank> findActiveBanks();

    /**
     * Find all banks
     */
    List<Bank> findAll();

    /**
     * Check if bank exists by BIN code
     */
    boolean existsByBinCode(String binCode);

    /**
     * Check if bank exists by name
     */
    boolean existsByName(String name);

    /**
     * Count total banks
     */
    long count();

    /**
     * Find all banks with pagination and filtering
     */
    Page<Bank> findAllBanks(Map<String, String> mapParameter);
}
