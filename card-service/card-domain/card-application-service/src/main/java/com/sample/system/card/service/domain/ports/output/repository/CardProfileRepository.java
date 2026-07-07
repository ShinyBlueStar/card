package com.sample.system.card.service.domain.ports.output.repository;

import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.CardDomainException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for CardProfile operations
 * Following DDD repository pattern
 */
public interface CardProfileRepository {

    /**
     * Save a card profile
     */
    CardProfile save(CardProfile cardProfile) throws CardDomainException;

    /**
     * Find card profile by ID
     */
    Optional<CardProfile> findById(Long id);

    /**
     * Find all card profiles
     */
    List<CardProfile> findAll();

    /**
     * Find all active card profiles
     */
    List<CardProfile> findActiveCardProfiles();

    /**
     * Find card profiles by issuing bank
     */
    List<CardProfile> findByIssuingBankId(Long bankId);

    /**
     * Find card profiles that require credit limit
     */
    List<CardProfile> findCardProfilesRequiringCreditLimit();

    /**
     * Find rechargeable card profiles
     */
    List<CardProfile> findRechargeableCardProfiles();

    /**
     * Count total card profiles
     */
    long count();

    /**
     * Delete card profile by ID
     */
    void deleteById(Long id);

    Page<CardProfile> findAllProfiles(Map<String, String> mapParameter);

    boolean existsByProfileName(String profileName) throws CardDomainException;
}
