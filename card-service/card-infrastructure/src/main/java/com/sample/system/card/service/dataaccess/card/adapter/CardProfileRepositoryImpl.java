package com.sample.system.card.service.dataaccess.card.adapter;

import com.sample.system.card.service.domain.enums.CardIssueMethod;
import com.sample.system.card.service.domain.enums.CardRenewalType;
import com.sample.system.card.service.dataaccess.card.entity.command.CardCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardProfileCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCategoryCommandEntity;
import com.sample.system.card.service.dataaccess.card.entity.command.CardTypeCommandEntity;
import com.sample.system.card.service.dataaccess.card.mapper.CardProfileDataAccessMapper;
import com.sample.system.card.service.dataaccess.card.repository.CardProfileJpaRepository;
import com.sample.system.card.service.dataaccess.utility.CardSearchUtility;
import com.sample.system.card.service.domain.entity.CardProfile;
import com.sample.system.card.service.domain.exception.CardDomainException;
import com.sample.system.card.service.domain.ports.output.repository.CardProfileRepository;
import com.sample.system.card.service.domain.utility.date.DateUtils;
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

import java.sql.Timestamp;
import java.util.*;

/**
 * CardProfile Repository Implementation
 * Following DDD infrastructure patterns with adapter pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardProfileRepositoryImpl implements CardProfileRepository {

    private final CardProfileJpaRepository cardProfileJpaRepository;
    private final CardProfileDataAccessMapper cardProfileDataAccessMapper;

    @Override
    public CardProfile save(CardProfile cardProfile) throws CardDomainException {
        log.info("Saving card profile with ID: {}", cardProfile.getId());
        CardProfileCommandEntity entity = cardProfileDataAccessMapper.toEntity(cardProfile);
        CardProfileCommandEntity savedEntity = cardProfileJpaRepository.save(entity);
        return cardProfileDataAccessMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CardProfile> findById(Long id) {
        log.debug("Finding card profile by ID: {}", id);
        return cardProfileJpaRepository.findById(id)
                .map(cardProfileDataAccessMapper::toDomain);
    }

    @Override
    public long count() {
        log.debug("Counting total card profiles");
        return cardProfileJpaRepository.count();
    }

    @Override
    public List<CardProfile> findAll() {
        log.debug("Finding all card profiles");
        return cardProfileJpaRepository.findAll()
                .stream()
                .map(cardProfileDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardProfile> findActiveCardProfiles() {
        return cardProfileJpaRepository.findByIsActiveTrue().stream()
                .map(cardProfileDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public List<CardProfile> findCardProfilesRequiringCreditLimit() {
        return List.of();
    }

    @Override
    public List<CardProfile> findRechargeableCardProfiles() {
        return List.of();
    }

    @Override
    public List<CardProfile> findByIssuingBankId(Long bankId) {
        return cardProfileJpaRepository.findByIssuingBankId(bankId)
                .stream()
                .map(cardProfileDataAccessMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        cardProfileJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByProfileName(String profileName) throws CardDomainException{
        log.info("Check if Existing profile with name {}", profileName);
        return cardProfileJpaRepository.existsByProfileName(profileName);
    }

    /**
     * Advanced search for CardProfile with pagination and filtering
     */
    public Page<CardProfile> findAllProfiles(Map<String, String> searchCriteria) {
        if (searchCriteria == null) {
            searchCriteria = new java.util.HashMap<>();
        }

        Pageable pageable = PageRequest.of(
            Integer.parseInt(searchCriteria.getOrDefault("page", "0")),
            Integer.parseInt(searchCriteria.getOrDefault("size", "10"))
        );

        Page<CardProfileCommandEntity> entityPage = cardProfileJpaRepository
                .findAll(getCardProfileSpecification(searchCriteria), pageable);

        List<CardProfile> profiles = entityPage.getContent().stream()
            .map(cardProfileDataAccessMapper::toDomain)
            .toList();
        log.info("card profile list size is: {}" ,  profiles.size());

        return new PageImpl<>(profiles, entityPage.getPageable(), entityPage.getTotalElements());
    }

    /**
     * Build CardProfile specification from search criteria
     */
    private Specification<CardProfileCommandEntity> getCardProfileSpecification(Map<String, String> searchCriteria) {
        return (root, query, criteriaBuilder) -> {
            // Use distinct to avoid duplicate results when using joins
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                // For count queries, don't use distinct as it can cause issues
            } else {
                query.distinct(true);
            }

            List<Predicate> predicates = buildCardProfilePredicatesFromCriteria(searchCriteria, root, query, criteriaBuilder);

            // Set ordering
            String orderBy = searchCriteria.getOrDefault("orderBy", "id");
            String sortDirection = searchCriteria.getOrDefault("sort", "desc");
            setCardProfileOrder(query, orderBy, sortDirection, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build predicates from search criteria for CardProfile
     */
    private List<Predicate> buildCardProfilePredicatesFromCriteria(Map<String, String> searchCriteria,
                                                                  Root<CardProfileCommandEntity> root,
                                                                  CriteriaQuery<?> query,
                                                                  CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        Join<CardProfileCommandEntity, CardTypeCategoryCommandEntity> cardTypeCategoryJoin = null;
        Join<CardTypeCategoryCommandEntity, CardTypeCommandEntity> cardTypeJoin = null;
        Join<CardTypeCategoryCommandEntity, CardCategoryCommandEntity> cardCategoryJoin = null;
        Join<CardProfileCommandEntity, ?> bankJoin = null;
        Join<CardProfileCommandEntity, ?> feeProfileJoin = null;

        // ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("id"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "id", searchCriteria.get("id"));
            if (p != null) predicates.add(p);
        }

        // Profile name filter (LIKE search)
        if (StringUtils.isNotEmpty(searchCriteria.get("profileName"))) {
            Predicate p = CardSearchUtility.buildFarsiLikePredicate(root, criteriaBuilder, "profileName", searchCriteria.get("profileName"));
            if (p != null) predicates.add(p);
        }

        // Product code filter
        if (StringUtils.isNotEmpty(searchCriteria.get("productCode"))) {
            Predicate p = CardSearchUtility.buildExactMatchPredicate(root, criteriaBuilder, "productCode", searchCriteria.get("productCode"));
            if (p != null) predicates.add(p);
        }

        // Card type filters
        String cardTypeIdValue = searchCriteria.get("cardTypeId");
        if (StringUtils.isNotBlank(cardTypeIdValue)) {
            try {
                if (cardTypeCategoryJoin == null) {
                    cardTypeCategoryJoin = root.join("cardTypeCategory", JoinType.INNER);
                }
                if (cardTypeJoin == null) {
                    cardTypeJoin = cardTypeCategoryJoin.join("cardType", JoinType.INNER);
                }
                Long typeId = Long.parseLong(cardTypeIdValue.trim());
                predicates.add(criteriaBuilder.equal(cardTypeJoin.get("id"), typeId));
            } catch (NumberFormatException e) {
                log.warn("Invalid cardTypeId value: {}", cardTypeIdValue, e);
            }
        }

        String cardCategoryIdValue = searchCriteria.get("cardCategoryId");
        if (StringUtils.isNotBlank(cardCategoryIdValue)) {
            try {
                if (cardTypeCategoryJoin == null) {
                    cardTypeCategoryJoin = root.join("cardTypeCategory", JoinType.INNER);
                }
                if (cardCategoryJoin == null) {
                    cardCategoryJoin = cardTypeCategoryJoin.join("cardCategory", JoinType.INNER);
                }
                Long categoryId = Long.parseLong(cardCategoryIdValue.trim());
                predicates.add(criteriaBuilder.equal(cardCategoryJoin.get("id"), categoryId));
            } catch (NumberFormatException e) {
                log.warn("Invalid cardCategoryId value: {}", cardCategoryIdValue, e);
            }
        }

        // Issuing bank filters
        String issuingBankIdValue = searchCriteria.get("issuingBankId");
        if (StringUtils.isNotBlank(issuingBankIdValue)) {
            try {
                if (bankJoin == null) {
                    bankJoin = root.join("issuingBank", JoinType.LEFT);
                }
                Long bankId = Long.parseLong(issuingBankIdValue.trim());
                predicates.add(criteriaBuilder.equal(bankJoin.get("id"), bankId));
            } catch (NumberFormatException ignored) {}
        }

        String feeProfileIdValue = searchCriteria.get("feeProfileId");
        if (StringUtils.isNotBlank(feeProfileIdValue)) {
            try {
                if (feeProfileJoin == null) {
                    feeProfileJoin = root.join("feeProfile", JoinType.LEFT);
                }
                Long feeProfileId = Long.parseLong(feeProfileIdValue.trim());
                predicates.add(criteriaBuilder.equal(feeProfileJoin.get("id"), feeProfileId));
            } catch (NumberFormatException ignored) {}
        }

        // Issuing bank ID filter
        if (StringUtils.isNotEmpty(searchCriteria.get("cardNumGenerationMethod"))) {
            try {
                Integer method = Integer.parseInt(searchCriteria.get("cardNumGenerationMethod").trim());
                predicates.add(criteriaBuilder.equal(root.get("cardNumGenerationMethod"), method));
            } catch (NumberFormatException ignored) {}
        }

        // Active status filter
        if (StringUtils.isNotEmpty(searchCriteria.get("isActive"))) {
            Boolean isActive = CardSearchUtility.parseBooleanValue(searchCriteria.get("isActive"));
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
        }

        // Initial card status filter
        if (StringUtils.isNotEmpty(searchCriteria.get("initialCardStatus"))) {
            Boolean initial = CardSearchUtility.parseBooleanValue(searchCriteria.get("initialCardStatus"));
            if (initial != null) {
                predicates.add(criteriaBuilder.equal(root.get("initialCardStatus"), initial));
            }
        }

        // Reloadable filter
        if (StringUtils.isNotEmpty(searchCriteria.get("reloadable"))) {
            Boolean reloadable = CardSearchUtility.parseBooleanValue(searchCriteria.get("reloadable"));
            if (reloadable != null) {
                predicates.add(criteriaBuilder.equal(root.get("reloadable"), reloadable));
            }
        }

        // Card renewal type filter
        if (StringUtils.isNotEmpty(searchCriteria.get("cardRenewalType"))) {
            try {
                Integer renewalType = Integer.parseInt(searchCriteria.get("cardRenewalType").trim());
                predicates.add(CardSearchUtility.buildEnumCodePredicate(root, criteriaBuilder, "cardRenewalType",
                        CardRenewalType.class, renewalType));
            } catch (Exception e) {
                log.warn("Invalid cardRenewalType value: {}", searchCriteria.get("cardRenewalType"));
            }
        }

        // Allowed PIN2 attempts filter
        if (StringUtils.isNotEmpty(searchCriteria.get("allowedPin2Attempts"))) {
            try {
                Integer attempts = Integer.parseInt(searchCriteria.get("allowedPin2Attempts").trim());
                predicates.add(criteriaBuilder.equal(root.get("allowedPin2Attempts"), attempts));
            } catch (NumberFormatException e) {
                log.warn("Invalid allowedPin2Attempts value: {}", searchCriteria.get("allowedPin2Attempts"));
            }
        }

        // Card issue method filter
        String cardIssueMethodValue = searchCriteria.get("issueMethod");
        if (StringUtils.isEmpty(cardIssueMethodValue)) {
            cardIssueMethodValue = searchCriteria.get("issueMethod");
        }
        if (StringUtils.isNotEmpty(cardIssueMethodValue)) {
            try {
                Integer issueMethod = Integer.parseInt(cardIssueMethodValue.trim());
                predicates.add(CardSearchUtility.buildEnumCodePredicate(root, criteriaBuilder, "cardIssueMethod",
                        CardIssueMethod.class, issueMethod));
            } catch (NumberFormatException ignored) {}
        }

        // Validity type filter
        if (StringUtils.isNotBlank(searchCriteria.get("validityType"))) {
            predicates.add(criteriaBuilder.equal(root.get("validityType"), searchCriteria.get("validityType").trim()));
        }

        // Validity period range filter
        String validityFrom = searchCriteria.get("validityPeriodFrom");
        String validityTo = searchCriteria.get("validityPeriodTo");
        if (StringUtils.isNotBlank(validityFrom)) {
            try {
                Integer fromValue = Integer.parseInt(validityFrom.trim());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("validityPeriod"), fromValue));
            } catch (NumberFormatException ignored) {}
        }
        if (StringUtils.isNotBlank(validityTo)) {
            try {
                Integer toValue = Integer.parseInt(validityTo.trim());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("validityPeriod"), toValue));
            } catch (NumberFormatException ignored) {}
        }

        // Credit limit range filter
        if (StringUtils.isNotEmpty(searchCriteria.get("maximumAllowedAmountFrom")) || StringUtils.isNotEmpty(searchCriteria.get("maximumAllowedAmountTo"))) {
            Predicate p = CardSearchUtility.buildNumericRangePredicate(root, criteriaBuilder, "maximumAllowedAmount",
                searchCriteria.get("maximumAllowedAmountFrom"), searchCriteria.get("maximumAllowedAmountTo"));
            if (p != null) predicates.add(p);
        }

        // Created date range filter
        String fromDateStr = searchCriteria.get("createDateFrom");
        String toDateStr   = searchCriteria.get("createDateTo");
        Path<Timestamp> datePath = root.get("createdDate");
        if ((StringUtils.isNotEmpty(fromDateStr))) {
            Date sDate;
            if (Integer.parseInt(fromDateStr.substring(0, 4)) < 1900) {
                sDate = DateUtils.parse(fromDateStr, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                sDate = DateUtils.parse(fromDateStr, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(datePath, sDate));
        }
        if (StringUtils.isNotEmpty(toDateStr)) {
            Date tDate;
            if (Integer.parseInt(toDateStr.substring(0, 4)) < 1900) {
                tDate = DateUtils.parse(toDateStr, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.FARSI_LOCALE);
            } else {
                tDate = DateUtils.parse(toDateStr, DateUtils.PERSIAN_DATE_FORMAT, true, DateUtils.ENGLISH_LOCALE);
            }
            predicates.add(criteriaBuilder.lessThanOrEqualTo(datePath, tDate));
        }

        return predicates;
    }

    /**
     * Set ordering for CardProfile query
     */
    private void setCardProfileOrder(CriteriaQuery<?> query, String orderBy, String sortDirection,
                                   CriteriaBuilder criteriaBuilder, Root<CardProfileCommandEntity> root) {
        try {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
            }
        } catch (Exception e) {
            log.warn("Invalid orderBy field: {}, using default ordering", orderBy);
            query.orderBy(criteriaBuilder.desc(root.get("id")));
        }
    }
}
