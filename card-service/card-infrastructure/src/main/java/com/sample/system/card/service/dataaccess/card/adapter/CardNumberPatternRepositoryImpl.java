package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.domain.enums.CardNumGenerationMethod;
import com.sample.system.card.service.dataaccess.card.entity.command.CardNumberPatternCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CardNumberPatternDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CardNumberPatternCommandJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.command.numberPattern.UpdateCardNumberPatternCommand;
import com.sample.system.card.service.domain.entity.CardNumberPattern;
import com.sample.system.card.service.domain.enums.CardNumberPatternStatus;
import com.sample.system.card.service.domain.ports.output.repository.CardNumberPatternRepository;
import com.sample.system.card.service.domain.response.number.UpdateCardNumberPatternResponse;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import java.util.Objects;
import java.util.Optional;

/**
 * CardNumberPattern Repository Implementation
 * Following DDD infrastructure patterns with adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardNumberPatternRepositoryImpl implements CardNumberPatternRepository {

    private final CardNumberPatternCommandJpaRepository cardNumberPatternCommandJpaRepository;
    private final CardNumberPatternDataAccessMapper cardNumberPatternDataAccessMapper;

    @Override
    public CardNumberPattern save(CardNumberPattern cardNumberPattern) {
        Objects.requireNonNull(cardNumberPattern, "cardNumberPattern must not be null");
        log.debug("Saving card number pattern with ID: {}", cardNumberPattern.getId());
        var entity = Objects.requireNonNull(
                cardNumberPatternDataAccessMapper.toEntity(cardNumberPattern),
                "Mapped card number pattern entity must not be null");
        var savedEntity = cardNumberPatternCommandJpaRepository.save(entity);
        return cardNumberPatternDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CardNumberPattern> findById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Finding card number pattern by ID: {}", id);
        return cardNumberPatternCommandJpaRepository.findById(id)
                .map(cardNumberPatternDataAccessMapper::toDomain);
    }

    public List<CardNumberPattern> findByCardProfileId(Long cardProfileId) {
        Objects.requireNonNull(cardProfileId, "cardProfileId must not be null");
        log.debug("Finding card number patterns by profile id: {}", cardProfileId);
        return cardNumberPatternCommandJpaRepository.findByCardProfileId(cardProfileId)
                .stream()
                .map(cardNumberPatternDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardNumberPattern> findByProductCode(String productCode) {
        Objects.requireNonNull(productCode, "productCode must not be null");
        log.debug("Finding card number patterns by product code: {}", productCode);
        return cardNumberPatternCommandJpaRepository.findByProductCode(productCode)
                .stream()
                .map(cardNumberPatternDataAccessMapper::toDomain)
                .toList();
    }

    public List<CardNumberPattern> findOverlappingPatterns(Long excludeId, Long profileId, String from, String to) {
        log.debug("Finding overlapping patterns. excludeId={}, profileId={}, from={}, to={}", excludeId, profileId, from, to);
        return cardNumberPatternCommandJpaRepository.findOverlappingPatterns(excludeId, profileId, from, to)
                .stream().map(cardNumberPatternDataAccessMapper::toDomain).toList();
    }

    @Override
    public List<CardNumberPattern> findOverlappingPatternsByProductCode(Long excludeId, String productCode, String from, String to) {
        Objects.requireNonNull(productCode, "productCode must not be null");
        log.debug("Finding overlapping patterns by product code. excludeId={}, productCode={}, from={}, to={}", excludeId, productCode, from, to);
        return cardNumberPatternCommandJpaRepository.findOverlappingPatternsByProductCode(excludeId, productCode, from, to)
                .stream().map(cardNumberPatternDataAccessMapper::toDomain).toList();
    }

    @Override
    public List<CardNumberPattern> saveAll(List<CardNumberPattern> cardNumberPatterns) {
        log.debug("Saving {} card number patterns in bulk", cardNumberPatterns != null ? cardNumberPatterns.size() : 0);
        if (cardNumberPatterns == null || cardNumberPatterns.isEmpty()) {
            return List.of();
        }
        var entities = cardNumberPatterns.stream()
                .map(cardNumberPatternDataAccessMapper::toEntity)
                .filter(Objects::nonNull)
                .toList();
        if (entities.isEmpty()) {
            return List.of();
        }
        var savedEntities = cardNumberPatternCommandJpaRepository.saveAll(entities);
        return savedEntities.stream()
                .map(cardNumberPatternDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<UpdateCardNumberPatternResponse> updateAll(List<UpdateCardNumberPatternCommand> commands) {
        return List.of();
    }

    @Override
    public List<CardNumberPattern> findAll() {
        log.debug("Finding all card number patterns");
        return cardNumberPatternCommandJpaRepository.findAll()
                .stream()
                .map(cardNumberPatternDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public Page<CardNumberPattern> findAll(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<CardNumberPatternCommandEntity> entityPage = cardNumberPatternCommandJpaRepository
                .findAll(getCardNumberPatternSpecification(searchCriteria), pageable);

        List<CardNumberPattern> patterns = entityPage.getContent().stream()
            .map(cardNumberPatternDataAccessMapper::toDomain)
            .toList();

        return new PageImpl<>(patterns, entityPage.getPageable(), entityPage.getTotalElements());
    }

    /**
     * Build CardNumberPattern specification from search criteria
     */
    private Specification<CardNumberPatternCommandEntity> getCardNumberPatternSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = buildCardNumberPatternPredicatesFromCriteria(searchCriteria, root, criteriaBuilder);

            // Set ordering
            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sortDirection = searchCriteria.getOrDefault("sort", "desc");
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                if ("asc".equalsIgnoreCase(sortDirection)) {
                    query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build predicates from search criteria for CardNumberPattern
     */
    private List<Predicate> buildCardNumberPatternPredicatesFromCriteria(Map<String, String> searchCriteria,
                                                                         Root<CardNumberPatternCommandEntity> root,
                                                                         CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        // ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        // Status filter (supports both "status" and "cardNumberPatternStatus" keys)
        String statusValue = searchCriteria.get("status");
        if (StringUtils.isEmpty(statusValue)) {
            statusValue = searchCriteria.get("cardNumberPatternStatus");
        }

        if (StringUtils.isNotEmpty(statusValue)) {
            Integer statusCode = parseCardNumberPatternStatus(statusValue);
            if (statusCode != null) {
                predicates.add(CardSearchUtility.buildEnumCodePredicate(root, criteriaBuilder, "patternStatus",
                        CardNumberPatternStatus.class, statusCode));
            } else {
                log.warn("Invalid CardNumberPatternStatus value: {}", statusValue);
            }
        }

        // Card Profile ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("cardProfileId"))) {
            try {
                Long profileId = Long.parseLong(searchCriteria.get("cardProfileId"));
                predicates.add(criteriaBuilder.equal(root.get("cardProfile").get("id"), profileId));
            } catch (NumberFormatException e) {
                log.warn("cardProfileId filter value is not a valid number: {}", searchCriteria.get("cardProfileId"));
            }
        }

        // Product Code filter
        if (StringUtils.isNotEmpty(searchCriteria.get("productCode"))) {
            predicates.add(criteriaBuilder.equal(root.get("productCode"), searchCriteria.get("productCode")));
        }

        // Name filter (LIKE search)
        if (StringUtils.isNotEmpty(searchCriteria.get("name"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "name", searchCriteria.get("name"));
            if (p != null) predicates.add(p);
        }

        // Card Number Generation Method filter
        if (StringUtils.isNotEmpty(searchCriteria.get("cardNumGenerationMethod"))) {
            String method = searchCriteria.get("cardNumGenerationMethod");
            try {
                Integer methodCode = Integer.parseInt(method);
                predicates.add(CardSearchUtility.buildEnumCodePredicate(root, criteriaBuilder, "cardNumGenerationMethod",
                        CardNumGenerationMethod.class, methodCode));
            } catch (NumberFormatException e) {
                log.warn("cardNumGenerationMethod filter value is not a number: {}", method);
            }
        }

        // Created by filter
        if (StringUtils.isNotEmpty(searchCriteria.get("createdBy"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "createdBy", searchCriteria.get("createdBy"));
            if (p != null) predicates.add(p);
        }

        // Created date range filter
        if (StringUtils.isNotEmpty(searchCriteria.get("createDateFrom")) || StringUtils.isNotEmpty(searchCriteria.get("createDateTo"))) {
            Predicate p = CardSearchUtility.buildDateRangePredicate(root, criteriaBuilder, "createdDate",
                    searchCriteria.get("createDateFrom"), searchCriteria.get("createDateTo"));
            if (p != null) predicates.add(p);
        }

        // Last modified by filter
        if (StringUtils.isNotEmpty(searchCriteria.get("lastModifiedBy"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "lastModifiedBy", searchCriteria.get("lastModifiedBy"));
            if (p != null) predicates.add(p);
        }

        // Last modified date range filter
        if (StringUtils.isNotEmpty(searchCriteria.get("lastModifiedDateFrom")) || StringUtils.isNotEmpty(searchCriteria.get("lastModifiedDateTo"))) {
            Predicate p = CardSearchUtility.buildDateRangePredicate(root, criteriaBuilder, "lastModifiedDate",
                    searchCriteria.get("lastModifiedDateFrom"), searchCriteria.get("lastModifiedDateTo"));
            if (p != null) predicates.add(p);
        }

        return predicates;
    }

    private Integer parseCardNumberPatternStatus(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return null;
        }

        String trimmed = statusValue.trim();

        // Try to parse as integer code first
        try {
            Integer code = Integer.parseInt(trimmed);
            // Validate that the code exists in enum
            try {
                CardNumberPatternStatus.fromCode(code);
                return code;
            } catch (Exception e) {
                log.warn("Invalid CardNumberPatternStatus code: {}", code);
                return null;
            }
        } catch (NumberFormatException e) {
            // Not a number, try to match as enum name
            try {
                CardNumberPatternStatus status = CardNumberPatternStatus.valueOf(trimmed.toUpperCase());
                return status.getCode();
            } catch (IllegalArgumentException ex) {
                // Try to match by description (Persian)
                for (CardNumberPatternStatus enumStatus : CardNumberPatternStatus.values()) {
                    if (enumStatus.getDescription().equals(trimmed) ||
                        enumStatus.name().equalsIgnoreCase(trimmed)) {
                        return enumStatus.getCode();
                    }
                }
                log.warn("Invalid CardNumberPatternStatus value: {}", trimmed);
                return null;
            }
        }
    }

    @Override
    public long count() {
        log.debug("Counting total card number patterns");
        return cardNumberPatternCommandJpaRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Deleting card number pattern by ID: {}", id);
        cardNumberPatternCommandJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Checking if card number pattern exists by ID: {}", id);
        return cardNumberPatternCommandJpaRepository.existsById(id);
    }

    @Override
    public Optional<CardNumberPattern> findByIdForUpdate(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        log.debug("Finding card number pattern with PESSIMISTIC_WRITE lock. ID={}", id);

        return cardNumberPatternCommandJpaRepository.findByIdForUpdate(id)
                .map(cardNumberPatternDataAccessMapper::toDomain);
    }
}
