package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.dataaccess.card.entity.command.BankCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.BankDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.BankJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.Bank;
import com.sample.system.card.service.domain.ports.output.repository.BankRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankRepositoryImpl implements BankRepository {

    private final BankJpaRepository bankJpaRepository;
    private final BankDataAccessMapper bankDataAccessMapper;

    @Override
    public Bank save(Bank bank) {
        log.info("Saving bank with BIN code: {} and name: {}", bank.getBinCode(), bank.getName());
        BankCommandEntity bankEntity = bankDataAccessMapper.toEntity(bank);
        BankCommandEntity savedEntity = bankJpaRepository.save(bankEntity);
        Bank savedBank = bankDataAccessMapper.toDomain(savedEntity);
        log.info("Bank saved successfully with ID: {}", savedBank.getId().getValue());
        return savedBank;
    }

    @Override
    public Optional<Bank> findById(Long id) {
        log.info("Finding bank by ID: {}", id);
        Optional<Bank> bank = bankJpaRepository.findById(id)
                .map(bankDataAccessMapper::toDomain);
        bank.ifPresentOrElse(
                found -> log.info("Bank found with ID: {}", id),
                () -> log.warn("Bank not found with ID: {}", id));
        return bank;
    }

    @Override
    public Optional<Bank> findByBinCode(String binCode) {
        log.info("Finding bank by BIN code: {}", binCode);
        Optional<Bank> bank = bankJpaRepository.findByBinCode(binCode)
                .map(bankDataAccessMapper::toDomain);
        bank.ifPresentOrElse(
                found -> log.info("Bank found with BIN code: {}", binCode),
                () -> log.warn("Bank not found with BIN code: {}", binCode));
        return bank;
    }

    @Override
    public List<Bank> findAll() {
        log.info("Finding all banks");
        List<Bank> banks = bankJpaRepository.findAll()
                .stream()
                .map(bankDataAccessMapper::toDomain)
                .toList();
        log.info("Found {} banks", banks.size());
        return banks;
    }

    @Override
    public List<Bank> findActiveBanks() {
        log.info("Finding all active banks");
        List<Bank> banks = bankJpaRepository.findByIsActiveTrue()
                .stream()
                .map(bankDataAccessMapper::toDomain)
                .toList();
        log.info("Found {} active banks", banks.size());
        return banks;
    }

    @Override
    public long count() {
        log.info("Counting total banks");
        long count = bankJpaRepository.count();
        log.info("Total bank count: {}", count);
        return count;
    }

    @Override
    public boolean existsByName(String name) {
        log.info("Checking if bank exists by name: {}", name);
        boolean exists = bankJpaRepository.existsByName(name);
        log.info("Bank exists by name '{}': {}", name, exists);
        return exists;
    }

    @Override
    public boolean existsByBinCode(String binCode) {
        log.info("Checking if bank exists by BIN code: {}", binCode);
        boolean exists = bankJpaRepository.existsByBinCode(binCode);
        log.info("Bank exists by BIN code '{}': {}", binCode, exists);
        return exists;
    }

    @Override
    public Page<Bank> findAllBanks(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<BankCommandEntity> entityPage = bankJpaRepository
                .findAll(getBankSpecification(searchCriteria), pageable);

        List<Bank> banks = entityPage.getContent().stream()
            .map(bankDataAccessMapper::toDomain)
            .toList();
        log.info("bank list size is: {}", banks.size());

        return new PageImpl<>(banks, entityPage.getPageable(), entityPage.getTotalElements());
    }

    private Specification<BankCommandEntity> getBankSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildBankPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);

            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sortDirection = searchCriteria.getOrDefault("sort", "desc");
            if ("asc".equalsIgnoreCase(sortDirection)) {
                query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildBankPredicatesFromCriteria(Map<String, String> searchCriteria,
                                                             Root<BankCommandEntity> root,
                                                             CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("name"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "name", searchCriteria.get("name"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("binCode"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "binCode", searchCriteria.get("binCode"));
            if (p != null) predicates.add(p);
        }

        if (StringUtils.isNotEmpty(searchCriteria.get("isActive"))) {
            Boolean isActive = CardSearchUtility.parseBooleanValue(searchCriteria.get("isActive"));
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
        }

        return predicates;
    }
}